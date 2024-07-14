package org.nations.listener;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nations.gui.TownGUI;

public class NpcInteractListener implements Listener {

    @EventHandler
    public void click(NPCRightClickEvent event) {
        if (!event.getNPC().getEntity().hasMetadata("NPC")) return;
        Player player = event.getClicker();
        player.sendMessage(event.getNPC().getFullName());
        new TownGUI(player).open();



    }
}
