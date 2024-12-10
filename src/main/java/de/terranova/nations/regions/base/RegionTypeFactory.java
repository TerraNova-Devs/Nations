package de.terranova.nations.regions.base;

import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface RegionTypeFactory {
    RegionType create(String name, Player p);
    RegionType retrieve(String name, UUID ruuid, Vectore2 loc);
}
