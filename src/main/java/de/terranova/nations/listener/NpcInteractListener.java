package de.terranova.nations.listener;

import de.terranova.nations.gui.TownGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NpcInteractListener implements Listener {


  @EventHandler
  public void click(NPCRightClickEvent event) {
    if (!event.getNPC().getEntity().hasMetadata("NPC")) return;
    Player player = event.getClicker();
    new TownGUI(player).open();
  }

}
