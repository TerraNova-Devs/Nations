package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ElytraFlag {

    public static StateFlag ELYTRA_FLAG;

    public static void registerElytraFlag(Plugin plugin) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("nations-elytra", true); // default: allowed
            registry.register(flag);
            ELYTRA_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("nations-elytra");
            if (existing instanceof StateFlag) {
                ELYTRA_FLAG = (StateFlag) existing;
            } else {
                Bukkit.getLogger().warning("Could not register elytra flag!");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }
}