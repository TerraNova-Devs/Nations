package de.terranova.nations.worldguard.math;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Claims of grid regions in cells of 48 × 48 blocks. The cells are the truth: claim and unclaim
 * read the cells from the WorldGuard points, add or remove one and build the outline anew, so an
 * unclaim undoes a claim point for point.
 */
public class claimCalc {

  private static final int SUPERCHUNK_SIZE = 48;

  static final String ALREADY_CLAIMED = "Diese Fläche gehört schon zu deiner Stadt.";
  static final String NOT_CLAIMED = "Diese Fläche gehört nicht zu deiner Stadt.";
  static final String FOUNDING_CELL = "Du kannst den Initialclaim nicht entfernen!";
  static final String NOT_CONNECTED =
      "Die Stadt muss zusammenhängen, über eine Kante, nicht nur über eine Ecke.";
  static final String NO_SINGLE_OUTLINE =
      "So entstünde ein Loch oder eine Berührung nur über eine Ecke.";

  /** A cell of the grid by its corner with the smallest x and z. */
  public record Cell(long x, long z) {

    /** The cell of the block at {@code x}, {@code z}. */
    public static Cell at(double x, double z) {
      return new Cell(gridFloor(x), gridFloor(z));
    }

    List<Cell> neighbors() {
      return List.of(
          new Cell(x + SUPERCHUNK_SIZE, z),
          new Cell(x - SUPERCHUNK_SIZE, z),
          new Cell(x, z + SUPERCHUNK_SIZE),
          new Cell(x, z - SUPERCHUNK_SIZE));
    }
  }

  /** The new WorldGuard points of a region, or the reason, for the player, why not. */
  public record Change(List<Vectore2> points, String refusal) {
    static Change refused(String refusal) {
      return new Change(null, refusal);
    }
  }

  /** The region of {@code points} with {@code cell} added. */
  public static Change claim(List<Vectore2> points, Cell cell) {
    Set<Cell> cells = cellsOf(points);
    if (!cells.add(cell)) {
      return Change.refused(ALREADY_CLAIMED);
    }
    return outline(cells);
  }

  /** The region of {@code points} without {@code cell}; the founding cell always stays. */
  public static Change unclaim(List<Vectore2> points, Cell cell, Cell founding) {
    if (cell.equals(founding)) {
      return Change.refused(FOUNDING_CELL);
    }
    Set<Cell> cells = cellsOf(points);
    if (!cells.remove(cell)) {
      return Change.refused(NOT_CLAIMED);
    }
    return outline(cells);
  }

  /**
   * The cells of a region from its WorldGuard points. The middle of each cell decides, 24 blocks
   * from any edge, so the direction of the points and the corners old versions wrote do not matter.
   */
  public static Set<Cell> cellsOf(List<Vectore2> points) {
    Set<Cell> cells = new HashSet<>();
    if (points.size() < 3) {
      return cells;
    }
    double minX = Double.MAX_VALUE;
    double minZ = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxZ = -Double.MAX_VALUE;
    for (Vectore2 p : points) {
      minX = Math.min(minX, p.x);
      minZ = Math.min(minZ, p.z);
      maxX = Math.max(maxX, p.x);
      maxZ = Math.max(maxZ, p.z);
    }
    for (long x = gridFloor(minX); x <= maxX; x += SUPERCHUNK_SIZE) {
      for (long z = gridFloor(minZ); z <= maxZ; z += SUPERCHUNK_SIZE) {
        double half = SUPERCHUNK_SIZE / 2.0;
        if (containsPoint(points, x + half, z + half)) {
          cells.add(new Cell(x, z));
        }
      }
    }
    return cells;
  }

  /**
   * The WorldGuard points of exactly these cells: one ring along the outline, a point at each
   * corner. Refused if the cells do not touch along edges or the outline is not a single ring, as
   * with a hole or cells that touch only at a corner.
   */
  static Change outline(Set<Cell> cells) {
    if (!isConnected(cells)) {
      return Change.refused(NOT_CONNECTED);
    }
    List<GridPoint> ring = traceSingleOutline(cells);
    if (ring == null) {
      return Change.refused(NO_SINGLE_OUTLINE);
    }
    List<Vectore2> points = new ArrayList<>();
    for (int i = 0; i < ring.size(); i++) {
      GridPoint last = ring.get((i - 1 + ring.size()) % ring.size());
      GridPoint point = ring.get(i);
      GridPoint next = ring.get((i + 1) % ring.size());
      boolean straight =
          (last.x() == point.x() && point.x() == next.x())
              || (last.z() == point.z() && point.z() == next.z());
      if (!straight) {
        points.add(block(point, cells));
      }
    }
    return new Change(points, null);
  }

