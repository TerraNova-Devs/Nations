package de.terranova.nations.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
//TODO get regions from DB
public class RegionManager {
    // Map structure: Type -> (UUID -> RegionType)
    private final static Map<String, Map<UUID, ? extends RegionType>> regionCache = new HashMap<>();

    public static <T extends RegionType> void cacheRegions(String type, Map<UUID, T> regions) {
        regionCache.put(type, regions);
    }

    @SuppressWarnings("unchecked")
    public static <T extends RegionType> Map<UUID, T> retrieveAllCachedTypes(String type) {
        return (Map<UUID, T>) regionCache.get(type);
    }

    public static <T extends RegionType> void addRegion(String type, UUID uuid, T region) {
        // Retrieve the map for the specified type, or create a new one if it doesn't exist
        Map<UUID, T> regions = retrieveAllCachedTypes(type);
        if (regions == null) {
            regions = new HashMap<>();
            cacheRegions(type, regions);
        }
        regions.put(uuid, region);
    }

    public static <T extends RegionType> boolean removeRegion(String type, UUID uuid) {
        // Retrieve the map for the specified type
        Map<UUID, T> regions = retrieveAllCachedTypes(type);
        if (regions == null) {
            return false; // Type not found
        }
        return regions.remove(uuid) != null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends RegionType> Optional<T> retrieveRegion(String type, UUID uuid) {
        Map<UUID, T> typeMap = (Map<UUID, T>) regionCache.get(type);
        if (typeMap == null) {
            return Optional.empty(); // Type not found
        }
        if(typeMap.containsKey(uuid)) {
            return Optional.of(typeMap.get(uuid)); // Retrieve region by name
        }
        return Optional.empty();
    }

    public static <T extends RegionType> Optional<T> retrieveRegion(String type, String name) {
        Map<UUID, T> regions = retrieveAllCachedTypes(type);
        for (T region : regions.values()) {
            if(name.equalsIgnoreCase(region.getName())) return Optional.of(region);
        }
        return Optional.empty();
    }

    public static <T extends RegionType> Optional<T> retrieveRegion(String type, Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        Map<UUID, T> regions = retrieveAllCachedTypes(type);

        for (ProtectedRegion region : set) {
            for (RegionType regionType : regions.values()) {
                String regionUUIDString = region.getFlag(RegionFlag.REGION_UUID_FLAG);
                if(regionUUIDString == null) continue;
                if(regionUUIDString.equals("00000000-0000-0000-0000-000000000000")) continue;
                UUID regionUUID = UUID.fromString(regionUUIDString);
                if(regionType.getId().equals(regionUUID)) {
                    return Optional.of(regions.get(regionUUID));
                }
            }
        }
        return Optional.empty();
    }
}
