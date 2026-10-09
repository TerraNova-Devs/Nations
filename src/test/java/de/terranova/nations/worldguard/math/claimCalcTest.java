package de.terranova.nations.worldguard.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.terranova.nations.worldguard.math.claimCalc.Cell;
import de.terranova.nations.worldguard.math.claimCalc.Change;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class claimCalcTest {

  private static final Cell HOME = new Cell(0, 0);

  /** Cells by their column and row, in steps of 48 blocks. */
  private static Set<Cell> cells(int[]... colRow) {
    Set<Cell> cells = new HashSet<>();
    for (int[] c : colRow) {
      cells.add(new Cell(c[0] * 48L, c[1] * 48L));
    }
    return cells;
  }

  private static List<Vectore2> points(Set<Cell> cells) {
    Change change = claimCalc.outline(cells);
    assertNull(change.refusal(), cells.toString());
    return change.points();
  }

  /** Vectore2 has no equals(Object); compare as text, in order. */
  private static List<String> text(List<Vectore2> points) {
    return points.stream().map(v -> "(" + (long) v.x + "," + (long) v.z + ")").toList();
  }

  private static List<String> text(String... points) {
    return List.of(points);
  }

  @Test
  void foundingInEitherDirectionIsOneCell() {
    // as createGridClaim: nw, ne, se, sw
    List<Vectore2> clockwise =
        List.of(
            new Vectore2(0, 0), new Vectore2(47, 0), new Vectore2(47, 47), new Vectore2(0, 47));

    assertEquals(Set.of(HOME), claimCalc.cellsOf(clockwise));
    assertEquals(Set.of(HOME), claimCalc.cellsOf(clockwise.reversed()));
    assertEquals(text("(0,0)", "(47,0)", "(47,47)", "(0,47)"), text(points(Set.of(HOME))));
  }

  @Test
  void claimAddsTheCellNextToTheFounding() {
    List<Vectore2> founding =
        List.of(
            new Vectore2(0, 0), new Vectore2(47, 0), new Vectore2(47, 47), new Vectore2(0, 47));

    Change change = claimCalc.claim(founding.reversed(), new Cell(48, 0));

    assertEquals(text("(0,0)", "(95,0)", "(95,47)", "(0,47)"), text(change.points()));
  }

  @Test
  void concaveCornersLieOnTheBlockInside() {
    // L: the corner at 48,48 is block 47,47; block 48,48 belongs to the free cell
    assertEquals(
        text("(0,0)", "(95,0)", "(95,47)", "(47,47)", "(47,95)", "(0,95)"),
        text(points(cells(new int[] {0, 0}, new int[] {1, 0}, new int[] {0, 1}))));
    // U open to the south: the corners at 48,48 and 96,48
    assertEquals(
        text("(0,0)", "(143,0)", "(143,95)", "(96,95)", "(96,47)", "(47,47)", "(47,95)", "(0,95)"),
        text(
            points(
                cells(
                    new int[] {0, 0},
                    new int[] {1, 0},
                    new int[] {2, 0},
                    new int[] {0, 1},
                    new int[] {2, 1}))));
  }

  @Test
  void negativeCoordinates() {
    assertEquals(
        text("(-96,-48)", "(-1,-48)", "(-1,-1)", "(-96,-1)"),
        text(points(cells(new int[] {-2, -1}, new int[] {-1, -1}))));
    List<Vectore2> points =
        List.of(
            new Vectore2(-96, -48),
            new Vectore2(-1, -48),
            new Vectore2(-1, -1),
            new Vectore2(-96, -1));
    assertEquals(Set.of(new Cell(-96, -48), new Cell(-48, -48)), claimCalc.cellsOf(points));
  }

  @Test
  void oldConcaveCornersAreReadAsTheSameCells() {
    // what claimCalc wrote before: 48,48 instead of 47,47, with slanted edges
    List<Vectore2> old =
        List.of(
            new Vectore2(0, 0),
            new Vectore2(95, 0),
            new Vectore2(95, 47),
            new Vectore2(48, 48),
            new Vectore2(47, 95),
            new Vectore2(0, 95));

    assertEquals(
        cells(new int[] {0, 0}, new int[] {1, 0}, new int[] {0, 1}), claimCalc.cellsOf(old));
  }

  /** Claim and unclaim of every cell at the border of each shape give back the old points. */
  @Test
  void unclaimUndoesClaimPointForPoint() {
    List<Set<Cell>> shapes =
        List.of(
            cells(new int[] {0, 0}),
            cells(new int[] {0, 0}, new int[] {1, 0}),
            cells(new int[] {0, 0}, new int[] {1, 0}, new int[] {0, 1}),
            cells(
                new int[] {0, 0},
                new int[] {1, 0},
                new int[] {2, 0},
                new int[] {0, 1},
                new int[] {2, 1}),
            cells(new int[] {0, 0}, new int[] {1, 0}, new int[] {2, 0}),
            cells(new int[] {0, 0}, new int[] {-1, 0}, new int[] {-1, -1}, new int[] {0, -1}));
    int checked = 0;
    for (Set<Cell> shape : shapes) {
      List<Vectore2> before = points(shape);
      for (Cell cell : around(shape)) {
        Change claimed = claimCalc.claim(before, cell);
        if (claimed.refusal() == null) {
          Change back = claimCalc.unclaim(claimed.points(), cell, HOME);
          assertEquals(text(before), text(back.points()), shape + " + " + cell);
          checked++;
        }
      }
      for (Cell cell : shape) {
        Change unclaimed = claimCalc.unclaim(before, cell, HOME);
        if (unclaimed.refusal() == null) {
          Change back = claimCalc.claim(unclaimed.points(), cell);
          assertEquals(text(before), text(back.points()), shape + " - " + cell);
          checked++;
        }
      }
    }
    assertTrue(checked > 30, "only " + checked + " round trips");
  }

  /** The cells around a shape, diagonals included. */
  private static Set<Cell> around(Set<Cell> shape) {
    Set<Cell> around = new HashSet<>();
    for (Cell c : shape) {
      for (long dx = -48; dx <= 48; dx += 48) {
        for (long dz = -48; dz <= 48; dz += 48) {
          around.add(new Cell(c.x() + dx, c.z() + dz));
        }
      }
    }
    around.removeAll(shape);
    return around;
  }

  @Test
  void refusedClaims() {
    List<Vectore2> row = points(cells(new int[] {0, 0}, new int[] {1, 0}));

    assertEquals(claimCalc.ALREADY_CLAIMED, claimCalc.claim(row, new Cell(48, 0)).refusal());
    // not next to the town: the old claimCalc said "erweitert" and changed nothing
    assertEquals(claimCalc.NOT_CONNECTED, claimCalc.claim(row, new Cell(144, 0)).refusal());
    assertEquals(claimCalc.NOT_CONNECTED, claimCalc.claim(row, new Cell(96, 48)).refusal());

    // closing a ring leaves a hole
    List<Vectore2> ring =
        points(
            cells(
                new int[] {0, 0},
                new int[] {1, 0},
                new int[] {2, 0},
                new int[] {2, 1},
                new int[] {2, 2},
                new int[] {1, 2},
                new int[] {0, 2}));
    assertEquals(claimCalc.NO_SINGLE_OUTLINE, claimCalc.claim(ring, new Cell(0, 48)).refusal());

    // touching the town also at a corner only, at 96,48
    List<Vectore2> hook =
        points(
            cells(
                new int[] {0, 0},
                new int[] {1, 0},
                new int[] {0, 1},
                new int[] {0, 2},
                new int[] {1, 2},
                new int[] {2, 2}));
    assertEquals(claimCalc.NO_SINGLE_OUTLINE, claimCalc.claim(hook, new Cell(96, 48)).refusal());
  }

  @Test
  void refusedUnclaims() {
    List<Vectore2> row = points(cells(new int[] {0, 0}, new int[] {1, 0}, new int[] {2, 0}));
    assertEquals(claimCalc.NOT_CLAIMED, claimCalc.unclaim(row, new Cell(0, 48), HOME).refusal());
    assertEquals(claimCalc.FOUNDING_CELL, claimCalc.unclaim(row, HOME, HOME).refusal());
    // splitting the town: the old claimCalc wrote a broken ring
    assertEquals(
        claimCalc.NOT_CONNECTED, claimCalc.unclaim(row, new Cell(48, 0), HOME).refusal());

    // only a corner left between the two parts
    List<Vectore2> step = points(cells(new int[] {0, 0}, new int[] {1, 0}, new int[] {1, 1}));
    assertEquals(
        claimCalc.NOT_CONNECTED, claimCalc.unclaim(step, new Cell(48, 0), HOME).refusal());

    // a hole in the middle
    Set<Cell> square = new HashSet<>();
    for (int x = 0; x < 3; x++) {
      for (int z = 0; z < 3; z++) {
        square.addAll(cells(new int[] {x, z}));
      }
    }
    assertEquals(
        claimCalc.NO_SINGLE_OUTLINE,
        claimCalc.unclaim(points(square), new Cell(48, 48), HOME).refusal());

    // the last cell is the founding cell
    assertEquals(
        claimCalc.FOUNDING_CELL, claimCalc.unclaim(points(Set.of(HOME)), HOME, HOME).refusal());
  }

  /**
   * Random claims and unclaims around the founding cell. After each change the points hold
   * exactly the expected cells, every edge is straight, and toggling back gives the old points;
   * each refusal has its reason.
   */
  @Test
  void randomClaimsAndUnclaims() {
    Random random = new Random(1);
    int changes = 0;
    int refusals = 0;
    for (int run = 0; run < 200; run++) {
      Set<Cell> cells = new HashSet<>(Set.of(HOME));
      List<Vectore2> points = points(cells);
      for (int step = 0; step < 15; step++) {
        Cell cell = new Cell((random.nextInt(5) - 2) * 48L, (random.nextInt(5) - 2) * 48L);
        boolean unclaim = cells.contains(cell);
        Change change =
            unclaim ? claimCalc.unclaim(points, cell, HOME) : claimCalc.claim(points, cell);
        Set<Cell> want = new HashSet<>(cells);
        if (unclaim) {
          want.remove(cell);
        } else {
          want.add(cell);
        }

        if (change.refusal() != null) {
          refusals++;
          assertEquals(expectedRefusal(want, cell, unclaim), change.refusal(), want + " " + cell);
          continue;
        }
        assertNull(expectedRefusal(want, cell, unclaim), want.toString());
        assertEquals(want, claimCalc.cellsOf(change.points()));
        assertStraight(change.points());
        Change back =
            unclaim
                ? claimCalc.claim(change.points(), cell)
                : claimCalc.unclaim(change.points(), cell, HOME);
        assertEquals(text(points), text(back.points()));
        cells = want;
        points = change.points();
        changes++;
      }
    }
    assertTrue(changes > 500 && refusals > 500, changes + " changes, " + refusals + " refusals");
  }

  /** Why a change to {@code want} must be refused, worked out on the cells alone; null if not. */
  private static String expectedRefusal(Set<Cell> want, Cell cell, boolean unclaim) {
    if (unclaim && cell.equals(HOME)) {
      return claimCalc.FOUNDING_CELL;
    }
    if (!connected(want)) {
      return claimCalc.NOT_CONNECTED;
    }
    if (hasHole(want) || touchesAtACorner(want)) {
      return claimCalc.NO_SINGLE_OUTLINE;
    }
    return null;
  }

  private static boolean connected(Set<Cell> cells) {
    if (cells.isEmpty()) {
      return false;
    }
    Set<Cell> seen = new HashSet<>();
    Queue<Cell> queue = new ArrayDeque<>(List.of(cells.iterator().next()));
    seen.addAll(queue);
    while (!queue.isEmpty()) {
      Cell c = queue.remove();
      for (Cell n : c.neighbors()) {
        if (cells.contains(n) && seen.add(n)) {
          queue.add(n);
        }
      }
    }
    return seen.size() == cells.size();
  }

  /** A free cell that cannot reach the border of the bounding box around the cells. */
  private static boolean hasHole(Set<Cell> cells) {
    long minX = cells.stream().mapToLong(Cell::x).min().orElseThrow() - 48;
    long maxX = cells.stream().mapToLong(Cell::x).max().orElseThrow() + 48;
    long minZ = cells.stream().mapToLong(Cell::z).min().orElseThrow() - 48;
    long maxZ = cells.stream().mapToLong(Cell::z).max().orElseThrow() + 48;
    Set<Cell> outside = new HashSet<>();
    Queue<Cell> queue = new ArrayDeque<>(List.of(new Cell(minX, minZ)));
    outside.addAll(queue);
    while (!queue.isEmpty()) {
      Cell c = queue.remove();
      for (Cell n : c.neighbors()) {
        boolean inBox = n.x() >= minX && n.x() <= maxX && n.z() >= minZ && n.z() <= maxZ;
        if (inBox && !cells.contains(n) && outside.add(n)) {
          queue.add(n);
        }
      }
    }
    long free = (maxX - minX) / 48 + 1;
    free = free * ((maxZ - minZ) / 48 + 1) - cells.size();
    return outside.size() != free;
  }

  /** Two cells meeting only at a corner, with neither of the other two cells there. */
  private static boolean touchesAtACorner(Set<Cell> cells) {
    for (Cell c : cells) {
      for (long dx : new long[] {-48, 48}) {
        Cell diagonal = new Cell(c.x() + dx, c.z() + 48);
        boolean side1 = cells.contains(new Cell(c.x() + dx, c.z()));
        boolean side2 = cells.contains(new Cell(c.x(), c.z() + 48));
        if (cells.contains(diagonal) && !side1 && !side2) {
          return true;
        }
      }
    }
    return false;
  }

  private static void assertStraight(List<Vectore2> points) {
    List<Vectore2> ring = new ArrayList<>(points);
    for (int i = 0; i < ring.size(); i++) {
      Vectore2 a = ring.get(i);
      Vectore2 b = ring.get((i + 1) % ring.size());
      assertTrue(a.x == b.x || a.z == b.z, "slanted edge " + text(List.of(a, b)));
    }
  }
}
