package de.terranova.nations.commands;

import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerraSelectCache {
    public static Map<UUID, TerraSelectCache> selectCache = new HashMap<>();

    private SettleRegionType settle;
    private AccessLevel access;
    TerraSelectCache(SettleRegionType settle, AccessLevel access) {
        this.settle = settle;
        this.access = access;
    }

    public SettleRegionType getSettle() {
        return settle;
    }

    public AccessLevel getAccess() {
        return access;
    }
}
