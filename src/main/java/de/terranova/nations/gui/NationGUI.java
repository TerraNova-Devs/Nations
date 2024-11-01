package de.terranova.nations.gui;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import java.util.UUID;

public class NationGUI extends RoseGUI {
    public NationGUI(Player player) {
        super(player, "nation-gui", Chat.blueFade("<b>Nation Menu"), 5);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        // Find the player's nation
        Nation nation = null;
        for (Nation n : NationsPlugin.nationManager.getNations().values()) {
            if (n.getMembers().contains(player.getUniqueId())) {
                nation = n;
                break;
            }
        }

        if (nation == null) {
            player.sendMessage("You are not part of a nation.");
            return;
        }

        // Create GUI items
        RoseItem nationInfo = new RoseItem.Builder()
                .material(Material.BOOK)
                .displayName("Nation Info")
                .addLore("Name: " + nation.getName())
                .addLore("Leader: " + Bukkit.getOfflinePlayer(nation.getLeader()).getName())
                .build();

        RoseItem membersItem = new RoseItem.Builder()
                .material(Material.PLAYER_HEAD)
                .displayName("Members")
                .addLore("Click to view members")
                .build();

        RoseItem relationsItem = new RoseItem.Builder()
                .material(Material.PAPER)
                .displayName("Relations")
                .addLore("Click to view relations")
                .build();

        // Add items to the GUI
        addItem(13, nationInfo);
        addItem(21, membersItem);
        addItem(23, relationsItem);

        // Set up click events
        membersItem.onClick(e -> new NationMembersGUI(player, nation).open());
        relationsItem.onClick(e -> new NationRelationsGUI(player, nation).open());
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // Handle GUI closure if needed
    }
}