  /**
   * The block WorldGuard stores for a corner of the outline. Of the four blocks around the corner,
   * a convex corner has one inside: that one. A concave corner has one outside: the block
   * diagonal to it. Neither depends on the direction of the ring.
   */
  private static Vectore2 block(GridPoint corner, Set<Cell> cells) {
    long x = corner.x();
    long z = corner.z();
    boolean nw = inside(cells, x - 1, z - 1);
    boolean ne = inside(cells, x, z - 1);
    boolean sw = inside(cells, x - 1, z);
    boolean se = inside(cells, x, z);
    boolean convex = (nw ? 1 : 0) + (ne ? 1 : 0) + (sw ? 1 : 0) + (se ? 1 : 0) == 1;
    boolean east = convex ? ne || se : ne && se;
    boolean south = convex ? sw || se : sw && se;
    return new Vectore2(east ? x : x - 1, south ? z : z - 1);
  }

  private static boolean inside(Set<Cell> cells, long blockX, long blockZ) {
    return cells.contains(Cell.at(blockX, blockZ));
  }

  public static double abstand(Vectore2 a, Vectore2 b) {
    return Math.sqrt((Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2)));
  }

  private static boolean containsPoint(List<Vectore2> polygon, double x, double z) {
    boolean inside = false;
    for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
      Vectore2 a = polygon.get(i);
      Vectore2 b = polygon.get(j);
      if (((a.z > z) != (b.z > z)) && (x < (b.x - a.x) * (z - a.z) / (b.z - a.z) + a.x)) {
        inside = !inside;
      }
    }
    return inside;
  }

  private static boolean isConnected(Set<Cell> cells) {
    if (cells.isEmpty()) {
      return false;
    }

    Queue<Cell> queue = new ArrayDeque<>();
    Set<Cell> seen = new HashSet<>();
    Cell first = cells.iterator().next();
    queue.add(first);
    seen.add(first);

    while (!queue.isEmpty()) {
      Cell cell = queue.remove();
      for (Cell neighbor : cell.neighbors()) {
        if (cells.contains(neighbor) && seen.add(neighbor)) {
          queue.add(neighbor);
        }
      }
    }
    return seen.size() == cells.size();
  }

  /** The outline as one ring of grid corners, or null if it is not one ring. */
  private static List<GridPoint> traceSingleOutline(Set<Cell> cells) {
    Set<Edge> edges = boundaryEdges(cells);
    if (edges.isEmpty()) {
      return null;
    }

    Map<GridPoint, GridPoint> nextByPoint = new HashMap<>();
    for (Edge edge : edges) {
      if (nextByPoint.put(edge.from(), edge.to()) != null) {
        return null;
      }
    }

    GridPoint start = startPoint(edges);
    List<GridPoint> outline = new ArrayList<>();
    GridPoint current = start;

    do {
      outline.add(current);
      current = nextByPoint.get(current);
      if (current == null || outline.size() > edges.size()) {
        return null;
      }
    } while (!current.equals(start));

    if (outline.size() != edges.size()) {
      return null;
    }
    return outline;
  }

  private static Set<Edge> boundaryEdges(Set<Cell> cells) {
    Set<Edge> edges = new HashSet<>();
    for (Cell cell : cells) {
      long x = cell.x();
      long z = cell.z();
      GridPoint nw = new GridPoint(x, z);
      GridPoint ne = new GridPoint(x + SUPERCHUNK_SIZE, z);
      GridPoint se = new GridPoint(x + SUPERCHUNK_SIZE, z + SUPERCHUNK_SIZE);
      GridPoint sw = new GridPoint(x, z + SUPERCHUNK_SIZE);

      addBoundaryEdge(edges, new Edge(nw, ne));
      addBoundaryEdge(edges, new Edge(ne, se));
      addBoundaryEdge(edges, new Edge(se, sw));
      addBoundaryEdge(edges, new Edge(sw, nw));
    }
    return edges;
  }

  private static void addBoundaryEdge(Set<Edge> edges, Edge edge) {
    Edge reverse = new Edge(edge.to(), edge.from());
    if (!edges.remove(reverse)) {
      edges.add(edge);
    }
  }

  private static GridPoint startPoint(Set<Edge> edges) {
    GridPoint start = null;
    for (Edge edge : edges) {
      GridPoint point = edge.from();
      if (start == null
          || point.z() < start.z()
          || (point.z() == start.z() && point.x() < start.x())) {
        start = point;
      }
    }
    return start;
  }

  private static long gridFloor(double value) {
    return (long) Math.floor(value / SUPERCHUNK_SIZE) * SUPERCHUNK_SIZE;
  }

  private record GridPoint(long x, long z) {}

  private record Edge(GridPoint from, GridPoint to) {}
}
