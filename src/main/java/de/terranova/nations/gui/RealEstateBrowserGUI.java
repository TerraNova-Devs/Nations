package de.terranova.nations.gui;

import de.terranova.nations.command.commands.CachedSupplier;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.realEstate.HasRealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateMarketCache;
import de.mcterranova.terranovaLib.utils.Chat;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

public class RealEstateBrowserGUI extends RoseGUI {

    private static final UUID GLOBAL_UUID = new UUID(0L, 0L);

    private static final Map<UUID, CachedSupplier<List<HasRealEstateAgent>>> OFFER_CACHE =
            new ConcurrentHashMap<>();

    private final RosePagination pagination = new RosePagination(this);
    private final UUID agentUUID;

    private FilterMode filterMode = FilterMode.ALL;
    private SortOrder sortOrder = SortOrder.ASC;

    // lightweight view model so we always know if an item is private
    private record OfferRow(RealEstateAgent agent, boolean isPrivate) {}

    public RealEstateBrowserGUI(@NotNull Player player, Region agentRegion) {
        super(
                player,
                "realestate-browser",
                Chat.blueFade(
                        String.format(
                                "RealEstate: %s", agentRegion != null ? agentRegion.getName() : "Global")),
                6);
        this.agentUUID = agentRegion != null ? agentRegion.getId() : GLOBAL_UUID;

        pagination.registerPageSlotsBetween(10, 16);
        pagination.registerPageSlotsBetween(19, 25);
        pagination.registerPageSlotsBetween(28, 34);
        pagination.registerPageSlotsBetween(37, 43);

        // Default cache time: 1min (region-specific) or 5min (global)
        OFFER_CACHE.computeIfAbsent(
                agentUUID,
                id -> {
                    if (id.equals(GLOBAL_UUID)) {
                        // Flatten all regions into one list
                        return new CachedSupplier<>(RealEstateMarketCache::getAllListings,20 * 60 * 5); // 5 minutes
                    } else {
                        return new CachedSupplier<>(() -> RealEstateMarketCache.getListing(id), 20 * 60);
                    }
                });
    }

    private List<RealEstateAgent> privateOffers = List.of();

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.privateOffers = RealEstateAgent.offerCache.getOrDefault(player.getUniqueId(), List.of());

        RoseItem fillerDark =
                new RoseItem.Builder()
                        .showTooltip(false)
                        .material(Material.BLACK_STAINED_GLASS_PANE)
                        .build();
        outlineGui(fillerDark);

        RoseItem next =
                new RoseItem.Builder()
                        .material(Material.SPECTRAL_ARROW)
                        .displayName(Chat.redFade("<b>Nächste Seite"))
                        .build()
                        .onClick(e -> pagination.goNextPage());
        addItem(next, 53);

        RoseItem last =
                new RoseItem.Builder()
                        .material(Material.SPECTRAL_ARROW)
                        .displayName(Chat.redFade("<b>Vorherige Seite"))
                        .build()
                        .onClick(e -> pagination.goPreviousPage());
        addItem(last, 45);

        createSortItems();

