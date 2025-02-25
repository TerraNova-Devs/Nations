package de.terranova.nations.gui.nations;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Optional;
import java.util.UUID;

public class NationSettlementsGUI extends RoseGUI {
    private final Nation nation;

    public NationSettlementsGUI(Player player, Nation nation) {
        super(player, "nation-settlements-gui", Chat.blueFade("<b>Nation Settlements"), 5);
        this.nation = nation;;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build());

        int slot = 0;
        for (UUID settlementId : nation.getSettlements()) {
            Optional<SettleRegion> settlement = RegionManager.retrieveRegion("settle", settlementId);

            if (settlement.isPresent()) {
                RoseItem settlementItem = new RoseItem.Builder()
                        .material(Material.OAK_SIGN)
                        .displayName(settlement.get().getName())
                        .addLore("Level: " + settlement.get().getRank().getLevel())
                        .build();

                addItem(slot++, settlementItem);
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // No special action needed on close
    }
}
