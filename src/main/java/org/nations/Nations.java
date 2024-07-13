package org.nations;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.nations.database.HikariCP;
import org.nations.listener.BlockPlaceListener;

import java.sql.SQLException;

public final class Nations extends JavaPlugin {

    HikariCP hikari;

    @Override
    public void onEnable() {
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
