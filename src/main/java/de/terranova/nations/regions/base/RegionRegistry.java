package de.terranova.nations.regions.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegionRegistry {
    public static final Map<String, RegionFactory> registry = new HashMap<>();

    public static void register(RegionFactory factory) {
        registry.put(factory.getType(), factory);
    }

    public static RegionFactory getFactory(String type) {
        return registry.get(type);
    }

    public static Region createFromArgs(String type, List<String> args) {

        return getFactory(type).createFromArgs(args);
    }

    public static Optional<Region> createWithContext(String type, RegionContext ctx) {
        if (registry.containsKey(type)) {
            Region region = getFactory(type).createWithContext(ctx);
            if(region != null) {
                region.onCreation(ctx.player);
                region.dataBaseCall();
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }
}
