package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class RegionFlag {
    public static UUIDFlag REGION_UUID_FLAG;
    public static String DefaultValue = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF";

    public static void registerRegionFlag(Plugin plugin) {

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            UUIDFlag flag = new UUIDFlag("nations-region-uuid", UUID.fromString(DefaultValue));
            registry.register(flag);
            REGION_UUID_FLAG = flag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("nations-settlement-uuid");
            if (existing instanceof UUIDFlag) {
                REGION_UUID_FLAG = (UUIDFlag) existing;
            } else {
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
                Bukkit.getLogger().warning("Could not register flag " + REGION_UUID_FLAG);
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }
}