package de.terranova.nations.regions.base;

import de.terranova.nations.regions.rule.RuleSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegionRegistry {
    public static final Map<String, RegionFactoryBase> factories = new HashMap<>();
    private static final Map<String, RuleSet> ruleSets = new HashMap<>();

    public static void register(RegionFactoryBase factory, RuleSet ruleSet) {
        factories.put(factory.getType(), factory);
        ruleSets.put(factory.getType(), ruleSet);
    }

    public static RegionFactoryBase getFactory(String type) {
        return factories.get(type);
    }

    public static RuleSet getRuleSet(String type) {
        return ruleSets.getOrDefault(type, RuleSet.defaultRules());
    }

    public static Region createFromArgs(String type, List<String> args) {
        return getFactory(type).createFromArgs(args);
    }

    public static Optional<Region> createWithContext(String type, RegionContext ctx) {
        if (factories.containsKey(type)) {
            Region region = getFactory(type).createWithContext(ctx);
            if (region != null) {
                region.onCreation(ctx.player);
                region.dataBaseCall();
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }
}