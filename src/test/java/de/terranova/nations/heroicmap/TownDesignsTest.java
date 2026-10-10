package de.terranova.nations.heroicmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.nekyia.heroicmap.api.BannerDesign;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.junit.jupiter.api.Test;

class TownDesignsTest {

  @Test
  void theBaseIsTheDyeOfTheBannerItem() {
    for (DyeColor dye : DyeColor.values()) {
      assertEquals(dye, TownDesigns.designOf(dye.name() + "_BANNER", List.of()).base());
    }
    assertEquals(DyeColor.LIGHT_BLUE, TownDesigns.designOf("LIGHT_BLUE_BANNER", List.of()).base());
  }

  @Test
  void noBannerItemNoDesign() {
    assertNull(TownDesigns.designOf("WHITE_WOOL", List.of()));
    // the block on a wall, not an item a nation holds
    assertNull(TownDesigns.designOf("RED_WALL_BANNER", List.of()));
    assertNull(TownDesigns.designOf("AIR", List.of()));
  }

  @Test
  void theLayersFromBottomToTop() {
    BannerDesign design =
        TownDesigns.designOf(
            "BLACK_BANNER",
            List.of(
                new BannerDesign.Pattern("minecraft:creeper", DyeColor.GREEN),
                new BannerDesign.Pattern("minecraft:border", DyeColor.LIME)));

    assertEquals(
        BannerDesign.of(DyeColor.BLACK)
            .with("minecraft:creeper", DyeColor.GREEN)
            .with("minecraft:border", DyeColor.LIME),
        design);
  }

  @Test
  void ofMoreThanSixteenLayersTheFirstSixteenCount() {
    List<BannerDesign.Pattern> layers = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      layers.add(new BannerDesign.Pattern("minecraft:pattern_" + i, DyeColor.BLACK));
    }

    List<BannerDesign.Pattern> kept = TownDesigns.designOf("WHITE_BANNER", layers).layers();

    assertEquals(16, kept.size());
    assertEquals("minecraft:pattern_0", kept.getFirst().pattern());
    assertEquals("minecraft:pattern_15", kept.getLast().pattern());
  }

  @Test
  void aLayerThatIsNoNamespacedIdIsLeftOut() {
    BannerDesign design =
        TownDesigns.designOf(
            "BLUE_BANNER",
            List.of(
                new BannerDesign.Pattern("creeper", DyeColor.RED),
                new BannerDesign.Pattern("Minecraft:Cross", DyeColor.RED),
                new BannerDesign.Pattern("example:unknown", DyeColor.GREEN)));

    // an unknown pattern stays: the renderer leaves it out and logs it
    assertEquals(BannerDesign.of(DyeColor.BLUE).with("example:unknown", DyeColor.GREEN), design);
  }
}
