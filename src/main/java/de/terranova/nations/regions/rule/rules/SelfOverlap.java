package de.terranova.nations.regions.rule.rules;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionRegistry;
import de.terranova.nations.regions.modules.HasChildren;
import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryFixer;

import java.util.ArrayList;
import java.util.List;

public class SelfOverlap implements RegionRule {

  private final boolean isGrid;

  public SelfOverlap(Boolean noParentGrid) {
    this.isGrid = noParentGrid;
  }

  @Override
  public boolean isAllowed(
          Player p,
          String type,
          String regionName,
          ProtectedRegion regionBeingPlaced,
          Region explicitParent
  ) {
    // 1) Grid occupancy check (unchanged)
    if (explicitParent == null && isGrid) {
      return !RegionClaimFunctions.checkAreaForSettles(p);
    }

    // 2) Build inner geometry (XZ plane)
    Geometry inner = toPolygonXZ(regionBeingPlaced);
    if (inner == null || inner.isEmpty()) {
      return true; // can't evaluate -> don't block
    }

    // 3) Your exact relevantRegions selection logic (unchanged)
    List<Region> relevantRegions;
    boolean hasInsideParentRule =
            RegionRegistry.getRuleSet(type).getRules().stream()
                    .anyMatch(rule -> rule instanceof WithinParent<?, ?>);

    if (hasInsideParentRule && explicitParent instanceof HasChildren hasChildren) {
      relevantRegions = hasChildren.getChildrenByType(type);
    } else {
      relevantRegions = new ArrayList<>(RegionManager.retrieveAllCachedRegions(type).values());
    }

    // 4) Disallow on ANY overlap (including containment and boundary touch)
    for (Region existing : relevantRegions) {
      // Skip self if editing/renaming same region
      if (existing.getName().equalsIgnoreCase(regionName)) continue;

      ProtectedRegion outerWG = existing.getWorldguardRegion();
      Geometry outer = toPolygonXZ(outerWG);
      if (outer == null || outer.isEmpty()) continue;

      // BLOCK: any intersection at all (touching edges/corners included)
      if (outer.intersects(inner)) {
        return false;
      }

      // If you want to allow touching but forbid area overlap/containment, use instead:
      // if (outer.intersects(inner) && !outer.touches(inner)) return false;
    }

    return true;
  }

  /* -------------------- Geometry helpers (2D, XZ plane) -------------------- */

  private static final GeometryFactory GF = new GeometryFactory();

  private Geometry toPolygonXZ(ProtectedRegion rg) {
    if (rg == null) return null;

    Geometry g;
    switch (rg.getType().name().toLowerCase()) {
      case "cuboid" -> g = buildCuboidXZ(rg);
      case "polygon" -> g = buildPolygonXZ(rg);
      default -> { return null; }
    }
    if (g == null || g.isEmpty()) return null;

    Geometry fixed = GeometryFixer.fix(g);
    return fixed.isEmpty() ? null : fixed;
  }

  private Polygon buildCuboidXZ(ProtectedRegion rg) {
    var min = rg.getMinimumPoint();
    var max = rg.getMaximumPoint();
    int minX = min.x(), minZ = min.z(), maxX = max.x(), maxZ = max.z();
    if (minX == maxX || minZ == maxZ) return null;

    Coordinate[] shell = new Coordinate[] {
            c(minX, minZ), c(minX, maxZ), c(maxX, maxZ), c(maxX, minZ), c(minX, minZ)
    };
    return polygon(shell);
  }

  private Polygon buildPolygonXZ(ProtectedRegion rg) {
    var pts = rg.getPoints();
    if (pts == null || pts.size() < 3) return null;

    List<Coordinate> coords = new ArrayList<>(pts.size() + 1);
    Integer lastX = null, lastZ = null;
    for (var p : pts) {
      if (lastX != null && lastX == p.x() && lastZ == p.z()) continue;
      coords.add(c(p.x(), p.z()));
      lastX = p.x(); lastZ = p.z();
    }
    if (coords.size() < 3) return null;

    if (!coords.get(0).equals2D(coords.get(coords.size() - 1))) {
      coords.add(new Coordinate(coords.get(0)));
    }
    if (coords.size() < 4) return null;

    return polygon(coords.toArray(Coordinate[]::new));
  }

  private Polygon polygon(Coordinate[] shell) {
    LinearRing ring = GF.createLinearRing(shell);
    if (ring.isEmpty() || ring.getNumPoints() < 4) return null;
    return GF.createPolygon(ring);
  }

  /** Map (x,z) -> JTS (x,y) for 2D operations. */
  private static Coordinate c(double x, double z) {
    return new Coordinate(x, z);
  }

  @Override
  public String getErrorMessage() {
    return "Diese Region darf sich nicht mit einer anderen Region desselben Typs überschneiden.";
  }
}