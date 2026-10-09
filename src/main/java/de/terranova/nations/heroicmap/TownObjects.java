package de.terranova.nations.heroicmap;

import com.nekyia.heroicmap.api.Layer;
import com.nekyia.heroicmap.api.MapObject;
import com.nekyia.heroicmap.api.MapObject.Point;
import com.nekyia.heroicmap.api.Panel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/** Maps a town to the objects of the HeroicMap layers. Free of Bukkit, so tests can load it. */
final class TownObjects {

  static final String MEMBERS_IMAGE = "images/mitglieder.png";
  static final String STATS_IMAGE = "images/statistiken.png";
  static final MapObject.Symbol CASTLE =
      new MapObject.Symbol("images/burg_16.png", "images/burg_9.png");

  /**
   * What the map shows of a town. {@code corners} are the WorldGuard points, inclusive block
   * coordinates; {@code color} is {@code #RRGGBB}; {@code nation}, {@code nationId} and
   * {@code banner} may be null.
   */
  record Town(
      UUID id,
      String name,
      double x,
      double z,
      List<Point> corners,
      String nation,
      UUID nationId,
      boolean capital,
      String color,
      String banner,
      int level,
      int claims,
      int maxClaims,
      String major,
      List<String> vices,
      List<String> council,
      List<Panel.Row> professions) {}

  private TownObjects() {}

  static MapObject.Pin pin(Town t) {
    return MapObject.Pin.at(t.id().toString(), t.x(), t.z())
        .withName(t.name())
        .withSize(t.capital() ? MapObject.Size.LARGE : MapObject.Size.MEDIUM)
        .withSymbol(CASTLE)
        .withColor(t.color())
        .withPanel(panel(t));
  }

  /** The area of the town, or nothing without a WorldGuard region. */
  static List<MapObject> area(Town t) {
    if (t.corners().size() < 3) {
      return List.of();
    }
    List<Point> outer =
        t.corners().stream().map(p -> new Point(edge(p.x()), edge(p.z()))).toList();
    return List.of(
        MapObject.Region.of(t.id().toString(), List.of(MapObject.Polygon.of(outer)))
            .withName(t.name())
            .withFill(t.color() + "55")
            .withStroke(MapObject.Stroke.of(t.color() + "DD")));
  }

  static List<MapObject> circles(Town t) {
    String id = t.id().toString();
    return List.of(
        MapObject.Circle.around(id + "-750", t.x(), t.z(), 750)
            .withStroke(MapObject.Stroke.of("#87A8FADD")),
        MapObject.Circle.around(id + "-2000", t.x(), t.z(), 2000)
            .withFill("#FADE6E44")
            .withStroke(MapObject.Stroke.of("#FA6E6EDD").withDash(10, 10)));
  }

  /**
   * Blocks kept free north of a town for its banner, 40 pixels high at the bottom of the town:
   * a good 3 blocks at the finest zoom.
   */
  static final double BANNER_BLOCKS = 4;

  /**
   * The name of the town in the lettering of the map, level, north of the town. The text is
   * centred on its point, so its lower edge stays {@link #BANNER_BLOCKS} clear of the town.
   */
  static MapObject.Label townName(Town t) {
    double size = townNameSize(t.level());
    Point above = new Point(t.x(), t.z() - BANNER_BLOCKS - size / 2);
    return MapObject.Label.along(t.id().toString(), t.name(), List.of(above))
        .withSize(size)
        .withOutline(new MapObject.Outline(null, 2.0));
  }

  /** Capitals this high in blocks: 12 at level 1, two more per level, at most 30. */
  static double townNameSize(int level) {
    return Math.min(10 + 2 * Math.max(level, 1), 30);
  }

  static final double NATION_SIZE = 40;
  static final double NATION_SPACING = 0.3;

  /** The name of each nation over the middle of its towns, larger and spaced out. */
  static List<MapObject> nationNames(List<Town> towns) {
    Map<UUID, List<Town>> byNation = new LinkedHashMap<>();
    for (Town t : towns) {
      if (t.nationId() != null) {
        byNation.computeIfAbsent(t.nationId(), id -> new ArrayList<>()).add(t);
      }
    }
    List<MapObject> labels = new ArrayList<>();
    for (List<Town> members : byNation.values()) {
      Town first = members.getFirst();
      labels.add(
          MapObject.Label.along("nation-" + first.nationId(), first.nation(), nationPath(members))
              .withSize(NATION_SIZE)
              .withSpacing(NATION_SPACING)
              .withColor(first.color())
              .withOutline(new MapObject.Outline(null, 3.0)));
    }
    return labels;
  }

