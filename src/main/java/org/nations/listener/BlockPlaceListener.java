package org.nations.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nations.Nations;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class BlockPlaceListener implements Listener {

    private final Nations plugin;

    private final NamespacedKey key;

    private final Material[] mats = {Material.DIAMOND_ORE,Material.IRON_ORE};

    public BlockPlaceListener(Nations plugin) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "wasPlaced");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        for(Material mat : mats) {
            if(mat.equals(event.getBlock().getType())) {
                PersistentDataContainer customBlockData = new CustomBlockData(event.getBlockPlaced(), plugin);
                customBlockData.set(key, PersistentDataType.BOOLEAN, true);
            }
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {


        PersistentDataContainer customBlockData = new CustomBlockData(event.getBlock(), plugin);
        if(customBlockData.has(key, PersistentDataType.BOOLEAN)) {
            if(Boolean.TRUE.equals(customBlockData.get(key, PersistentDataType.BOOLEAN))) {
                event.getPlayer().sendMessage("Hat funktioniert!");
            }

        }

    }

}
