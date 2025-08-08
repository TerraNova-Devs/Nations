package de.terranova.nations.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.worldguard.NationsRegionFlag.TypeFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class BoundaryClaimFunctions {

  public static ProtectedRegion asProtectedRegion(Region region, String id) {
    if (region instanceof CuboidRegion cuboid) {
      BlockVector3 min = cuboid.getMinimumPoint();
      BlockVector3 max = cuboid.getMaximumPoint();
      return new ProtectedCuboidRegion(id, min, max);
    } else if (region instanceof Polygonal2DRegion polygon) {
      List<BlockVector2> points = polygon.getPoints();
      int minY = polygon.getMinimumY();
      int maxY = polygon.getMaximumY();
      return new ProtectedPolygonalRegion(id, points, minY, maxY);
    } else {
      return null; // Unsupported region type
    }
  }

  private static boolean linesIntersect(
      BlockVector2 p1, BlockVector2 p2, BlockVector2 q1, BlockVector2 q2) {
    return ccw(p1, q1, q2) != ccw(p2, q1, q2) && ccw(p1, p2, q1) != ccw(p1, p2, q2);
  }

  private static boolean ccw(BlockVector2 a, BlockVector2 b, BlockVector2 c) {
    return (c.z() - a.z()) * (b.x() - a.x()) > (b.z() - a.z()) * (c.x() - a.x());
  }

  public static int getNextFreeRegionNumber(String baseName) {
    Set<Integer> usedNumbers = getUsedRegionNumbers(baseName);
    int nextFree = 1;
    while (usedNumbers.contains(nextFree)) {
      nextFree++;
    }
    return nextFree;
  }

  public static boolean isRegionNumberFree(String baseName, int number) {
    return !getUsedRegionNumbers(baseName).contains(number);
  }

  private static Set<Integer> getUsedRegionNumbers(String baseName) {
    Set<Integer> usedNumbers = new HashSet<>();
    Pattern pattern = Pattern.compile("^" + Pattern.quote(baseName.toLowerCase()) + "_(\\d+)$");

    World world = Bukkit.getWorld("world");
    if (world == null) return usedNumbers;

    RegionManager regionManager =
        WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    if (regionManager == null) return usedNumbers;

    for (ProtectedRegion region : regionManager.getRegions().values()) {
      String id = region.getId().toLowerCase();

      if (id.equals(baseName.toLowerCase())) {
        usedNumbers.add(0); // reserved base name
      } else {
        Matcher matcher = pattern.matcher(id);
        if (matcher.matches()) {
          int num = Integer.parseInt(matcher.group(1));
          usedNumbers.add(num);
        }
      }
    }

    return usedNumbers;
  }

  public static boolean propertyPointInside2DBox(
      World bukkitWorld, BlockVector2 min, BlockVector2 max, String typeFlag) {
    RegionManager manager =
        WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .get(BukkitAdapter.adapt(bukkitWorld));
    if (manager == null) return false;

    // Normalize bounds
    int minX = Math.min(min.x(), max.x());
    int maxX = Math.max(min.x(), max.x());
    int minZ = Math.min(min.z(), max.z());
    int maxZ = Math.max(min.z(), max.z());

    for (ProtectedRegion region : manager.getRegions().values()) {
      String type = region.getFlag(TypeFlag.NATIONS_TYPE);
      if (!typeFlag.equalsIgnoreCase(type)) continue;

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
