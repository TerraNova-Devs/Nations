package org.nations;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.nations.database.HikariCP;
import org.nations.listener.BlockPlaceListener;

import java.util.logging.Logger;

public final class Nations extends JavaPlugin {

    HikariCP hikari;
    Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        saveDefaultConfig();

        /*
        try {
            hikari = new HikariCP(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
         */

        listenerRegistry();
    }

    public void listenerRegistry() {
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this), this);
    }

    @Override
    public void onDisable() {

    }
}
