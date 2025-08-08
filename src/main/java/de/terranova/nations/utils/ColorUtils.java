package de.terranova.nations.utils;

import java.awt.*;

public class ColorUtils {
  /**
   * Generates a semi-transparent color deterministically from a string (e.g. Nation name). For
   * example, we: - Use the string's hash code to pick a hue in the [0..360) range - Fix
   * saturation/brightness to produce pleasant, distinct hues - Add an alpha channel for partial
   * transparency
   */
  public static int getColorFromName(String text) {
    if (text == null || text.isEmpty()) {
      // fallback to some default
      return 0x55FFFFFF;
    }

    // Get a positive hash to avoid negative mod issues
    int hash = Math.abs(text.hashCode());
    // We'll choose a hue from 0..360
    float hue = (hash % 360);
    // Convert to a 0..1 range
    hue /= 360F;
    // We can pick a mid-range saturation/brightness
    float saturation = 0.6F;
    float brightness = 0.8F;

    Color awtColor = Color.getHSBColor(hue, saturation, brightness);
    // awtColor.getRGB() includes an alpha of 0xFF. We can mask and add our own transparency if
    // desired
    int rgb = awtColor.getRGB() & 0x00FFFFFF; // strip existing alpha
    // For example, 0x55 is about 33% opacity. You can adjust as desired
    int alpha = 0x55 << 24; // shift alpha 0x55 to the correct place
    return alpha | rgb;
  }
}
