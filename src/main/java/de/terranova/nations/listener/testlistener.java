package de.terranova.nations.listener;


import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class testlistener implements Listener {

    @EventHandler
    public void d(VillagerAcquireTradeEvent e) {
        AbstractVillager v = e.getEntity();

    }

}
