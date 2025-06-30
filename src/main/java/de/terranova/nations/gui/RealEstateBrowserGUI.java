package de.terranova.nations.gui;

import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.realEstate.CanBeSold;
import de.terranova.nations.regions.modules.realEstate.RealEstateManager;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import de.terranova.nations.utils.roseGUI.RosePagination;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RealEstateBrowserGUI extends RoseGUI {

    private final RosePagination pagination = new RosePagination(this);
    private final UUID agentUUID;

    public RealEstateBrowserGUI(@NotNull Player player, Region agentRegion) {
        super(player, "realestate-browser", Chat.blueFade(String.format("RealEstate %s - Page: %s",agentRegion.getName())), 6);
        this.agentUUID = agentRegion.getId();
        pagination.registerPageSlotsBetween(10, 16);
        pagination.registerPageSlotsBetween(19, 25);
        pagination.registerPageSlotsBetween(28, 34);
        pagination.registerPageSlotsBetween(37, 43);
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
                .build();
        addItem(last, 45);

        RoseItem filter = new RoseItem.Builder()
                .material(Material.HOPPER)
                .displayName(Chat.redFade("<b>Menü verlassen"))
                .build().onClick(e -> pagination.goLastPage());
        addItem(filter, 49);

        List<RoseItem> offerItems = RealEstateManager.getRealestate(agentUUID).stream()
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
                .toList();
        pagination.addItem(offerItems.toArray(new RoseItem[0]));

    }
}
