package de.terranova.nations.gui;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.nations.nations.Nation;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import java.util.UUID;

public class NationMembersGUI extends RoseGUI {
    private final de.terranova.nations.nations.Nation nation;

    public NationMembersGUI(Player player, Nation nation) {
        super(player, "nation-members-gui", "Nation Members", 5);
        this.nation = nation;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        // Display nation members
        int slot = 0;
        for (UUID memberId : nation.getMembers()) {
            RoseItem memberItem = new RoseItem.Builder()
                    .playerHead(memberId)
                    .displayName(Bukkit.getOfflinePlayer(memberId).getName())
                    .build();
            addItem(slot++, memberItem);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // Handle GUI closure if needed
    }
}
