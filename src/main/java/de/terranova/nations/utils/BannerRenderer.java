package de.terranova.nations.utils;

import static org.bukkit.block.banner.PatternType.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

/**
 * Utility class to render a Banner item to a Base64-encoded PNG "data URI" string.
 *
 * <p>Make sure you have the PNG overlays (banner_base.png + pattern files) in:
 * src/main/resources/banners/
 */
public class BannerRenderer {

  // Map from Bukkit DyeColor to an approximate Java AWT color
  private static final EnumMap<DyeColor, Color> DYE_TO_AWT = new EnumMap<>(DyeColor.class);

  static {
    // Fill in the color map to match Minecraft’s banner dye tints as closely as possible
    DYE_TO_AWT.put(DyeColor.WHITE, new Color(255, 255, 255));
    DYE_TO_AWT.put(DyeColor.ORANGE, new Color(216, 127, 51));
    DYE_TO_AWT.put(DyeColor.MAGENTA, new Color(178, 76, 216));
    DYE_TO_AWT.put(DyeColor.LIGHT_BLUE, new Color(102, 153, 216));
    DYE_TO_AWT.put(DyeColor.YELLOW, new Color(229, 229, 51));
    DYE_TO_AWT.put(DyeColor.LIME, new Color(127, 204, 25));
    DYE_TO_AWT.put(DyeColor.PINK, new Color(242, 127, 165));
    DYE_TO_AWT.put(DyeColor.GRAY, new Color(76, 76, 76));
    DYE_TO_AWT.put(DyeColor.LIGHT_GRAY, new Color(153, 153, 153));
    DYE_TO_AWT.put(DyeColor.CYAN, new Color(76, 127, 153));
    DYE_TO_AWT.put(DyeColor.PURPLE, new Color(127, 63, 178));
    DYE_TO_AWT.put(DyeColor.BLUE, new Color(51, 76, 178));
    DYE_TO_AWT.put(DyeColor.BROWN, new Color(102, 76, 51));
    DYE_TO_AWT.put(DyeColor.GREEN, new Color(102, 127, 51));
    DYE_TO_AWT.put(DyeColor.RED, new Color(153, 51, 51));
    DYE_TO_AWT.put(DyeColor.BLACK, new Color(25, 25, 25));
  }

