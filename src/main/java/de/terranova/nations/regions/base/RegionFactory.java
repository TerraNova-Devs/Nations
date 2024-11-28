package de.terranova.nations.regions.base;

import org.bukkit.entity.Player;

public interface RegionFactory {
    RegionType create(String name, Player p);
}
