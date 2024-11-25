package de.terranova.nations.commands;

import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.RegionType;
import de.terranova.nations.settlements.RegionTypes.Access;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerraSelectCache {
    public static Map<UUID, TerraSelectCache> selectCache = new HashMap<>();

    private RegionType region;
    private AccessLevel access;
    public TerraSelectCache(RegionType region, UUID uuid) {
        this.region = region;
        if(region instanceof Access access){
            this.access = access.getAccess(uuid);
        } else {
            this.access = null;
        }
    }

    public RegionType getRegion() {
        return region;
    }

    public AccessLevel getAccess() {
        return access;
    }
}
