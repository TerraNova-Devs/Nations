package de.terranova.nations.gui;

import de.terranova.nations.command.commands.CachedSupplier;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.realEstate.CanBeSold;
import de.terranova.nations.regions.modules.realEstate.RealEstateManager;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import de.terranova.nations.utils.roseGUI.RosePagination;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RealEstateBrowserGUI extends RoseGUI {

    private static final Map<UUID, CachedSupplier<List<CanBeSold>>> OFFER_CACHE = new ConcurrentHashMap<>();
    private final RosePagination pagination = new RosePagination(this);
    private final UUID agentUUID;
    private FilterMode filterMode = FilterMode.ALL;
    private SortOrder sortOrder = SortOrder.ASC;

    public RealEstateBrowserGUI(@NotNull Player player, Region agentRegion) {
        super(player, "realestate-browser", Chat.blueFade(String.format("RealEstate %s", agentRegion.getName())), 6);
        this.agentUUID = agentRegion.getId();
        pagination.registerPageSlotsBetween(10, 16);
        pagination.registerPageSlotsBetween(19, 25);
        pagination.registerPageSlotsBetween(28, 34);
        pagination.registerPageSlotsBetween(37, 43);

        OFFER_CACHE.computeIfAbsent(agentUUID, id ->
                new CachedSupplier<>(() -> RealEstateManager.getRealestate(id), 180_000));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();
        fillGui(filler);

        RoseItem next = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.redFade("<b>Nächste Seite"))
                .build()
                .onClick(e -> pagination.goNextPage());
        addItem(next, 53);

        RoseItem last = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.redFade("<b>Vorherige Seite"))
                .build()
                .onClick(e -> pagination.goPreviousPage());
        addItem(last, 45);

        RoseItem filter = new RoseItem.Builder()
                .material(Material.HOPPER)
                .displayName(Chat.redFade("<b>Filter: ") + filterMode.name())
                .addLore("§7Klicke zum Wechseln: ALL → BUY → RENT")
                .build()
                .onClick(e -> {
                    filterMode = switch (filterMode) {
                        case ALL -> FilterMode.BUY;
                        case BUY -> FilterMode.RENT;
                        case RENT -> FilterMode.ALL;
                    };
                    onOpen(null); // Refresh GUI
                });
        addItem(filter, 48);

        RoseItem order = new RoseItem.Builder()
                .material(Material.COMPARATOR)
                .displayName(Chat.redFade("<b>Sort: ") + sortOrder.name())
                .addLore("§7Klicke zum Umschalten zwischen ASC/DESC")
                .build()
                .onClick(e -> {
                    sortOrder = (sortOrder == SortOrder.ASC) ? SortOrder.DESC : SortOrder.ASC;
                    onOpen(null); // Refresh GUI
                });
        addItem(order, 50);

        // Create and display filtered, sorted, cached items
        List<RoseItem> offerItems = getFilteredAndSortedOffers().stream()
                .map(offer -> {
                    Region region = (Region) offer;
                    return new RoseItem.Builder()
                            .material(Material.ACACIA_SIGN)
                            .displayName(region.getType() + " - " + region.getName())
                            .addLore(Chat.blueFade("Location: ") + Chat.prettyLocation(RegionClaimFunctions.getRegionCenterAsLocation(region.getWorldguardRegion())))
                            .addLore(Chat.blueFade("Buy: ") + (offer.getAgent().isForBuy() ? offer.getAgent().getBuyPrice() + " Silber" : "No buy option"))
                            .addLore(Chat.blueFade("Rent: ") + (offer.getAgent().isForRent() ? offer.getAgent().getBuyPrice() + " Silber / 7 Days" : "No rent option"))
                            .build();
                })
                .collect(Collectors.toList());

        pagination.addItem(offerItems.toArray(new RoseItem[0]));
    }

    private List<CanBeSold> getFilteredAndSortedOffers() {
        List<CanBeSold> offers = OFFER_CACHE.getOrDefault(agentUUID,
                new CachedSupplier<>(() -> List.of(), 180_000)).get();

        return offers.stream()
                .filter(offer -> switch (filterMode) {
                    case BUY -> offer.getAgent().isForBuy();
                    case RENT -> offer.getAgent().isForRent();
                    case ALL -> true;
                })
                .sorted((a, b) -> {
                    int priceA = getRelevantPrice(a);
                    int priceB = getRelevantPrice(b);
                    return sortOrder == SortOrder.ASC
                            ? Integer.compare(priceA, priceB)
                            : Integer.compare(priceB, priceA);
                })
                .toList();
    }

    private int getRelevantPrice(CanBeSold offer) {
        boolean isBuy = filterMode != FilterMode.RENT;
        boolean isRelevant = isBuy ? offer.getAgent().isForBuy() : offer.getAgent().isForRent();
        return isRelevant ? offer.getAgent().getBuyPrice() : Integer.MAX_VALUE;
    }

    private enum FilterMode {ALL, BUY, RENT}

    private enum SortOrder {ASC, DESC}

    @Override
    public void onClose(InventoryCloseEvent event) {

    }
}