  /**
   * Where the name of a nation runs. Over a lone town, one point well north of it, clear of the
   * name of the town. Over several, a slight arc through their middle, as long as the two towns
   * farthest apart and in their direction, bulging by a tenth of its length to the side the
   * letters stand on. It runs from west to east, so the letters never stand on their head; due
   * north and south it runs from south to north, like the spine of a book.
   */
  static List<Point> nationPath(List<Town> members) {
    double cx = members.stream().mapToDouble(Town::x).average().orElseThrow();
    double cz = members.stream().mapToDouble(Town::z).average().orElseThrow();
    Town a = members.getFirst();
    Town b = a;
    double longest = 0;
    for (Town p : members) {
      for (Town q : members) {
        double d = Math.hypot(q.x() - p.x(), q.z() - p.z());
        if (d > longest) {
          longest = d;
          a = p;
          b = q;
        }
      }
    }
    if (longest < 1) {
      return List.of(new Point(cx, cz - 2 * NATION_SIZE));
    }
    double dx = (b.x() - a.x()) / longest;
    double dz = (b.z() - a.z()) / longest;
    // due north and south, within rounding: from south to north
    boolean flip = Math.abs(dx) < 1e-9 ? dz > 0 : dx < 0;
    if (flip) {
      dx = -dx;
      dz = -dz;
    }
    // the letters stand on the normal (dz, -dx), never to the south since dx >= 0; the control
    // point lies twice as far out as the apex
    double bulge = longest / 10;
    Point start = new Point(cx - dx * longest / 2, cz - dz * longest / 2);
    Point end = new Point(cx + dx * longest / 2, cz + dz * longest / 2);
    Point control = new Point(cx + dz * 2 * bulge, cz - dx * 2 * bulge);
    List<Point> path = new ArrayList<>();
    for (int i = 0; i <= 8; i++) {
      double t = i / 8.0;
      double u = 1 - t;
      path.add(
          new Point(
              u * u * start.x() + 2 * u * t * control.x() + t * t * end.x(),
              u * u * start.z() + 2 * u * t * control.z() + t * t * end.z()));
    }
    return path;
  }

  /**
   * Claims are cells of 48 blocks, and WorldGuard stores the last block of a cell, 48n + 47. The
   * map wants the corner behind it, 48n + 48.
   */
  static double edge(double v) {
    return Math.floorMod((int) v, 48) == 47 ? v + 1 : v;
  }

  static Panel panel(Town t) {
    List<String> info = new ArrayList<>();
    info.add("Nation: " + (t.nation() == null ? "keine" : t.nation()));
    info.add("Level: " + t.level());
    info.add("Claims: " + t.claims() + "/" + t.maxClaims());
    String title = t.capital() ? "✪ " + t.name() : t.name();
    List<Panel.Block> head = List.of(new Panel.Title(title, t.color()), new Panel.Lines(info));

    List<Panel.Block> blocks = new ArrayList<>();
    if (t.banner() == null) {
      blocks.addAll(head);
    } else {
      blocks.add(new Panel.Columns(head, List.of(new Panel.Image(t.banner(), 44, 80, null))));
    }

    List<String> members = new ArrayList<>();
    members.add("Bürgermeister: " + (t.major() == null ? "-" : t.major()));
    members.addAll(wrap("Vize: ", t.vices()));
    members.addAll(wrap("Rat: ", t.council()));
    blocks.add(
        new Panel.Section(
            Panel.Heading.image(MEMBERS_IMAGE, 200, 50, "Mitglieder"),
            List.of(new Panel.Lines(members))));

    Panel.Block stats =
        t.professions().isEmpty()
            ? Panel.Lines.of("Noch keine Berufe")
            : new Panel.Rating(t.professions());
    blocks.add(
        new Panel.Section(
            Panel.Heading.image(STATS_IMAGE, 200, 50, "Statistiken"), List.of(stats)));
    return new Panel(blocks);
  }

  /** The names after {@code prefix}, comma separated, in lines of at most 120 characters. */
  static List<String> wrap(String prefix, List<String> names) {
    if (names.isEmpty()) {
      return List.of(prefix + "-");
    }
    List<String> lines = new ArrayList<>();
    String line = prefix + names.getFirst();
    for (String name : names.subList(1, names.size())) {
      if (line.length() + 2 + name.length() > 120) {
        lines.add(line);
        line = name;
      } else {
        line += ", " + name;
      }
    }
    lines.add(line);
    return lines;
  }

  /** A layer and what Nations last put there, so that only changes go out. */
  static final class Synced {
    final Layer layer;
    private final Logger log;
    private Map<String, MapObject> shown = new HashMap<>();

    Synced(Layer layer, Logger log) {
      this.layer = layer;
      this.log = log;
    }

    /**
     * Puts every object that changed and removes every one no town has any more. An object the
     * layer refuses is logged; the layer keeps its old one, and the next sync tries again.
     */
    void sync(List<? extends MapObject> objects) {
      Map<String, MapObject> next = new HashMap<>();
      for (MapObject o : objects) {
        MapObject old = shown.get(o.id());
        if (!o.equals(old)) {
          try {
            layer.put(o);
          } catch (IllegalArgumentException e) {
            log.warning("HeroicMap: " + layer.id() + " " + o.id() + ": " + e.getMessage());
            if (old != null) {
              next.put(o.id(), old);
            }
            continue;
          }
        }
        next.put(o.id(), o);
      }
      for (String id : shown.keySet()) {
        if (!next.containsKey(id)) {
          layer.remove(id);
        }
      }
      shown = next;
    }
  }
}
