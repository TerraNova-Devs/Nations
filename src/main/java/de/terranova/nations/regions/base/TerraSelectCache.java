package de.terranova.nations.regions.base;

import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.access.AccessControlled;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerraSelectCache {
    public static Map<UUID, TerraSelectCache> selectCache = new HashMap<>();

    private RegionType region;
    private AccessLevel access;
    public TerraSelectCache(RegionType region, UUID uuid) {
        this.region = region;
        if(region instanceof AccessControlled access){
            this.access = access.getAccess().getAccessLevel(uuid);
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
