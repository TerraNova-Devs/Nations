package de.terranova.nations.gui;

import de.terranova.nations.command.commands.CachedSupplier;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.realEstate.HasRealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateMarketCache;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import de.terranova.nations.utils.roseGUI.RosePagination;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    // Default cache time: 20s (region-specific) or 5min (global)
    OFFER_CACHE.computeIfAbsent(
        agentUUID,
        id -> {
          if (id.equals(GLOBAL_UUID)) {
            // Flatten all regions into one list
            return new CachedSupplier<>(RealEstateMarketCache::getAllListings, 60 * 5); // 5 minutes
          } else {
            return new CachedSupplier<>(() -> RealEstateMarketCache.getListing(id), 20);
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
            .addLore("Klicke zum Umschalten zwischen hoch-niedrig → niedrig-hoch")
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
    addItem(order, 50);
  }

  private List<RoseItem> calculateOffers() {
    return getFilteredAndSortedOffers().stream()
        .map(
            offer -> {
              Region region = offer.getAgent().getRegion();
              RealEstateAgent agent = offer.getAgent();
              boolean isPrivate = region == null;

              Material signMaterial = isPrivate ? Material.DARK_OAK_SIGN : Material.ACACIA_SIGN;
              String name =
                  isPrivate ? "Privates Angebot" : (region.getType() + " - " + region.getName());

              RoseItem.Builder builder =
                  new RoseItem.Builder().material(signMaterial).displayName(name);

              if (!isPrivate) {
                builder.addLore(
                    Chat.blueFade("Location: " + Chat.prettyLocation(region.getRegionCenter())));
              }

              if (agent.isForBuy()) builder.addLore("Buy: " + agent.getBuyPrice());

              if (agent.isForRent())
                builder.addLore("Rent: " + agent.getRentPrice() + " / 14 Tage");

              return builder
                  .build()
                  .onClick(e -> new RealEstateBuyGUI(player, agent, false).open());
            })
        .collect(Collectors.toList());
  }

  private List<HasRealEstateAgent> getFilteredAndSortedOffers() {
    List<HasRealEstateAgent> offers =
        OFFER_CACHE.getOrDefault(agentUUID, new CachedSupplier<>(List::of, 20)).get();

    // Add private offers as anonymous HasRealEstateAgent implementations
    List<HasRealEstateAgent> privateWrapped =
        privateOffers.stream()
            .<HasRealEstateAgent>map(
                agent ->
                    new HasRealEstateAgent() {
                      @Override
                      public RealEstateAgent getAgent() {
                        return agent;
                      }
                    })
            .toList();

    Stream<HasRealEstateAgent> stream;

    switch (filterMode) {
      case BUY -> stream = offers.stream().filter(o -> o.getAgent().isForBuy());
      case RENT -> stream = offers.stream().filter(o -> o.getAgent().isForRent());
      case PRIVATE -> stream = privateWrapped.stream();
      case ALL -> stream = Stream.concat(privateWrapped.stream(), offers.stream());
      default -> stream = offers.stream();
    }

    return stream
        .sorted(
            (a, b) -> {
              int priceA = getRelevantPrice(a);
              int priceB = getRelevantPrice(b);
              return sortOrder == SortOrder.ASC
                  ? Integer.compare(priceA, priceB)
                  : Integer.compare(priceB, priceA);
            })
        .toList();
  }

  private int getRelevantPrice(HasRealEstateAgent offer) {
    boolean isBuy = filterMode != FilterMode.RENT;
    boolean isRelevant = isBuy ? offer.getAgent().isForBuy() : offer.getAgent().isForRent();
    return isRelevant ? offer.getAgent().getBuyPrice() : Integer.MAX_VALUE;
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
}
