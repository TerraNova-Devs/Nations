package org.nations.listener;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.ItemMeta;
import org.nations.gui.TownGUI;

public class NpcInteractListener implements Listener {

    @EventHandler
    public void click(NPCRightClickEvent event) {
        if (!event.getNPC().getEntity().hasMetadata("NPC")) return;
        Player player = event.getClicker();
        new TownGUI(player).open();


    }
}
