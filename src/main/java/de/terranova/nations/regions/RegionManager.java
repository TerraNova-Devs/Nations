package de.terranova.nations.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionRegistry;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import org.bukkit.Location;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static de.terranova.nations.worldguard.NationsRegionFlag.TypeFlag.NATIONS_TYPE;

public class RegionManager {

  // Typesafe heterogeneous container: Class<T> -> Map<UUID, T>
  // The cast inside accessors is safe because all writes go through addRegion(Class<T>, ...),
  // which guarantees the inner map's element type matches the key.
  private static final Map<Class<? extends Region>, Map<UUID, ? extends Region>> regionCache =
          new ConcurrentHashMap<>();

  // ---------- Core accessors ----------

  /**
   * Returns the cached regions for the given type, or an empty (immutable) map if none are cached.
   * Never returns null. The returned map for a populated type is live — iteration is
   * weakly consistent under concurrent modification.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Region> Map<UUID, T> retrieveAllCachedRegions(Class<T> type) {
    Map<UUID, ? extends Region> map = regionCache.get(type);
    return map == null ? Collections.emptyMap() : (Map<UUID, T>) map;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Region> void addRegion(Class<T> type, UUID uuid, T region) {
    Map<UUID, T> regions =
            (Map<UUID, T>) regionCache.computeIfAbsent(type, k -> new ConcurrentHashMap<UUID, T>());
    regions.put(uuid, region);
  }

  public static <T extends Region> void addAllRegions(Class<T> type, Map<UUID, ? extends T> regions) {
    for (Map.Entry<UUID, ? extends T> entry : regions.entrySet()) {
      addRegion(type, entry.getKey(), entry.getValue());
    }
  }

  public static <T extends Region> boolean removeRegion(Class<T> type, UUID uuid) {
    return retrieveAllCachedRegions(type).remove(uuid) != null;
  }

  // ---------- Lookups ----------

  public static <T extends Region> Optional<T> retrieveRegion(Class<T> type, UUID uuid) {
    return Optional.ofNullable(retrieveAllCachedRegions(type).get(uuid));
  }

  public static <T extends Region> Optional<T> retrieveRegion(Class<T> type, String name) {
    return retrieveAllCachedRegions(type).values().stream()
            .filter(r -> name.equalsIgnoreCase(r.getName()))
            .findFirst();
  }

  public static <T extends Region> Optional<T> retrieveRegion(Class<T> type, Location location) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();
    ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
    Map<UUID, T> regions = retrieveAllCachedRegions(type);

    for (ProtectedRegion region : set) {
      UUID regionUUID = parseRegionUUID(region.getFlag(RegionFlag.REGION_UUID_FLAG));
      if (regionUUID == null) continue;
      T match = regions.get(regionUUID);
      if (match != null) return Optional.of(match);
    }
    return Optional.empty();
  }

  public static Optional<Region> retrieveRegion(ProtectedRegion region) {
    UUID regionUUID = parseRegionUUID(region.getFlag(RegionFlag.REGION_UUID_FLAG));
    String typeKey = region.getFlag(NATIONS_TYPE);
    if (regionUUID == null || typeKey == null) return Optional.empty();

    return RegionRegistry.getRegionClass(typeKey)
            .flatMap(cls -> retrieveRegion(cls, regionUUID))
            .map(r -> (Region) r);
  }

  /**
   * Registers a region under its runtime class. Used by Region's constructor so subclasses
   * don't need to pass their class explicitly. The cast is safe because obj.getClass()
   * returns the runtime type, which by definition matches T.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Region> void registerSelf(T region) {
    addRegion((Class<T>) region.getClass(), region.getId(), region);
  }

  // ---------- Convenience ----------

  public static Optional<SettleRegion> retrievePlayersSettlement(UUID player) {
    return retrieveAllCachedRegions(SettleRegion.class).values().stream()
            .filter(settle ->
                    Access.hasAccess(settle.getAccess().getAccessLevel(player), AccessLevel.CITIZEN))
            .findFirst();
  }

  // ---------- Internals ----------

  private static UUID parseRegionUUID(String raw) {
    if (raw == null) return null;
    try {
      UUID uuid = UUID.fromString(raw);
      return RegionFlag.NULL_UUID.equals(uuid) ? null : uuid;
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}