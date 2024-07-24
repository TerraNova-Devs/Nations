package de.terranova.paperweight.nations;

import de.terranova.paperweight.nations.settlements.settlementManager;
import de.terranova.paperweight.nations.commands.settle;
import de.terranova.paperweight.nations.database.HikariCP;
import de.terranova.paperweight.nations.listener.NpcInteractListener;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class NationsPlugin extends JavaPlugin {


    //WAS PASSIER WENN 2 CLAIMS SICH ÜBERLAPPEN?
    //NPC HOLOGRAM

    public de.terranova.paperweight.nations.settlements.settlementManager settlementManager = new settlementManager();
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
        Objects.requireNonNull(getCommand("settle")).setExecutor(new settle(this));
    }

    public void listenerRegistry() {
        Bukkit.getPluginManager().registerEvents(new NpcInteractListener(), this);
    }

    public void serilizationRegistry() {

    }


    @Override
    public void onDisable() {

    }
}
