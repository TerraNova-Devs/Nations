package de.terranova.nations;

import com.mojang.brigadier.Command;
import de.terranova.nations.commands.settle;
import de.terranova.nations.database.HikariCP;
import de.terranova.nations.listener.NpcInteractListener;
import de.terranova.nations.settlements.settlementManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public final class NationsPlugin extends JavaPlugin {


    //WAS PASSIER WENN 2 CLAIMS SICH ÜBERLAPPEN?
    //NPC HOLOGRAM

    public settlementManager settlementManager = new settlementManager();
    HikariCP hikari;
    Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        saveDefaultConfig();
        new InventoryAPI(this).init();
        /*
        try {
            hikari = new HikariCP(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
         */
        commandRegistry();
        listenerRegistry();
        serilizationRegistry();


    }

    public void commandRegistry() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("settle", "Command facilitates settlements creation.",List.of("s"), new settle(this));
        });
    }
    //Objects.requireNonNull(getCommand("settle")).setExecutor(new settle(this));

    public void listenerRegistry() {
        Bukkit.getPluginManager().registerEvents(new NpcInteractListener(), this);
    }

    public void serilizationRegistry() {

    }

    @Override
    public void onDisable() {

    }
}






