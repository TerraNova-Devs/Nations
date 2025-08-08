package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TypeFlag {
  // Muss StringFlag sein, da UUIDs von WorldGuards SnakeYML nicht gespeichert werden können
  public static StringFlag NATIONS_TYPE;
  public static String DefaultValue = "none";

  public static void registerRegionFlag(Plugin plugin) {

    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    try {
      // create a flag with the name "my-custom-flag", defaulting to true
      StringFlag flag = new StringFlag("nations-type", DefaultValue);
      registry.register(flag);
      NATIONS_TYPE = flag; // only set our field if there was no error
    } catch (FlagConflictException e) {
      // some other plugin registered a flag by the same name already.
      // you can use the existing flag, but this may cause conflicts - be sure to check type
      Flag<?> existing = registry.get("nations-type");
      if (existing instanceof StringFlag) {
        NATIONS_TYPE = (StringFlag) existing;
      } else {
        // types don't match - this is bad news! some other plugin conflicts with you
        // hopefully this never actually happens
        Bukkit.getLogger().warning("Could not register flag " + NATIONS_TYPE);
        Bukkit.getPluginManager().disablePlugin(plugin);
      }
    }
  }
}
