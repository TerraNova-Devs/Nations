package de.terranova.nations;

import de.terranova.nations.commands.settle;
import de.terranova.nations.database.HikariCP;
import de.terranova.nations.listener.NpcInteractListener;
import de.terranova.nations.settlements.settlementManager;
import de.terranova.nations.utils.YMLHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class NationsPlugin extends JavaPlugin {

    public settlementManager settlementManager = new settlementManager();

    //WAS PASSIER WENN 2 CLAIMS SICH ÜBERLAPPEN?
    //NPC HOLOGRAM
    YMLHandler skins;
    HikariCP hikari;
    Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        saveDefaultConfig();
        try {
            skins = new YMLHandler("skins.yml", this.getDataFolder());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            commands.register("settle", "Command facilitates settlements creation.", List.of("s"), new settle(this));
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
        skins.unloadYAML();
    }
}






