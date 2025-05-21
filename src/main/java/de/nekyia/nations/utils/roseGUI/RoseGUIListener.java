package de.nekyia.nations.utils.roseGUI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.sql.SQLException;

public class RoseGUIListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(final InventoryOpenEvent event) throws SQLException {
        if (!(event.getPlayer() instanceof Player player)) return;

        final RoseGUI openGui = RoseGUI.players.get(player.getUniqueId());
        if (openGui == null) return;
        if (event.isCancelled()) return;
        openGui.onOpen(event);
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        final RoseGUI openGui = RoseGUI.players.get(player.getUniqueId());
        if (openGui == null) return;
        if (!event.getInventory().equals(openGui.getInventory())) return;

        openGui.onClose(event);
        openGui.setClosed(true);
        RoseGUI.players.remove(player.getUniqueId());
    }

    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        final RoseGUI openGui = RoseGUI.players.get(event.getWhoClicked().getUniqueId());
        if (openGui == null) return;

        final boolean doNotProtect = openGui.onClick(event);
        final int index = event.getRawSlot();

        if (!doNotProtect) {
            //default click
            if (event.getSlot() == index) {
                event.setCancelled(true);
            } else {
                switch (event.getAction()) {
                    case MOVE_TO_OTHER_INVENTORY:
                        //SHIFT CLICK etc.
                    case COLLECT_TO_CURSOR:
                        //DOUBLE CLICK WITH CURSOR
                    case UNKNOWN:
                        //SOMETIMES HACKED CLIENT CLICK etc.
                        event.setCancelled(true);
                }
            }
        } else {
            event.setCancelled(false);
        }

        final RoseItem item = openGui.getItems().get(index);
        if (item == null) return;

        item.getClickAction().accept(event);
    }

    @EventHandler
    public void onDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        final RoseGUI openGui = RoseGUI.players.get(player.getUniqueId());
        if (openGui == null) return;

        event.setCancelled(!openGui.onDrag(event));
        for (int index : event.getRawSlots()) {
            final RoseItem item = openGui.getItems().get(index);

            if (item == null) return;
            item.getDragAction().accept(event);
        }
    }

}