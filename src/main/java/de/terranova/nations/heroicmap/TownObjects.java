package de.terranova.nations.heroicmap;

import com.nekyia.heroicmap.api.Layer;
import com.nekyia.heroicmap.api.MapObject;
import com.nekyia.heroicmap.api.MapObject.Point;
import com.nekyia.heroicmap.api.Panel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/** Maps a town to the objects of the HeroicMap layers. Free of Bukkit, so tests can load it. */
final class TownObjects {

  static final String MEMBERS_IMAGE = "images/mitglieder.png";
  static final String STATS_IMAGE = "images/statistiken.png";
  static final String WHITE_BANNER = "images/banner-white.png";

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

  /**
   * The town as the banner of its nation, a white one without a nation or a banner, with its name
   * and panel. The name stays plain: ✪ marks a capital in the title of the panel only.
   */
  static MapObject.Banner banner(Town t) {
    String image = t.banner() == null ? WHITE_BANNER : t.banner();
    return MapObject.Banner.at(t.id().toString(), t.x(), t.z(), image)
        .withName(t.name())
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

  static final double NATION_SIZE = 40;
  static final double NATION_SPACING = 0.3;

  /**
   * The name of each nation over the middle of its towns, larger and spaced out. Towns are named
   * by their pins; this lettering holds the nations only. A nation without a name gets none.
   */
  static List<MapObject> nationNames(List<Town> towns) {
    Map<UUID, List<Town>> byNation = new LinkedHashMap<>();
    for (Town t : towns) {
      if (t.nationId() != null && t.nation() != null && !t.nation().isBlank()) {
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
              .withColor(letteringColor(first.color()))
              .withOutline(new MapObject.Outline(null, 3.0)));
    }
    return labels;
  }

  /**
   * Where the name of a nation runs, always {@code 2 * NATION_SIZE} north, so that it lies clear of
   * the towns and their pins. Over a lone town, one point. Over several, a straight line through
   * their middle, as long as the two towns farthest apart and in their direction; the view turns
   * the letters upright. Of pairs equally far apart, the first by the ids of the towns counts.
   */
  static List<Point> nationPath(List<Town> members) {
    List<Town> byId = members.stream().sorted(Comparator.comparing(Town::id)).toList();
    double cx = byId.stream().mapToDouble(Town::x).average().orElseThrow();
    double cz = byId.stream().mapToDouble(Town::z).average().orElseThrow();
    Town a = byId.getFirst();
    Town b = a;
    double longest = 0;
    for (Town p : byId) {
      for (Town q : byId) {
        double d = Math.hypot(q.x() - p.x(), q.z() - p.z());
        if (d > longest) {
          longest = d;
          a = p;
          b = q;
        }
      }
    }
    cz -= 2 * NATION_SIZE;
    if (longest < 1) {
      return List.of(new Point(cx, cz));
    }
    double hx = (b.x() - a.x()) / 2;
    double hz = (b.z() - a.z()) / 2;
    return List.of(new Point(cx - hx, cz - hz), new Point(cx + hx, cz + hz));
  }

  /**
   * The colour of the lettering of a nation: its hue and saturation at brightness 0.45, so it
   * reads on the light contour of the map, {@code #F2E8D0}, in every hue.
   */
  static String letteringColor(String color) {
    int rgb = Integer.parseInt(color.substring(1, 7), 16);
    float[] hsb = Color.RGBtoHSB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, null);
    return String.format("#%06X", Color.HSBtoRGB(hsb[0], hsb[1], 0.45f) & 0xFFFFFF);
  }

  /**
   * The name of a nation as shown: {@code _} as spaces, the first letter a capital, at most 64
   * characters; null stays null.
   */
  static String displayName(String raw) {
    if (raw == null || raw.isEmpty()) {
      return raw;
    }
    String name = raw.replace('_', ' ');
    int first = name.offsetByCodePoints(0, 1);
    name = name.substring(0, first).toUpperCase(Locale.ROOT) + name.substring(first);
    if (name.codePointCount(0, name.length()) > 64) {
      name = name.substring(0, name.offsetByCodePoints(0, 64));
    }
    return name;
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
