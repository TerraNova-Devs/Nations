package de.terranova.nations.worldguard.math;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class claimCalc {

  private static final int SUPERCHUNK_SIZE = 48;
  private static final double WORLDGUARD_OFFSET = 0.5;

  public static Optional<List<Vectore2>> dothatshitforme(
      List<Vectore2> oldlist, List<Vectore2> newlist) {
    if (oldlist == null || oldlist.size() < 4 || newlist == null || newlist.size() < 4) {
      return Optional.empty();
    }

    Set<Cell> cells = cellsFromWorldGuardRegion(oldlist);
    Set<Cell> newCells = cellsFromClaimRegion(newlist);
    if (newCells.isEmpty()) {
      return Optional.empty();
    }

    cells.addAll(newCells);
    if (!isConnected(cells)) {
      return Optional.empty();
    }

    Optional<List<GridPoint>> outline = traceSingleOutline(cells);
    if (outline.isEmpty()) {
      return Optional.empty();
    }

    List<Vectore2> compactOutline = entprojezieren(toVectors(outline.get()));
    List<Vectore2> centered = reverseaufplustern(compactOutline);
    return Optional.of(entnormalisieren(centered));
  }

  public static double area(Vectore2[] vertices) {
    double sum = 0;
    for (int i = 0; i < vertices.length; i++) {
      Vectore2 current = vertices[i];
      Vectore2 next = vertices[(i + 1) % vertices.length];
      sum += current.x * next.z - next.x * current.z;
    }
    return Math.abs(sum) / 2;
  }

  public static List<Vectore2> normalisieren(List<Vectore2> current) {
    List<Vectore2> output = new ArrayList<>();
    for (Vectore2 v : current) {
      output.add(new Vectore2(v.x + WORLDGUARD_OFFSET, v.z + WORLDGUARD_OFFSET));
    }
    return output;
  }

  static List<Vectore2> entnormalisieren(List<Vectore2> current) {
    List<Vectore2> output = new ArrayList<>();
    for (Vectore2 v : current) {
      output.add(new Vectore2(v.x - WORLDGUARD_OFFSET, v.z - WORLDGUARD_OFFSET));
    }
    return output;
  }

  static Optional<List<Vectore2>> mergen(List<Vectore2> oldRegion, List<Vectore2> newRegion) {
    Set<Cell> cells = cellsFromClaimRegion(reverseaufplustern(entprojezieren(oldRegion)));
    cells.addAll(cellsFromClaimRegion(reverseaufplustern(entprojezieren(newRegion))));
    Optional<List<GridPoint>> outline = traceSingleOutline(cells);
    return outline.map(claimCalc::toVectors);
  }

  public static double abstand(Vectore2 a, Vectore2 b) {
    return Math.sqrt((Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2)));
  }

  static List<Vectore2> projezieren(List<Vectore2> current) {
    List<Vectore2> output = new ArrayList<>();
    if (current.isEmpty()) {
      return output;
    }

    for (int i = 0; i < current.size(); i++) {
      Vectore2 start = current.get(i);
      Vectore2 end = current.get((i + 1) % current.size());
      output.add(new Vectore2(start.x, start.z));

      double dx = end.x - start.x;
      double dz = end.z - start.z;
      int steps = (int) (Math.max(Math.abs(dx), Math.abs(dz)) / SUPERCHUNK_SIZE);
      for (int step = 1; step < steps; step++) {
        output.add(
            new Vectore2(
                start.x + Math.signum(dx) * SUPERCHUNK_SIZE * step,
                start.z + Math.signum(dz) * SUPERCHUNK_SIZE * step));
      }
    }
    return output;
  }

  static List<Vectore2> entprojezieren(List<Vectore2> current) {
    List<Vectore2> output = new ArrayList<>();
    if (current.size() < 3) {
      output.addAll(current);
      return output;
    }

    for (int i = 0; i < current.size(); i++) {
      Vectore2 last = current.get((i - 1 + current.size()) % current.size());
      Vectore2 point = current.get(i);
      Vectore2 next = current.get((i + 1) % current.size());
      if ((last.x == point.x && point.x == next.x) || (last.z == point.z && point.z == next.z)) {
        continue;
      }
      output.add(point);
    }
    return output;
  }

  public static List<Vectore2> aufplustern(List<Vectore2> current) {
    List<Vectore2> output = new ArrayList<>();
    if (current.size() < 3) {
      return output;
    }

    for (int i = 0; i < current.size(); i++) {
      Vectore2 last = current.get((i - 1 + current.size()) % current.size());
      Vectore2 point = current.get(i);
      Vectore2 next = current.get((i + 1) % current.size());
      output.add(offsetCorner(point, last, next, WORLDGUARD_OFFSET));
    }
    return output;
  }

  static List<Vectore2> reverseaufplustern(List<Vectore2> current) {
    List<Vectore2> output = new ArrayList<>();
    if (current.size() < 3) {
      return output;
    }

    for (int i = 0; i < current.size(); i++) {
      Vectore2 last = current.get((i - 1 + current.size()) % current.size());
      Vectore2 point = current.get(i);
      Vectore2 next = current.get((i + 1) % current.size());
      output.add(offsetCorner(point, last, next, -WORLDGUARD_OFFSET));
    }
    return output;
  }

  private static Vectore2 offsetCorner(Vectore2 point, Vectore2 last, Vectore2 next, double offset) {
    double previousDirectionX = Math.signum(point.x - last.x);
    double previousDirectionZ = Math.signum(point.z - last.z);
    double nextDirectionX = Math.signum(next.x - point.x);
    double nextDirectionZ = Math.signum(next.z - point.z);

    double xOffset = offset * (previousDirectionX - nextDirectionX);
    double zOffset = offset * (previousDirectionZ - nextDirectionZ);
    return new Vectore2(point.x + xOffset, point.z + zOffset);
  }

  private static Set<Cell> cellsFromWorldGuardRegion(List<Vectore2> points) {
    return cellsInsideBoundary(aufplustern(normalisieren(points)));
  }

  private static Set<Cell> cellsFromClaimRegion(List<Vectore2> points) {
    return cellsInsideBoundary(aufplustern(points));
  }

  private static Set<Cell> cellsInsideBoundary(List<Vectore2> boundary) {
    Set<Cell> cells = new HashSet<>();
    if (boundary.size() < 4) {
      return cells;
    }

    long minX = Long.MAX_VALUE;
    long minZ = Long.MAX_VALUE;
    long maxX = Long.MIN_VALUE;
    long maxZ = Long.MIN_VALUE;
    for (Vectore2 point : boundary) {
      minX = Math.min(minX, gridFloor(point.x));
      minZ = Math.min(minZ, gridFloor(point.z));
      maxX = Math.max(maxX, gridCeil(point.x));
      maxZ = Math.max(maxZ, gridCeil(point.z));
    }

    for (long x = minX; x < maxX; x += SUPERCHUNK_SIZE) {
      for (long z = minZ; z < maxZ; z += SUPERCHUNK_SIZE) {
        if (containsPoint(boundary, x + SUPERCHUNK_SIZE / 2.0, z + SUPERCHUNK_SIZE / 2.0)) {
          cells.add(new Cell(x, z));
        }
      }
    }
    return cells;
  }

  private static boolean containsPoint(List<Vectore2> polygon, double x, double z) {
    boolean inside = false;
    for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
      Vectore2 a = polygon.get(i);
      Vectore2 b = polygon.get(j);
      if (((a.z > z) != (b.z > z))
          && (x < (b.x - a.x) * (z - a.z) / (b.z - a.z) + a.x)) {
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

  private static Optional<List<GridPoint>> traceSingleOutline(Set<Cell> cells) {
    Set<Edge> edges = boundaryEdges(cells);
    if (edges.isEmpty()) {
      return Optional.empty();
    }

    Map<GridPoint, GridPoint> nextByPoint = new HashMap<>();
    for (Edge edge : edges) {
      if (nextByPoint.put(edge.from(), edge.to()) != null) {
        return Optional.empty();
      }
    }

    GridPoint start = startPoint(edges);
    List<GridPoint> outline = new ArrayList<>();
    GridPoint current = start;

    do {
      outline.add(current);
      current = nextByPoint.get(current);
      if (current == null || outline.size() > edges.size()) {
        return Optional.empty();
      }
    } while (!current.equals(start));

    if (outline.size() != edges.size()) {
      return Optional.empty();
    }
    return Optional.of(outline);
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

  private static List<Vectore2> toVectors(List<GridPoint> points) {
    List<Vectore2> output = new ArrayList<>();
    for (GridPoint point : points) {
      output.add(new Vectore2(point.x(), point.z()));
    }
    return output;
  }

  private static long gridFloor(double value) {
    return (long) Math.floor(value / SUPERCHUNK_SIZE) * SUPERCHUNK_SIZE;
  }

  private static long gridCeil(double value) {
    return (long) Math.ceil(value / SUPERCHUNK_SIZE) * SUPERCHUNK_SIZE;
  }

  private record Cell(long x, long z) {
    List<Cell> neighbors() {
      return List.of(
          new Cell(x + SUPERCHUNK_SIZE, z),
          new Cell(x - SUPERCHUNK_SIZE, z),
          new Cell(x, z + SUPERCHUNK_SIZE),
          new Cell(x, z - SUPERCHUNK_SIZE));
    }
  }

  private record GridPoint(long x, long z) {}

  private record Edge(GridPoint from, GridPoint to) {}
}
