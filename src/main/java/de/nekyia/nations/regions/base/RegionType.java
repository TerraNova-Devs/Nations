package de.nekyia.nations.regions.base;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.nekyia.nations.utils.Chat;
import de.nekyia.nations.worldguard.NationsRegionFlag.RegionFlag;
import de.nekyia.nations.worldguard.math.Vectore2;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class RegionType {


    // Registry for dynamically adding RegionType
    public static final Map<String, RegionTypeFactory> registry = new HashMap<>();
    protected final UUID id;
    protected final String type;
    private final RegionTypeEventBus eventBus = new RegionTypeEventBus();
    protected String name;
    protected ProtectedRegion region;
    private static final List<String> nameCache = new ArrayList<>();


    public RegionType(String name, UUID id, String type) {
        this.id = id;
        this.type = type.toLowerCase();
        if (name == null) return;
        this.name = name;
        de.nekyia.nations.regions.RegionManager.addRegion(getType(),getId(), this);
    }

    public static List<String> getNameCache() {
        return nameCache;
    }

    public static boolean isNameCached(String name) {
        return nameCache.contains(name.toLowerCase());
    }

    public void addNameToCache(String name) {
        nameCache.add(name.toLowerCase());
    }

    // Method to register new RegionType creators
    public static void registerRegionType(String type, RegionTypeFactory factory) {
        registry.put(type.toLowerCase(), factory);
    }

    // Method to deregister an RegionType
    public static void deregisterRegionType(String type) {
        registry.remove(type.toLowerCase());
    }

    // Factory method to create RegionTypes
    public static Optional<RegionType> createRegionType(String type, String name, Player p) {
        RegionTypeFactory factory = registry.get(type.toLowerCase());
        if (factory == null) {
            p.sendMessage(Chat.errorFade("No such region type registered: " + type));
            return Optional.empty();
        }

        RegionType regionType = factory.create(name, p);
        if (regionType == null) {
            return Optional.empty();  // Creation failed due to validation issues.
        }
        regionType.onCreation(p);
        regionType.dataBaseCall();
        return Optional.of(regionType);
    }

    // Factory method to retrieve RegionTypes
    public static Optional<RegionType> retrieveRegionType(String type, String name, UUID ruuid, Vectore2 loc) {
        RegionTypeFactory factory = registry.get(type.toLowerCase());
        if (factory == null) {
            return Optional.empty();
        }

        RegionType regionType = factory.retrieve(name, ruuid, loc);
        if (regionType == null) {
            return Optional.empty();  // Creation failed due to validation issues.
        }
        return Optional.of(regionType);
    }

    // Abstract method to enforce implementation in subclasses
    public final void remove() {
        eventBus.publishRemoval();
        removeWGRegion();
        nameCache.remove(name);
        onRemove();
    }

    public void onRemove() {

    }

    public abstract void dataBaseCall();

    public void onCreation(Player p) {
    }

    // Method to rename the region
    protected void rename(String name) {
        renameRegion(name);
        eventBus.publishRename(name);
        onRename(name);
    }

    public void onRename(String name) {

    }

    public void addListener(RegionTypeListener listener) {
        eventBus.subscribe(listener); // Dynamically add listeners
    }

    private void renameRegion(String name) {
        try {
            ProtectedPolygonalRegion newRegion = new ProtectedPolygonalRegion(name, region.getPoints(), region.getMinimumPoint().y(), region.getMaximumPoint().y());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))));
            if (regions == null) {
                throw new IllegalStateException("Could not access the RegionManager.");
            }
            newRegion.copyFrom(region);

            // Remove the old region and add the new one
            regions.removeRegion(region.getId());
            regions.addRegion(newRegion);

            // Update the region reference
            this.region = newRegion;

            this.name = name;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to rename region: " + e.getMessage());
        }
    }

    public ProtectedRegion getWorldguardRegion() {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            Bukkit.getLogger().severe("World 'world' not found.");
            return null;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) {
            Bukkit.getLogger().severe("Could not access RegionManager for the world.");
            return null;
        }

        for (ProtectedRegion region : regions.getRegions().values()) {
            if (region.getFlag(RegionFlag.REGION_UUID_FLAG) == null) continue;
            UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(RegionFlag.REGION_UUID_FLAG)));
            if (this.id.equals(settlementUUID)) {
                return region;
            }
        }

        Bukkit.getLogger().warning("No matching region found for UUID: " + this.id);
        return null;
    }

    public void addMember(UUID uuid) {
        this.region.getMembers().addPlayer(uuid);
    }

    public void removeMember(UUID uuid) {
        this.region.getMembers().removePlayer(uuid);
    }

    protected void removeWGRegion() {
        ProtectedRegion region = getWorldguardRegion();
        if (region == null) {
            Bukkit.getLogger().warning("Attempted to remove a region that does not exist.");
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))));
        if (regions != null) {
            regions.removeRegion(region.getId());
        } else {
            Bukkit.getLogger().severe("Failed to access RegionManager while removing a region.");
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static List<String> getRegionTypes() {
        return new ArrayList<>(registry.keySet());
    }
}

