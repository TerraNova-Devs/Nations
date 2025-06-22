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
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.worldguard.NationsRegionFlag.TypeFlag;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PropertyValidationFunctions {
    public static boolean isValidSelection(Player player) throws IncompleteRegionException {
        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        RegionSelector selector = session.getRegionSelector(BukkitAdapter.adapt(player.getWorld()));

        if (!selector.isDefined()) return false;

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
}
