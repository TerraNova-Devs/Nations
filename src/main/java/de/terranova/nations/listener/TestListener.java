package de.terranova.nations.listener;

import com.sk89q.worldedit.IncompleteRegionException;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class TestListener implements Listener {

    @EventHandler
    private void onBlockBreak(PlayerInteractEvent e) throws IncompleteRegionException {
        if (!e.getAction().isRightClick()) return;
        if (!e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BEDROCK)) {
        }
        //e.getPlayer().sendMessage(Chat.errorFade("" + BoundaryClaimFunctions.isValidSelection(e.getPlayer())));
    }

}
