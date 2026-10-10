package de.terranova.nations.heroicmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nekyia.heroicmap.api.Layer;
import com.nekyia.heroicmap.api.MapObject;
import com.nekyia.heroicmap.api.MapObject.Point;
import com.nekyia.heroicmap.api.Panel;
import de.terranova.nations.heroicmap.TownObjects.Synced;
import de.terranova.nations.heroicmap.TownObjects.Town;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class TownObjectsTest {

  private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000017");
  private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000018");
  private static final UUID NATION = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

  private static Town town(UUID id, boolean capital, String banner, List<Point> corners) {
    return new Town(
        id,
        "Hafen Stadt",
        120.5,
        -340.5,
        corners,
        "Nordreich",
        NATION,
        capital,
        "#AABBCC",
        banner,
        3,
        12,
        40,
        "Anna",
        List.of("Ben"),
        List.of("Cara", "Dario"),
        List.of(new Panel.Row("Bergbau", 2, 4, "#7F8C8D")));
  }

  /** A town of the nation at x, z. */
  private static Town member(int n, double x, double z) {
    return member(n, x, z, NATION, "Nordreich");
  }

  /** A town of any nation, or of none with null. */
  private static Town member(int n, double x, double z, UUID nation, String nationName) {
    return new Town(
        new UUID(0, n),
        "Stadt " + n,
        x,
        z,
        List.of(),
        nationName,
        nation,
        false,
        "#AABBCC",
        null,
        1,
        1,
        20,
        null,
        List.of(),
        List.of(),
        List.of());
  }

  private static List<Point> square(int x, int z) {
    return List.of(
        new Point(x, z), new Point(x + 47, z), new Point(x + 47, z + 47), new Point(x, z + 47));
  }

  @Test
  void bannerOfACapital() {
    MapObject.Banner banner =
        TownObjects.banner(town(ID, true, "images/banner-n.png", square(0, 0)));

    assertEquals(ID.toString(), banner.id());
    assertEquals(new Point(120.5, -340.5), banner.at());
    assertEquals("images/banner-n.png", banner.image());
    // ✪ marks the capital in the panel only; the lettering of the map has no ✪
    assertEquals("Hafen Stadt", banner.name());

    List<Panel.Block> blocks = banner.panel().blocks();
    assertEquals(
        new Panel.Columns(
            List.of(
                new Panel.Title("✪ Hafen Stadt", "#AABBCC"),
                Panel.Lines.of("Nation: Nordreich", "Level: 3", "Claims: 12/40")),
            List.of(new Panel.Image("images/banner-n.png", 40, 80, null))),
        blocks.get(0));
    assertEquals(
        new Panel.Section(
            Panel.Heading.image("images/mitglieder.png", 200, 50, "Mitglieder"),
            List.of(Panel.Lines.of("Bürgermeister: Anna", "Vize: Ben", "Rat: Cara, Dario"))),
        blocks.get(1));
    assertEquals(
        new Panel.Section(
            Panel.Heading.image("images/statistiken.png", 200, 50, "Statistiken"),
            List.of(new Panel.Rating(List.of(new Panel.Row("Bergbau", 2, 4, "#7F8C8D"))))),
        blocks.get(2));
  }

  @Test
  void aTownWithoutANationFliesAWhiteBanner() {
    MapObject.Banner banner = TownObjects.banner(member(1, 0, 0, null, null));

    assertEquals("images/banner-white.png", banner.image());
    assertEquals("Stadt 1", banner.name());
  }

  @Test
  void aNationWithoutBannerFliesAWhiteOne() {
    MapObject.Banner banner = TownObjects.banner(town(ID, false, null, square(0, 0)));

    assertEquals("images/banner-white.png", banner.image());
    List<Panel.Block> blocks = banner.panel().blocks();
    assertEquals(new Panel.Title("Hafen Stadt", "#AABBCC"), blocks.get(0));
    assertEquals(Panel.Lines.of("Nation: Nordreich", "Level: 3", "Claims: 12/40"), blocks.get(1));
  }

  @Test
  void areaEndsOnTheCornersOfTheBlocks() {
    MapObject.Region area =
        (MapObject.Region) TownObjects.area(town(ID, false, null, square(0, 0))).getFirst();

    assertEquals(
        List.of(new Point(0, 0), new Point(48, 0), new Point(48, 48), new Point(0, 48)),
        area.polygons().getFirst().outer());
    assertEquals("#AABBCC55", area.fill());
    assertEquals(MapObject.Stroke.of("#AABBCCDD"), area.stroke());
  }

  @Test
  void areaWithNegativeCoordinates() {
    MapObject.Region area =
        (MapObject.Region) TownObjects.area(town(ID, false, null, square(-96, -48))).getFirst();

    assertEquals(
        List.of(new Point(-96, -48), new Point(-48, -48), new Point(-48, 0), new Point(-96, 0)),
        area.polygons().getFirst().outer());
  }

  @Test
  void areaOfAnLShapedTownFromClaimCalc() {
    // The founding cell as createGridClaim stores it, then two cells claimed as /town claim does
    List<Vectore2> claim =
        List.of(new Vectore2(0, 0), new Vectore2(47, 0), new Vectore2(47, 47), new Vectore2(0, 47));
    claim = addCell(claim, 48, 0);
    claim = addCell(claim, 0, 48);
    List<Point> corners = claim.stream().map(v -> new Point(v.x, v.z)).toList();

    MapObject.Region area =
        (MapObject.Region) TownObjects.area(town(ID, false, null, corners)).getFirst();

    assertRing(
        List.of(
            new Point(0, 0),
            new Point(96, 0),
            new Point(96, 48),
            new Point(48, 48),
            new Point(48, 96),
            new Point(0, 96)),
        area.polygons().getFirst().outer());
  }

  /** Like /town claim for a player in the cell at x, z. */
  private static List<Vectore2> addCell(List<Vectore2> claim, int x, int z) {
    claimCalc.Change change =
        claimCalc.claim(claimCalc.cellsOf(claim), claimCalc.Cell.at(x, z), Integer.MAX_VALUE);
    assertEquals(null, change.refusal());
    return change.points();
  }

  /** The same points in the same cyclic order, in either direction. */
  private static void assertRing(List<Point> expected, List<Point> actual) {
    assertEquals(expected.size(), actual.size(), actual.toString());
    int start = actual.indexOf(expected.getFirst());
    assertTrue(start >= 0, actual.toString());
    List<Point> forward = new ArrayList<>();
    List<Point> backward = new ArrayList<>();
    for (int i = 0; i < actual.size(); i++) {
      forward.add(actual.get((start + i) % actual.size()));
      backward.add(actual.get(Math.floorMod(start - i, actual.size())));
    }
    assertTrue(forward.equals(expected) || backward.equals(expected), actual.toString());
  }

  @Test
  void noAreaWithoutRegion() {
    assertEquals(List.of(), TownObjects.area(town(ID, false, null, List.of())));
  }

  @Test
  void twoCircles() {
    List<MapObject> circles = TownObjects.circles(town(ID, false, null, square(0, 0)));

    MapObject.Circle near = (MapObject.Circle) circles.get(0);
    MapObject.Circle far = (MapObject.Circle) circles.get(1);
    assertEquals(ID + "-750", near.id());
    assertEquals(750, near.radius());
    assertEquals(null, near.fill());
    assertEquals(ID + "-2000", far.id());
    assertEquals(2000, far.radius());
    assertEquals(new Point(120.5, -340.5), far.center());
    assertEquals(MapObject.Style.DASHED, far.stroke().style());
  }

  @Test
  void longListsOfNamesWrap() {
    List<String> names = Collections.nCopies(30, "Spielername16abc");

    List<String> lines = TownObjects.wrap("Rat: ", names);

    assertTrue(lines.size() > 1);
    assertTrue(lines.stream().allMatch(l -> l.length() <= 120), lines.toString());
    String joined = String.join(", ", lines);
    assertEquals("Rat: " + String.join(", ", names), joined);
  }

  @Test
  void syncRemovesDeletedTownsAndPutsOnlyChanges() {
    FakeLayer layer = new FakeLayer();
    Synced synced = new Synced(layer, Logger.getAnonymousLogger());
    Town a = town(ID, false, null, square(0, 0));
    Town b = town(OTHER, false, null, square(96, 0));

    synced.sync(objects(a, b));
    assertEquals(6, layer.objects.size());

    layer.puts.clear();
    synced.sync(objects(a));

    assertEquals(List.of(), layer.puts, "unchanged objects go out again");
    assertEquals(
        objects(a).stream().map(MapObject::id).toList(), List.copyOf(layer.objects.keySet()));
  }

  @Test
  void nameOfANationWithOneTown() {
    MapObject.Label label =
        (MapObject.Label) TownObjects.nationNames(List.of(member(1, 100, 200))).getFirst();

    assertEquals("nation-" + NATION, label.id());
    assertEquals("Nordreich", label.text());
    assertEquals(List.of(new Point(100, 120)), label.path());
    assertEquals(40.0, label.size());
    assertEquals(0.3, label.spacing());
    assertEquals(TownObjects.letteringColor("#AABBCC"), label.color());
    assertEquals(new MapObject.Outline(null, 3.0), label.outline());
  }

  @Test
  void namesOfTheNationsOnly() {
    UUID a = new UUID(1, 0xa);
    UUID b = new UUID(1, 0xb);
    List<MapObject> labels =
        TownObjects.nationNames(
            List.of(
                member(1, 0, 0, a, "Nordreich"),
                member(2, 3000, 0, null, null),
                member(3, 0, 5000, b, "Westmark"),
                member(4, 2000, 0, a, "Nordreich")));

    assertEquals(2, labels.size());
    MapObject.Label first = (MapObject.Label) labels.get(0);
    MapObject.Label second = (MapObject.Label) labels.get(1);
    assertEquals("nation-" + a, first.id());
    assertEquals("Nordreich", first.text());
    assertEquals(List.of(new Point(0, -80), new Point(2000, -80)), first.path());
    assertEquals("nation-" + b, second.id());
    assertEquals("Westmark", second.text());
    assertEquals(new MapObject.Outline(null, 3.0), first.outline());
    assertEquals(new MapObject.Outline(null, 3.0), second.outline());
  }

  @Test
  void aNationWithoutANameGetsNone() {
    UUID a = new UUID(1, 0xa);
    assertEquals(
        List.of(),
        TownObjects.nationNames(
            List.of(member(1, 0, 0, a, null), member(2, 96, 0, new UUID(1, 0xb), " "))));
  }

  @Test
  void nameOfANationAlongItsLongestExtent() {
    List<Point> path =
        TownObjects.nationPath(
            List.of(member(1, 1000, 0), member(2, 500, 60), member(3, 0, 0)));

    // a straight line through the middle (500, 20), 80 north, from town 1 to town 3
    assertEquals(List.of(new Point(1000, -60), new Point(0, -60)), path);
  }

  @Test
  void ofPairsEquallyFarApartTheIdsDecide() {
    List<Town> square =
        List.of(member(1, 0, 0), member(2, 1000, 0), member(3, 0, 1000), member(4, 1000, 1000));
    List<Town> shuffled = List.of(square.get(3), square.get(1), square.get(2), square.get(0));

    List<Point> path = TownObjects.nationPath(square);

    assertEquals(List.of(new Point(0, -80), new Point(1000, 920)), path);
    assertEquals(path, TownObjects.nationPath(shuffled));
  }

  @Test
  void theNameOfANationLiesClearOfItsTowns() {
    // three towns in a row: the middle of the line would lie on the middle town
    assertEquals(
        List.of(new Point(0, -80), new Point(2000, -80)),
        TownObjects.nationPath(List.of(member(1, 0, 0), member(2, 1000, 0), member(3, 2000, 0))));
  }

  @Test
  void townsOfANationAtOnePlaceAreLikeOne() {
    assertEquals(
        List.of(new Point(10, -70)),
        TownObjects.nationPath(List.of(member(1, 10, 10), member(2, 10, 10))));
  }

  /** In every hue of ColorUtils the lettering reads on the contour #F2E8D0 at 4:1 or better. */
  @Test
  void letteringReadsInEveryHue() {
    double contour = luminance(0xF2E8D0);
    for (int hue = 0; hue < 360; hue++) {
      int rgb = java.awt.Color.HSBtoRGB(hue / 360f, 0.6f, 0.8f) & 0xFFFFFF;
      String lettering = TownObjects.letteringColor(String.format("#%06X", rgb));
      double l = luminance(Integer.parseInt(lettering.substring(1), 16));
      double contrast = (contour + 0.05) / (l + 0.05);
      assertTrue(contrast >= 4, "hue " + hue + ": " + lettering + " only " + contrast);
    }
  }

  /** Relative luminance as in WCAG 2. */
  private static double luminance(int rgb) {
    double[] c = {rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF};
    for (int i = 0; i < 3; i++) {
      double v = c[i] / 255;
      c[i] = v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
  }

  @Test
  void displayNameOfANation() {
    assertEquals("Nord reich", TownObjects.displayName("nord_reich"));
    assertEquals("Äpfelland", TownObjects.displayName("äpfelland"));
    assertEquals(64, TownObjects.displayName("a".repeat(70)).length());
    assertEquals(null, TownObjects.displayName(null));
  }

  @Test
  void theNameOfANationGoesWithIt() {
    FakeLayer layer = new FakeLayer();
    Synced synced = new Synced(layer, Logger.getAnonymousLogger());
    synced.sync(TownObjects.nationNames(List.of(member(1, 0, 0), member(2, 96, 0))));
    assertEquals(List.of("nation-" + NATION), List.copyOf(layer.objects.keySet()));

    synced.sync(TownObjects.nationNames(List.of()));

    assertEquals(List.of(), List.copyOf(layer.objects.keySet()));
  }

  @Test
  void aChangedObjectGoesOutOnce() {
    FakeLayer layer = new FakeLayer();
    Synced synced = new Synced(layer, Logger.getAnonymousLogger());
    MapObject.Banner a = TownObjects.banner(town(ID, false, null, square(0, 0)));
    MapObject.Banner b = TownObjects.banner(town(OTHER, false, null, square(96, 0)));
    synced.sync(List.of(a, b));
    layer.puts.clear();

    synced.sync(List.of(a.withName("Neu"), b));

    assertEquals(List.of(a.id()), layer.puts);
  }

  @Test
  void aRefusedObjectStaysAndGoesOutAgain() {
    FakeLayer layer = new FakeLayer();
    Synced synced = new Synced(layer, Logger.getAnonymousLogger());
    MapObject.Banner old = TownObjects.banner(town(ID, false, null, square(0, 0)));
    MapObject.Banner changed = old.withName("Neu");
    synced.sync(List.of(old));

    layer.refused.add(old.id());
    synced.sync(List.of(changed));

    assertEquals(old, layer.objects.get(old.id()), "the layer keeps the old one");

    layer.refused.clear();
    layer.puts.clear();
    synced.sync(List.of(changed));

    assertEquals(List.of(old.id()), layer.puts);
    assertEquals(changed, layer.objects.get(old.id()));
  }

  /** Banner and circles; in Nations the area has a layer of its own, with the id of the banner. */
  private static List<MapObject> objects(Town... towns) {
    return Stream.of(towns)
        .flatMap(
            t -> Stream.concat(Stream.of(TownObjects.banner(t)), TownObjects.circles(t).stream()))
        .toList();
  }

  /** Keeps what Nations puts, like the layer of HeroicMap; refuses the ids in {@code refused}. */
  private static final class FakeLayer implements Layer {
    final Map<String, MapObject> objects = new LinkedHashMap<>();
    final List<String> puts = new ArrayList<>();
    final Set<String> refused = new HashSet<>();

    @Override
    public String id() {
      return "nations:test";
    }

    @Override
    public void name(String de, String en) {}

    @Override
    public void visible(boolean visible) {}

    @Override
    public void order(int order) {}

    @Override
    public void web(boolean web) {}

    @Override
    public void permission(String permission) {}

    @Override
    public void image(String path, byte[] data) {}

    @Override
    public void removeImage(String path) {}

    @Override
    public void put(MapObject object) {
      if (refused.contains(object.id())) {
        throw new IllegalArgumentException("abgelehnt");
      }
      puts.add(object.id());
      objects.put(object.id(), object);
    }

    @Override
    public void remove(String id) {
      objects.remove(id);
    }

    @Override
    public void clear() {
      objects.clear();
    }

    @Override
    public void delete() {}
  }
}
