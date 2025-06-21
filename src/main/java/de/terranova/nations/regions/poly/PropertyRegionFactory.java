package de.terranova.nations.regions.poly;

import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionFactory;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PropertyRegionFactory implements RegionFactory {
    @Override
    public Region create(String name, Player p) {
        return new PropertyRegion(name, UUID.randomUUID(),p.getUniqueId());
    }

    @Override
    public Region retrieve(String name, UUID ruuid) {
        return new PropertyRegion(name,ruuid);
    }
}
