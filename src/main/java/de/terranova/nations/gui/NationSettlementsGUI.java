package de.terranova.nations.gui;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.settlements.Settle;
import de.terranova.nations.settlements.SettleManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.UUID;

public class NationSettlementsGUI extends RoseGUI {
    private final Nation nation;
    private final SettleManager settleManager;

    public NationSettlementsGUI(Player player, Nation nation) {
        super(player, "nation-settlements-gui", Chat.blueFade("<b>Nation Settlements"), 5);
        this.nation = nation;
        this.settleManager = NationsPlugin.settleManager;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build());

        int slot = 0;
        for (UUID settlementId : nation.getSettlements()) {
            Settle settlement = settleManager.getSettle(settlementId).orElse(null);
            if (settlement != null) {
                RoseItem settlementItem = new RoseItem.Builder()
                        .material(Material.OAK_SIGN)
                        .displayName(settlement.name)
                        .addLore("Level: " + settlement.level)
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
