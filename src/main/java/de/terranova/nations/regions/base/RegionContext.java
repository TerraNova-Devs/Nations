package de.terranova.nations.regions.base;

import org.bukkit.entity.Player;

import java.util.Map;

public class RegionContext {

    public final Player player;
    public final Map<String, String> extra;

    public RegionContext(Player player, Map<String, String> extra) {
        this.player = player;
        this.extra = extra;
    }
}
