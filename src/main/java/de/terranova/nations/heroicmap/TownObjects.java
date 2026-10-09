package de.terranova.nations.heroicmap;

import com.nekyia.heroicmap.api.Layer;
import com.nekyia.heroicmap.api.MapObject;
import com.nekyia.heroicmap.api.MapObject.Point;
import com.nekyia.heroicmap.api.Panel;
import java.util.ArrayList;
import java.util.HashMap;
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
   * coordinates; {@code color} is {@code #RRGGBB}; {@code nation} and {@code banner} may be null.
   */
  record Town(
      UUID id,
      String name,
      double x,
      double z,
      List<Point> corners,
      String nation,
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
