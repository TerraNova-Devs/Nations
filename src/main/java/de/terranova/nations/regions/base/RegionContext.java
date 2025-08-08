package de.terranova.nations.regions.base;

import java.util.Map;
import org.bukkit.entity.Player;

public class RegionContext {

  public final Player player;
  public final String name;
  public final Map<String, String> extra;

  public RegionContext(Player player, String name, Map<String, String> extra) {
    this.player = player;
    this.name = name;
    this.extra = extra;
  }
}
