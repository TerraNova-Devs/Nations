package de.terranova.nations.regions.rule;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.base.Region;
import org.bukkit.entity.Player;

import java.util.Set;

public interface RegionRule {
    boolean isAllowed(Player p,Class<? extends Region> regionClass,String regionName, ProtectedRegion regionBeingPlaced, Region explicitParent);
    String getErrorMessage();
}
