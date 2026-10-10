package de.terranova.nations.heroicmap;

import com.nekyia.heroicmap.api.BannerDesign;
import com.nekyia.heroicmap.api.Layer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.DyeColor;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

/**
 * Banner designs of the towns, drawn by the renderer from the banner of their nation, with a crown
 * for capitals. The only class that names the design API of HeroicMap 0.6; TownMap loads it only
 * when HeroicMap is that new, so an older one keeps working with images.
 */
final class TownDesigns {

  /** The design of towns without a nation, without a banner, or whose banner fails. */
  static final String WHITE = "white";

  /** The game draws no more layers than this. */
  private static final int MAX_LAYERS = 16;

  private static final Pattern PATTERN_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

  private TownDesigns() {}

  /**
   * The design of a banner item from its material, such as {@code LIGHT_BLUE_BANNER}, and its
   * layers; null if the material is no banner item. Of more than 16 layers the first 16 count, as
   * the game draws them; a layer whose pattern is not {@code namespace:path} is left out, since
   * HeroicMap would refuse the design.
   */
  static BannerDesign designOf(String material, List<BannerDesign.Pattern> layers) {
    if (!material.endsWith("_BANNER")) {
      return null;
    }
    DyeColor base;
    try {
      base = DyeColor.valueOf(material.substring(0, material.length() - "_BANNER".length()));
    } catch (IllegalArgumentException e) {
      return null;
    }
    BannerDesign design = BannerDesign.of(base);
    for (BannerDesign.Pattern layer : layers) {
      if (design.layers().size() == MAX_LAYERS) {
        break;
      }
      if (PATTERN_ID.matcher(layer.pattern()).matches()) {
        design = design.with(layer.pattern(), layer.color());
      }
    }
    return design;
  }

  /** The design of a banner item; reading its meta needs the server. Null if it is no banner. */
  static BannerDesign designOf(ItemStack banner) {
    if (banner == null || !(banner.getItemMeta() instanceof BannerMeta meta)) {
      return null;
    }
    Registry<PatternType> patterns =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN);
    List<BannerDesign.Pattern> layers = new ArrayList<>();
    for (org.bukkit.block.banner.Pattern layer : meta.getPatterns()) {
      String id = patterns.getKeyOrThrow(layer.getPattern()).asString();
      layers.add(new BannerDesign.Pattern(id, layer.getColor()));
    }
    return designOf(banner.getType().name(), layers);
  }

  /** Sets the white design on the layer. */
  static void putWhite(Layer layer) {
    layer.design(WHITE, BannerDesign.of(DyeColor.WHITE));
  }

  /** Sets the design of the banner under this name; false if it is no banner. */
  static boolean put(Layer layer, String name, ItemStack banner) {
    BannerDesign design = designOf(banner);
    if (design == null) {
      return false;
    }
    layer.design(name, design);
    return true;
  }

  /** Removes the design under this name; throws while a banner of the layer names it. */
  static void remove(Layer layer, String name) {
    layer.removeDesign(name);
  }
}
