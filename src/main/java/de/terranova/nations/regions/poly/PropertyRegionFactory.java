package de.terranova.nations.regions.poly;

import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionFactory;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PropertyRegionFactory implements RegionFactory {

    @Override
    public String getType() {
        return PropertyRegion.REGION_TYPE;
    }

    @Override
    public Region createWithContext(RegionContext ctx) {
        return new PropertyRegion(ctx.extra.get("name"), UUID.randomUUID(),ctx.player.getUniqueId());
    }

    @Override
    public Region createFromArgs(List<String> args) {
        return new PropertyRegion(
                args.getFirst(),
                UUID.fromString(args.get(1)),
                UUID.fromString(args.get(2))
        );
    }
}
