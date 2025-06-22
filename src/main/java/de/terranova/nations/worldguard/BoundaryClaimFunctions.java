package de.terranova.nations.worldguard;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.worldguard.NationsRegionFlag.TypeFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoundaryClaimFunctions {
    public static boolean isValidSelection(Player player) {
        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        RegionSelector selector = session.getRegionSelector(BukkitAdapter.adapt(player.getWorld()));

        if (!selector.isDefined()) return false;
        try {
            Region region = selector.getRegion();
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            BlockVector2 point1 = BlockVector2.at(min.x(), min.z());
            BlockVector2 point2 = BlockVector2.at(max.x(), max.z());

            // 🔍 Get region manager
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
            if (regionManager == null) return false;

            // 🔍 Find region with flag "settle"
            ProtectedRegion parentRegion = null;
            for (ProtectedRegion r : regionManager.getRegions().values()) {
                String type = r.getFlag(TypeFlag.NATIONS_TYPE);
                if ("settle".equalsIgnoreCase(type)) {
                    parentRegion = r;
                    break;
                }
            }

            if (parentRegion == null) return false;

            boolean inside1 = isInsideRegion(point1, parentRegion);
            boolean inside2 = isInsideRegion(point2, parentRegion);

            if (!inside1 || !inside2) return false;

            return !lineCrossesRegionBorder(point1, point2, parentRegion);
        } catch (IncompleteRegionException e) {
            return false;
        }
    }

    private static boolean isInsideRegion(BlockVector2 point, ProtectedRegion region) {
        if (region instanceof ProtectedPolygonalRegion) {
            return isInsidePolygon(point, region.getPoints());
        } else if (region instanceof ProtectedCuboidRegion) {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            return point.x() >= min.x() && point.x() <= max.x()
                    && point.z() >= min.z() && point.z() <= max.z();
        }
        return false;
    }

    private static boolean lineCrossesRegionBorder(BlockVector2 p1, BlockVector2 p2, ProtectedRegion region) {
        List<BlockVector2> edges = new ArrayList<>();

        if (region instanceof ProtectedPolygonalRegion) {
            edges = region.getPoints();
        } else if (region instanceof ProtectedCuboidRegion) {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            edges.add(BlockVector2.at(min.x(), min.z()));
            edges.add(BlockVector2.at(min.x(), max.z()));
            edges.add(BlockVector2.at(max.x(), max.z()));
            edges.add(BlockVector2.at(max.x(), min.z()));
        } else {
            return false;
        }

        for (int i = 0; i < edges.size(); i++) {
            BlockVector2 a = edges.get(i);
            BlockVector2 b = edges.get((i + 1) % edges.size());

            if (linesIntersect(p1, p2, a, b)) {
                return true;
            }
        }
        return false;
    }



    private static boolean isInsidePolygon(BlockVector2 point, List<BlockVector2> polygon) {
        int intersections = 0;
        for (int i = 0; i < polygon.size(); i++) {
            BlockVector2 a = polygon.get(i);
            BlockVector2 b = polygon.get((i + 1) % polygon.size());

            if (((a.z() > point.z()) != (b.z() > point.z())) &&
                    (point.x() < (b.x() - a.x()) * (point.z() - a.z()) / (double)(b.z() - a.z()) + a.x())) {
                intersections++;
            }
        }
        return (intersections % 2) == 1;
    }

    private static boolean linesIntersect(BlockVector2 p1, BlockVector2 p2, BlockVector2 q1, BlockVector2 q2) {
        return ccw(p1, q1, q2) != ccw(p2, q1, q2) && ccw(p1, p2, q1) != ccw(p1, p2, q2);
    }

    private static boolean ccw(BlockVector2 a, BlockVector2 b, BlockVector2 c) {
        return (c.z() - a.z()) * (b.x() - a.x()) > (b.z() - a.z()) * (c.x() - a.x());
    }
    public static int getNextFreeRegionNumber(String baseName) {
        Set<Integer> usedNumbers = new HashSet<>();
        Pattern pattern = Pattern.compile("^" + Pattern.quote(baseName.toLowerCase()) + "_(\\d+)$");

        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (regionManager == null) continue;

            for (ProtectedRegion region : regionManager.getRegions().values()) {
                String id = region.getId().toLowerCase();

                if (id.equals(baseName)) {
                    usedNumbers.add(0); // reserved without suffix
                    continue;
                }

                Matcher matcher = pattern.matcher(id);
                if (matcher.matches()) {
                    int number = Integer.parseInt(matcher.group(1));
                    usedNumbers.add(number);
                }
            }
        }

        // Find the smallest free number starting from 1
        int nextFree = 1;
        while (usedNumbers.contains(nextFree)) {
            nextFree++;
        }

        return nextFree;
    }

    public static boolean propertyPointInside2DBox(World bukkitWorld, BlockVector2 min, BlockVector2 max) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(bukkitWorld));
        if (manager == null) return false;

        // Normalize bounds
        int minX = Math.min(min.x(), max.x());
        int maxX = Math.max(min.x(), max.x());
        int minZ = Math.min(min.z(), max.z());
        int maxZ = Math.max(min.z(), max.z());

        for (ProtectedRegion region : manager.getRegions().values()) {
            String type = region.getFlag(TypeFlag.NATIONS_TYPE);
            if (!"property".equalsIgnoreCase(type)) continue;

            // Representative point of the other region (e.g., its center)
            int regionX = (region.getMinimumPoint().x() + region.getMaximumPoint().x()) / 2;
            int regionZ = (region.getMinimumPoint().z() + region.getMaximumPoint().z()) / 2;

            if (regionX >= minX && regionX <= maxX && regionZ >= minZ && regionZ <= maxZ) {
                return true;
            }
        }

        return false;
    }
    public static boolean isPointIn2DBox(Vectore2 x, Vectore2 z, Vectore2 point) {
        // Normalize bounds
        double minX = Math.min(x.x, z.x);
        double maxX = Math.max(x.x, z.x);
        double minZ = Math.min(x.z, z.z);
        double maxZ = Math.max(x.z, z.z);

        double px = point.x;
        double pz = point.z;

        return px >= minX && px <= maxX && pz >= minZ && pz <= maxZ;
    }
}
