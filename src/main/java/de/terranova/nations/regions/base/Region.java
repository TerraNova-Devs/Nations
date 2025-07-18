package de.terranova.nations.regions.base;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class Region {

    private static final List<String> nameCache = new ArrayList<>();
    // Registry for dynamically adding Region
    protected final UUID id;
    protected final String type;
    private final RegionEventBus eventBus = new RegionEventBus();
    protected String name;
    protected ProtectedRegion region;

    public Region(String name, UUID id, String type) {
        this.id = id;
        this.type = type.toLowerCase();
        if (name == null) return;
        this.name = name;
        de.terranova.nations.regions.RegionManager.addRegion(getType(), getId(), this);
    }

    public Location getRegionCenter() {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        int centerX = (min.x() + max.x()) / 2;
        int centerY = (min.y() + max.y()) / 2;
        int centerZ = (min.z() + max.z()) / 2;
        BlockVector3 location = BlockVector3.at(centerX, centerY, centerZ);
        return new Location(Bukkit.getWorld("world"),location.x(), location.y(), location.z());
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
    public void rename(String name) {
        renameRegion(name);
        eventBus.publishRename(name);
        onRename(name);
    }

    public void onRename(String name) {
    }

    public void addListener(RegionListener listener) {
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
        getWorldguardRegion().getMembers().addPlayer(uuid);
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

    public boolean doesOverlap2D(ProtectedRegion r2) {
        Polygon poly1 = get2DPolygon(region);
        Polygon poly2 = get2DPolygon(r2);

        if (poly1 == null || poly2 == null) return false;

        // Early bounding box exclusion
        if (!poly1.getBounds2D().intersects(poly2.getBounds2D())) {
            return false;
        }

        // 1. Edge intersection
        if (edgesIntersect(poly1, poly2)) return true;

        // 2. One region inside the other
        if (pointsInside(poly1, poly2)) return true;
        if (pointsInside(poly2, poly1)) return true;

        return false;
    }

    private static boolean edgesIntersect(Polygon p1, Polygon p2) {
        for (int i = 0; i < p1.npoints; i++) {
            int nextI = (i + 1) % p1.npoints;
            double x1 = p1.xpoints[i], y1 = p1.ypoints[i];
            double x2 = p1.xpoints[nextI], y2 = p1.ypoints[nextI];

            for (int j = 0; j < p2.npoints; j++) {
                int nextJ = (j + 1) % p2.npoints;
                double x3 = p2.xpoints[j], y3 = p2.ypoints[j];
                double x4 = p2.xpoints[nextJ], y4 = p2.ypoints[nextJ];

                if (Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pointsInside(Polygon source, Polygon target) {
        for (int i = 0; i < source.npoints; i++) {
            if (target.contains(source.xpoints[i], source.ypoints[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a ProtectedRegion to a 2D polygon on the XZ plane.
     */
    private Polygon get2DPolygon(ProtectedRegion region) {
        if (region == null) return null;

        Polygon polygon = new Polygon();

        switch (region.getType().name().toLowerCase()) {
            case "cuboid" -> {
                var min = region.getMinimumPoint();
                var max = region.getMaximumPoint();
                int minX = min.x();
                int maxX = max.x();
                int minZ = min.z();
                int maxZ = max.z();

                polygon.addPoint(minX, minZ);
                polygon.addPoint(minX, maxZ);
                polygon.addPoint(maxX, maxZ);
                polygon.addPoint(maxX, minZ);
            }

            case "polygon" -> {
                for (var point : region.getPoints()) {
                    polygon.addPoint(point.x(), point.z());
                }
            }

            default -> {
                return null; // Unsupported region type
            }
        }

        return polygon;
    }

}