  /**
   * Render the given banner ItemStack into a Base64-encoded PNG "data:image/png;base64,..." string.
   *
   * @param bannerItem A colored Banner item (any color, with zero or more patterns).
   * @return Base64 data URI string, or null if invalid banner or error.
   */
  public static String renderBannerToDataURI(ItemStack bannerItem) {
    try {
      // 1) Validate item & get BannerMeta
      if (bannerItem == null || bannerItem.getType() == Material.AIR) {
        return null;
      }
      if (!(bannerItem.getItemMeta() instanceof BannerMeta meta)) {
        return null;
      }

      // 2) Determine the base color
      DyeColor baseColor = guessBaseColor(bannerItem.getType());
      if (baseColor == null) {
        baseColor = guessBaseColor(bannerItem.getType());
      }

      // 3) Collect all Patterns
      List<Pattern> patterns = meta.getPatterns();

      // 4) Create an ARGB image (64x128 is typical banner resolution)
      BufferedImage finalImage = new BufferedImage(64, 128, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = finalImage.createGraphics();

      // 5) Draw "base banner" silhouette, tinted with baseColor
      BufferedImage bannerBase = loadOverlay("base.png");
      if (bannerBase == null) {
        g.dispose();
        return null;
      }
      BufferedImage coloredBase = colorize(bannerBase, getAwtColor(baseColor));
      g.drawImage(coloredBase, 0, 0, null);

      // 6) Apply each pattern in order
      for (Pattern pattern : patterns) {
        PatternType patternType = pattern.getPattern();
        String fileName = getPatternFile(patternType);
        DyeColor dye = pattern.getColor();
        BufferedImage overlay = loadOverlay(fileName);
        if (overlay == null) {
          // If we don't have a file for that pattern, skip it
          continue;
        }
        BufferedImage tinted = colorize(overlay, getAwtColor(dye));
        g.drawImage(tinted, 0, 0, null);
      }

      // Done layering
      g.dispose();

      BufferedImage frontOnly = finalImage.getSubimage(0, 0, 22, 40);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(frontOnly, "png", baos);
      String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
      return "data:image/png;base64," + base64;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /** Helper to map a Bukkit DyeColor to our AWT color map (defaulting to white if missing). */
  private static Color getAwtColor(DyeColor dye) {
    Color c = DYE_TO_AWT.get(dye);
    return (c != null) ? c : Color.WHITE;
  }

  /**
   * Basic guess of the banner's base color by material name (e.g. "RED_BANNER" -> DyeColor.RED).
   */
  private static DyeColor guessBaseColor(Material material) {
    String name = material.name();
    for (DyeColor dye : DyeColor.values()) {
      if (name.startsWith(dye.name())) {
        return dye;
      }
    }
    // fallback
    return DyeColor.WHITE;
  }

  /** Load a PNG overlay from the /banners/ folder inside plugin resources. */
  private static BufferedImage loadOverlay(String fileName) {
    try (InputStream in = BannerRenderer.class.getResourceAsStream("/banners/" + fileName)) {
      if (in == null) {
        return null;
      }
      return ImageIO.read(in);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Switch on ALL known PatternType enum constants (1.20/1.21). Return the overlay PNG filename
   * that you placed in /banners/.
   *
   * <p>For example, if you have "pattern_stripe_top.png" for STRIPE_TOP, you return that exact
   * string. Adjust to match the actual file names you are using.
   */
  private static final Map<PatternType, String> PATTERN_FILES = new HashMap<>();

  static {
    PATTERN_FILES.put(BASE, "base.png");
    PATTERN_FILES.put(BORDER, "border.png");
    PATTERN_FILES.put(BRICKS, "bricks.png");
    PATTERN_FILES.put(CIRCLE, "circle.png");
    PATTERN_FILES.put(CREEPER, "creeper.png");
    PATTERN_FILES.put(CROSS, "cross.png");
    PATTERN_FILES.put(CURLY_BORDER, "curly_border.png");
    PATTERN_FILES.put(DIAGONAL_LEFT, "diagonal_left.png");
    PATTERN_FILES.put(DIAGONAL_RIGHT, "diagonal_right.png");
    PATTERN_FILES.put(DIAGONAL_UP_LEFT, "diagonal_up_left.png");
    PATTERN_FILES.put(DIAGONAL_UP_RIGHT, "diagonal_up_right.png");
    PATTERN_FILES.put(FLOW, "flow.png");
    PATTERN_FILES.put(FLOWER, "flower.png");
    PATTERN_FILES.put(GLOBE, "globe.png");
    PATTERN_FILES.put(GRADIENT, "gradient.png");
    PATTERN_FILES.put(GRADIENT_UP, "gradient_up.png");
    PATTERN_FILES.put(GUSTER, "guster.png");
    PATTERN_FILES.put(HALF_HORIZONTAL, "half_horizontal.png");
    PATTERN_FILES.put(HALF_HORIZONTAL_BOTTOM, "half_horizontal_bottom.png");
    PATTERN_FILES.put(HALF_VERTICAL, "half_vertical.png");
    PATTERN_FILES.put(HALF_VERTICAL_RIGHT, "half_vertical_right.png");
    PATTERN_FILES.put(MOJANG, "mojang.png");
    PATTERN_FILES.put(PIGLIN, "piglin.png");
    PATTERN_FILES.put(RHOMBUS, "rhombus.png");
    PATTERN_FILES.put(SKULL, "skull.png");
    PATTERN_FILES.put(SMALL_STRIPES, "small_stripes.png");
    PATTERN_FILES.put(SQUARE_BOTTOM_LEFT, "square_bottom_left.png");
    PATTERN_FILES.put(SQUARE_BOTTOM_RIGHT, "square_bottom_right.png");
    PATTERN_FILES.put(SQUARE_TOP_LEFT, "square_top_left.png");
    PATTERN_FILES.put(SQUARE_TOP_RIGHT, "square_top_right.png");
    PATTERN_FILES.put(STRAIGHT_CROSS, "straight_cross.png");
    PATTERN_FILES.put(STRIPE_BOTTOM, "stripe_bottom.png");
    PATTERN_FILES.put(STRIPE_CENTER, "stripe_center.png");
    PATTERN_FILES.put(STRIPE_DOWNLEFT, "stripe_downleft.png");
    PATTERN_FILES.put(STRIPE_DOWNRIGHT, "stripe_downright.png");
    PATTERN_FILES.put(STRIPE_LEFT, "stripe_left.png");
    PATTERN_FILES.put(STRIPE_MIDDLE, "stripe_middle.png");
    PATTERN_FILES.put(STRIPE_RIGHT, "stripe_right.png");
    PATTERN_FILES.put(STRIPE_TOP, "stripe_top.png");
    PATTERN_FILES.put(TRIANGLE_BOTTOM, "triangle_bottom.png");
    PATTERN_FILES.put(TRIANGLE_TOP, "triangle_top.png");
    PATTERN_FILES.put(TRIANGLES_BOTTOM, "triangles_bottom.png");
    PATTERN_FILES.put(TRIANGLES_TOP, "triangles_top.png");
  }

  /** Returns the filename for the given PatternType, or null if none found. */
  private static String getPatternFile(PatternType type) {
    return PATTERN_FILES.get(type);
  }

  /**
   * Colorize a white overlay to the given Color, preserving alpha. White pixels get replaced by
   * 'color'; transparent remains transparent.
   */
  private static BufferedImage colorize(BufferedImage overlay, Color color) {
    int width = overlay.getWidth();
    int height = overlay.getHeight();

    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = overlay.getRGB(x, y);
        int alpha = (argb >>> 24);

        if (alpha == 0) {
          // remain transparent
          result.setRGB(x, y, argb);
        } else {
          // force alpha=255 for any non-transparent pixel
          int tinted =
              (0xFF << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
          result.setRGB(x, y, tinted);
        }
      }
    }
    return result;
  }
}
