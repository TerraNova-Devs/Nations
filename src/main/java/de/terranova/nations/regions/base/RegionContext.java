package de.terranova.nations.regions.base;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionContext {

  public final Player player;
  public final String name;
  public final Map<String, String> extra;
  public final Location location;

  public RegionContext(Player player, String name, Map<String, String> extra) {
    this(player, name, extra, player.getLocation().clone());
  }

  public RegionContext(Player player, String name, Map<String, String> extra, Location location) {
    this.player = player;
    this.name = name;
    this.extra = extra;
    this.location = location.clone();
  }
}