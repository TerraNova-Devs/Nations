package org.nations.listener;

import com.jeff_media.customblockdata.CustomBlockData;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nations.Nations;

import java.util.ArrayList;
import java.util.List;

public class BlockPlaceListener implements Listener {

    private final Nations plugin;

    private final NamespacedKey key;


    //SELTENHEITEN NUR SPORADISCHE LÖSUNG MAP jedes items mit Seltenheit wäre besser customizable

    List<Material> common = new ArrayList<>();
    List<Material> uncommon = new ArrayList<>();
    List<Material> rare = new ArrayList<>();

    public BlockPlaceListener(Nations plugin) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "wasPlaced");
        for (String s : (List<String>) plugin.getConfig().getList("common")) {
            common.add(Material.valueOf(s));
        }
        for (String s : (List<String>) plugin.getConfig().getList("uncommon")) {
            uncommon.add(Material.valueOf(s));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if(common.contains(event.getBlock().getType()) || uncommon.contains(event.getBlock().getType())){
            PersistentDataContainer customBlockData = new CustomBlockData(event.getBlockPlaced(), plugin);
            customBlockData.set(key, PersistentDataType.BOOLEAN, true);
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
