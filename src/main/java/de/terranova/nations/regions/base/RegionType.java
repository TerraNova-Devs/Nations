package de.terranova.nations.regions.base;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class RegionType {

    // Registry for storing region creators by type name
  //  private static final Map<String, RegionCreator> registry = new HashMap<>();

    // Static registry for storing region types


    protected final UUID id;
    protected String name;
    protected ProtectedRegion region;
    protected final String type;

    public RegionType(String name, UUID id, String type) {
        this.id = id;
        this.type = type.toLowerCase();
        if(name == null) return;
        this.name = name;
    }


    // Abstract method to enforce implementation in subclasses
    public abstract void remove();
    public void postInit(Player p) {
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

            SettleDBstuff settleDB = new SettleDBstuff(this.id, this.type);
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


    // Registry for dynamically adding RegionType
    public static final Map<String, RegionFactory> registry = new HashMap<>();

    // Method to register new RegionType creators
    public static void registerRegionType(String type, RegionFactory factory) {
        registry.put(type.toLowerCase(), factory);
    }

    // Method to deregister an RegionType
    public static void deregisterRegionType(String type) {
        registry.remove(type.toLowerCase());
    }

    // Factory method to create RegionTypes
    public static Optional<RegionType> createRegionType(String type, String name, Player p) {
        RegionFactory factory = registry.get(type.toLowerCase());
        if (factory == null) {
            p.sendMessage(Chat.errorFade("No such region type registered: " + type));
            return Optional.empty();
        }

        RegionType regionType = factory.create(name, p);
        if (regionType == null) {
            return Optional.empty();  // Creation failed due to validation issues.
        }

        return Optional.of(regionType);
    }
}

