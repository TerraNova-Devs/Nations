package de.terranova.nations.worldguard;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.utils.terraRenderer.refactor.Listener.MarkToolListener;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import de.terranova.nations.worldguard.NationsRegionFlag.TypeFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import java.util.*;
import net.goldtreeservers.worldguardextraflags.flags.Flags;
import net.goldtreeservers.worldguardextraflags.flags.helpers.ForcedStateFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RegionClaimFunctions {

  public static ProtectedRegion createGridClaim(String name, Player p, UUID uuid, String type) {

    int nx = (int) (Math.floor(p.getLocation().x() / 48) * 48);
    int nz = (int) (Math.floor(p.getLocation().z() / 48) * 48);

    BlockVector2 nw = BlockVector2.at(nx, nz);
    BlockVector2 sw = BlockVector2.at(nx, nz + 47);
    BlockVector2 se = BlockVector2.at(nx + 47, nz + 47);
    BlockVector2 ne = BlockVector2.at(nx + 47, nz);

    List<BlockVector2> corners = Arrays.asList(nw, ne, se, sw);

    LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

    ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(name, corners, -64, 320);

    DefaultDomain owners = region.getOwners();
    owners.addPlayer(lp);
    region.setOwners(owners);
    region.setFlag(RegionFlag.REGION_UUID_FLAG, uuid.toString());
    region.setFlag(TypeFlag.NATIONS_TYPE, type);
    region.setFlag(Flags.GLIDE, ForcedStateFlag.ForcedState.ALLOW);
    region.setPriority(100);

    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regions = container.get(lp.getWorld());

    assert regions != null;
    regions.addRegion(region);
    return regions.getRegion(region.getId());
  }

  public static ProtectedRegion createBoundaryClaim(String name, Player p, UUID uuid, String type) {

    Optional<MarkToolListener.RegionSelection> selOpt =
            MarkToolListener.getSelection(p.getUniqueId());

    if (selOpt.isEmpty()) {
      p.sendMessage(Chat.errorFade("Du hast keine Region mit dem Blaze-Tool ausgewählt."));
      return null;
    }

    com.sk89q.worldedit.regions.Region tempregion = MarkToolListener.toWorldEdit(selOpt.get());
    ProtectedRegion region = BoundaryClaimFunctions.asProtectedRegion(tempregion, name);

    org.bukkit.World bukkitWorld = p.getWorld();
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
    RegionManager regionManager = container.get(weWorld);
    if (regionManager == null) {
      p.sendMessage("WorldGuard region manager not found for this world.");
      return null;
    }

    region.setFlag(RegionFlag.REGION_UUID_FLAG, uuid.toString());
    region.setFlag(TypeFlag.NATIONS_TYPE, type);
    DefaultDomain owners = region.getOwners();
    owners.addPlayer(p.getUniqueId());
    region.setOwners(owners); // optional, da `getOwners()` nicht kopiert
    regionManager.addRegion(region);
    return region;
  }

  public static void changeFlag(Player p, UUID settlementID, Flag flag) {
    World world = Bukkit.getWorld("world");
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    assert world != null;
    RegionManager regions = container.get(BukkitAdapter.adapt(world));
    for (ProtectedRegion region : regions.getRegions().values()) {
      if (!Objects.equals(region.getFlag(RegionFlag.REGION_UUID_FLAG), settlementID)) continue;
    }
  }

  public static void addToExistingClaim(Player p, ProtectedRegion oldRegion) {
    if (oldRegion instanceof ProtectedPolygonalRegion oldPolygonalRegion) {

      int nx = (int) (Math.floor(p.getLocation().x() / 48) * 48);
      int nz = (int) (Math.floor(p.getLocation().z() / 48) * 48);

      Vectore2 nw = new Vectore2(nx + 0.5, nz + 0.5);
      Vectore2 ne = new Vectore2(nx + 0.5 + 47, nz + 0.5);
      Vectore2 sw = new Vectore2(nx + 0.5, nz + 47 + 0.5);
      Vectore2 se = new Vectore2(nx + 47 + 0.5, nz + 47 + 0.5);

      List<Vectore2> newPoints = Arrays.asList(nw, ne, se, sw);
      List<Vectore2> oldPoints = new ArrayList<>();

      for (BlockVector2 v : oldPolygonalRegion.getPoints()) {
        oldPoints.add(new Vectore2(v.x(), v.z()));
      }

      Optional<List<Vectore2>> claims = claimCalc.dothatshitforme(oldPoints, newPoints);
      if (claims.isEmpty()) {
        p.sendMessage(Chat.errorFade("Bitte keine leeren flächen umclaimen."));
        return;
      }

      List<BlockVector2> finalNewRegion = new ArrayList<>();

      for (Vectore2 v : claims.get()) {
        finalNewRegion.add(BlockVector2.at(v.x, v.z));
        // p.sendMessage(String.valueOf(BlockVector2.at(v.x, v.z)));
      }

      ProtectedPolygonalRegion region =
          new ProtectedPolygonalRegion(
              oldRegion.getId(),
              finalNewRegion,
              oldPolygonalRegion.getMinimumPoint().y(),
              oldPolygonalRegion.getMaximumPoint().y());
      region.copyFrom(oldRegion);

      LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager regions = container.get(lp.getWorld());
      assert regions != null;
      regions.addRegion(region);
    }
  }

  public static Vectore2 getSChunkMiddle(Location location) {
    int x = (int) (Math.floor(location.x() / 48) * 48);
    int z = (int) (Math.floor(location.z() / 48) * 48);
    return new Vectore2(x + 24, z + 24);
  }

  public static Vectore2 getSChunkMiddle(Vectore2 location) {
    int x = (int) (Math.floor(location.x / 48) * 48);
    int z = (int) (Math.floor(location.z / 48) * 48);
    return new Vectore2(x + 24, z + 24);
  }

  public static boolean checkAreaForSettles(Player p) {
    LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regions = container.get(lp.getWorld());
    assert regions != null;
    RegionQuery query = container.createQuery();
    ApplicableRegionSet set = query.getApplicableRegions(lp.getLocation());
    return !(set.size() == 0);
  }

  public static int getClaimAnzahl(UUID settle) {
    World world = Bukkit.getWorld("world");
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    assert world != null;
    RegionManager regions = container.get(BukkitAdapter.adapt(world));

    for (ProtectedRegion region : regions.getRegions().values()) {

      if (!Objects.equals(region.getFlag(RegionFlag.REGION_UUID_FLAG), settle.toString())) continue;

      List<Vectore2> list2 = new ArrayList();
      list2.addAll(Vectore2.fromBlockVectorList(region.getPoints()));
      List<Vectore2> list3;
      list3 = claimCalc.aufplustern(claimCalc.normalisieren(list2));

      return (int) claimCalc.area(list3.toArray(new Vectore2[list3.size()])) / 2304;
    }
    return 1;
  }

  public static void remove(String name) {}

  public static boolean checkRegionSize(ProtectedRegion region, int minHeight, int minVolume) {
    BlockVector3 min = region.getMinimumPoint();
    BlockVector3 max = region.getMaximumPoint();

    int height = max.y() - min.y() + 1;
    if (height < minHeight) return false;

    int volume;

    if (region instanceof ProtectedPolygonalRegion poly) {
      int area = calculate2dPolygonArea(poly.getPoints());
      volume = area * height;
    } else {
      int width = max.x() - min.x() + 1;
      int depth = max.z() - min.z() + 1;
      volume = width * depth * height;
    }

    return volume >= minVolume;
  }

  // Shoelace Formula
  private static int calculate2dPolygonArea(List<BlockVector2> points) {
    int area = 0;
    for (int i = 0; i < points.size(); i++) {
      BlockVector2 p1 = points.get(i);
      BlockVector2 p2 = points.get((i + 1) % points.size());

      area += (p1.x() * p2.z()) - (p2.x() * p1.z());
    }
    return Math.abs(area) / 2;
  }

  /**
   * Returns the number of blocks inside the given WorldGuard region.
   * Works for ProtectedCuboidRegion and ProtectedPolygonalRegion.
   */
  public static int getRegionVolume(ProtectedRegion region) {
    if (region instanceof ProtectedCuboidRegion cuboid) {
      return (int) cuboidVolume(cuboid);
    } else if (region instanceof ProtectedPolygonalRegion poly) {
      return (int) polygonalVolume(poly);
    }
    throw new UnsupportedOperationException(
            "Region type not supported: " + region.getClass().getSimpleName());
  }

  // --- Cuboid ---

  private static long cuboidVolume(ProtectedCuboidRegion cuboid) {
    BlockVector3 min = cuboid.getMinimumPoint();
    BlockVector3 max = cuboid.getMaximumPoint();

    long dx = (long) max.x() - min.x() + 1;
    long dy = (long) max.y() - min.y() + 1;
    long dz = (long) max.z() - min.z() + 1;

    return dx * dy * dz;
  }

  // --- Polygonal region (vertical prism) ---

  private static long polygonalVolume(ProtectedPolygonalRegion poly) {
    List<BlockVector2> pts = poly.getPoints();
    if (pts.isEmpty()) return 0L;

    int minY = poly.getMinimumPoint().y();
    int maxY = poly.getMaximumPoint().y();
    long height = (long) (maxY - minY + 1);
    if (height <= 0) return 0L;

    // Compute 2D bounding box on XZ plane
    int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
    for (BlockVector2 p : pts) {
      int x = p.x();
      int z = p.z();
      if (x < minX) minX = x;
      if (x > maxX) maxX = x;
      if (z < minZ) minZ = z;
      if (z > maxZ) maxZ = z;
    }

    long baseCells = 0L;

    // Integer grid fill: count block columns whose (x,z) is inside or on boundary
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        if (pointInPolygonInclusive(x, z, pts)) {
          baseCells++;
        }
      }
    }

    return baseCells * height;
  }

  /**
   * Ray-casting point-in-polygon with boundary inclusion (edges/vertices count as inside).
   * Coordinates are integer block coordinates on XZ plane.
   */
  private static boolean pointInPolygonInclusive(int x, int z, List<BlockVector2> polygon) {
    // Boundary check first
    for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
      BlockVector2 a = polygon.get(j);
      BlockVector2 b = polygon.get(i);
      if (pointOnSegment(x, z, a.x(), a.z(), b.x(), b.z())) {
        return true; // On edge
      }
    }

    // Standard ray-cast (odd-even rule)
    boolean inside = false;
    for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
      int xi = polygon.get(i).x();
      int zi = polygon.get(i).z();
      int xj = polygon.get(j).x();
      int zj = polygon.get(j).z();

      boolean intersect = ((zi > z) != (zj > z)) &&
              (x < (double) (xj - xi) * (z - zi) / (double) (zj - zi) + xi);
      if (intersect) inside = !inside;
    }
    return inside;
  }

  /**
   * Returns true if point P=(x,z) lies on the segment A=(x1,z1) → B=(x2,z2).
   * Uses collinearity and bounding-box checks.
   */
  private static boolean pointOnSegment(int x, int z, int x1, int z1, int x2, int z2) {
    long cross = (long) (x - x1) * (z2 - z1) - (long) (z - z1) * (x2 - x1);
    if (cross != 0) return false; // not collinear

    int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
    int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
    return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
  }

}
