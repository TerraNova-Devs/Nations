package de.terranova.nations.regions.rule;

import de.terranova.nations.regions.base.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class RuleContext {
    public final String type;
    public final Player player;
    public final Location location;
    public final Region parent;
    public final List<Region> nearbyRegions;
    public final Region fakeRegionBeingPlaced;

    public RuleContext(String type, Player player, Location location,
                       Region parent, List<Region> nearbyRegions,
                       Region fakeRegionBeingPlaced) {
        this.type = type;
        this.player = player;
        this.location = location;
        this.parent = parent;
        this.nearbyRegions = nearbyRegions;
        this.fakeRegionBeingPlaced = fakeRegionBeingPlaced;
    }
}