        pagination.addItem(calculateOffers().toArray(new RoseItem[0]));
        pagination.update();
    }

    private void createSortItems() {
        RoseItem filter =
                new RoseItem.Builder()
                        .material(Material.HOPPER)
                        .displayName(Chat.redFade("<b>Filter: " + filterMode.name()))
                        .addLore("Klicke zum Wechseln: ALL → BUY → RENT → PRIVATE")
                        .build()
                        .onClick(
                                e -> {
                                    filterMode =
                                            switch (filterMode) {
                                                case ALL -> FilterMode.BUY;
                                                case BUY -> FilterMode.RENT;
                                                case RENT -> FilterMode.PRIVATE;
                                                case PRIVATE -> FilterMode.ALL;
                                            };
                                    pagination.clearAllItems();
                                    pagination.addItem(calculateOffers().toArray(new RoseItem[0]));
                                    pagination.update();
                                    createSortItems();
                                });
        addItem(filter, 48);

        RoseItem order =
                new RoseItem.Builder()
                        .material(Material.COMPARATOR)
                        .displayName(Chat.redFade("<b>Sort: " + sortOrder.name()))
                        .addLore("Klicke zum Umschalten zwischen hoch-niedrig ↔ niedrig-hoch")
                        .build()
                        .onClick(
                                e -> {
                                    // FIX: toggle sort order instead of toggling filter
                                    sortOrder = (sortOrder == SortOrder.ASC) ? SortOrder.DESC : SortOrder.ASC;
                                    pagination.clearAllItems();
                                    pagination.addItem(calculateOffers().toArray(new RoseItem[0]));
                                    pagination.update();
                                    createSortItems();
                                });
        addItem(order, 50);
    }

    /** PUBLIC rows with filter applied (but not sorted). */
    private Stream<OfferRow> publicRowsFiltered() {
        List<HasRealEstateAgent> offers =
                OFFER_CACHE.getOrDefault(agentUUID, new CachedSupplier<>(List::of, 20)).get();

        Stream<RealEstateAgent> stream = offers.stream().map(HasRealEstateAgent::getAgent);

        stream =
                switch (filterMode) {
                    case BUY -> stream.filter(RealEstateAgent::isForBuy);
                    case RENT -> stream.filter(RealEstateAgent::isForRent);
                    case PRIVATE -> Stream.empty(); // no public items when PRIVATE filter is active
                    case ALL -> stream;
                };

        return stream.map(a -> new OfferRow(a, false));
    }

    /** PRIVATE rows with filter applied (but not sorted). */
    private Stream<OfferRow> privateRowsFiltered() {
        Stream<RealEstateAgent> stream = privateOffers.stream();

        stream =
                switch (filterMode) {
                    case BUY -> stream.filter(RealEstateAgent::isForBuy);
                    case RENT -> stream.filter(RealEstateAgent::isForRent);
                    case PRIVATE -> stream; // only private, no extra filter
                    case ALL -> stream;
                };

        return stream.map(a -> new OfferRow(a, true));
    }

    /** Private offers must always be in front of public ones, then sort by price. */
    private List<OfferRow> getFilteredAndSortedRows() {
        Stream<OfferRow> combined =
                (filterMode == FilterMode.PRIVATE)
                        ? privateRowsFiltered()
                        : Stream.concat(privateRowsFiltered(), publicRowsFiltered());

        // Primary key: isPrivate (private first), Secondary: price (ASC/DESC)
        Comparator<OfferRow> cmpPrivateFirst =
                Comparator.comparingInt((OfferRow r) -> r.isPrivate() ? 0 : 1);

        Comparator<OfferRow> cmpPriceAsc =
                Comparator.comparingInt(r -> getRelevantPrice(r.agent()));
        Comparator<OfferRow> cmpPrice =
                (sortOrder == SortOrder.ASC) ? cmpPriceAsc : cmpPriceAsc.reversed();

        return combined.sorted(cmpPrivateFirst.thenComparing(cmpPrice)).toList();
    }

    private List<RoseItem> calculateOffers() {
        return getFilteredAndSortedRows().stream()
                .map(
                        row -> {
                            RealEstateAgent agent = row.agent();
                            boolean isPrivate = row.isPrivate();

                            Region region = agent.getRegion();

                            Material signMaterial = isPrivate ? Material.DARK_OAK_SIGN : Material.ACACIA_SIGN;
                            Component name = Chat.greenFade("<bold>" + region.getType() + " - " + region.getName());

                            RoseItem.Builder builder =
                                    new RoseItem.Builder().material(signMaterial).displayName(name);

                            builder.addLore(Chat.blueFade("Location: " + Chat.prettyLocation(region.getRegionCenter())));


                            if (!isPrivate) builder.addLore("Buy: " + agent.getBuyPrice());
                            if (!isPrivate) builder.addLore("Rent: " + agent.getRentPrice() + " / 14 Tage");
                            if (isPrivate) builder.addLore(agent.offeredType + ": " + agent.getOfferAmount());

                            return builder
                                    .build()
                                    .onClick(e -> new RealEstateBuyGUI(player, agent, isPrivate).open());
                        })
                .collect(Collectors.toList());
    }

    /** Price key selection: RENT mode uses rent price; otherwise buy price. */
    private int getRelevantPrice(RealEstateAgent agent) {
        if (filterMode == FilterMode.RENT) {
            return agent.isForRent() ? agent.getRentPrice() : Integer.MAX_VALUE;
        }
        // For ALL/BUY/PRIVATE, sort by buy price if available
        return agent.isForBuy() ? agent.getBuyPrice() : Integer.MAX_VALUE;
    }

    private enum FilterMode {
        ALL,
        BUY,
        RENT,
        PRIVATE
    }

    private enum SortOrder {
        ASC,
        DESC
    }

    @Override
    public void onClose(InventoryCloseEvent event) {}

    public static void invalidateOffers(UUID agentId) {
        OFFER_CACHE.remove(agentId);
    }

}
