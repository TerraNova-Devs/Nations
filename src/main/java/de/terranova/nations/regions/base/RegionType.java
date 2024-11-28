package de.terranova.nations.regions.base;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class RegionType {

    // Registry for storing region creators by type name
    private static final Map<String, RegionCreator> registry = new HashMap<>();

    // Static registry for storing region types
    private static final Set<String> regionTypes = new HashSet<>();

    protected final UUID id;
    protected String name;
    protected ProtectedRegion region;
    private final String type;

    public RegionType(String name, UUID id, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // Static method to create a region instance dynamically
    public static RegionType createRegion(String typeName, String name, Player player, UUID id, Vectore2 location) {
        RegionCreator creator = registry.get(typeName.toLowerCase());
        if (creator == null) {
            throw new IllegalArgumentException("Unknown region type: " + typeName);
        }
        return creator.create(name, player, id, location);
    }

    // Abstract method to enforce implementation in subclasses
    public abstract void remove();

    // Static method for registering new region types
    protected static void registerRegionType(String typeName, RegionCreator creator) {
        if (!regionTypes.contains(typeName.toLowerCase())) {
            regionTypes.add(typeName.toLowerCase());
            registry.put(typeName.toLowerCase(), creator);
        }
    }

    // Static method to get all registered region types
    public static Set<String> getAvailableRegionTypes() {
        return Collections.unmodifiableSet(regionTypes); // Return an unmodifiable view of the set
    }

    // Method to rename the region
    public void rename(String name) {
        renameRegion(name);
    }

    protected void renameRegion(String name) {
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

            SettleDBstuff settleDB = new SettleDBstuff(this.id);
            settleDB.rename(name);

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

    // Static method to check if a condition is met for a region type
    public static boolean conditionCheck(String typeName, Player player, String name) {
        RegionCreator creator = registry.get(typeName.toLowerCase());
        if (creator == null) {
            return false;
        }
        return creator.conditionCheck(player, name);
    }

    // Abstract Factory interface for creating regions
    public interface RegionCreator {
        RegionType create(String name, Player player, UUID id, Vectore2 location);

        boolean conditionCheck(Player player, String name);
    }
}

