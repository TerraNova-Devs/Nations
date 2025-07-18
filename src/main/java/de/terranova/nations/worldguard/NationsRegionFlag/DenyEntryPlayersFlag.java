package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DenyEntryPlayersFlag {

    public static SetFlag<String> DENY_ENTRY_PLAYERS;

    public static void registerRegionFlag(Plugin plugin) {
        FlagRegistry registry = com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry();

        try {
            SetFlag<String> flag = new SetFlag<>("deny-entry-players", new StringFlag(null));
            registry.register(flag);
            DENY_ENTRY_PLAYERS = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("deny-entry-players");
            if (existing instanceof SetFlag<?> existingSetFlag) {
                // Suppress unchecked cast safely
                @SuppressWarnings("unchecked")
                SetFlag<String> casted = (SetFlag<String>) existingSetFlag;
                DENY_ENTRY_PLAYERS = casted;
            } else {
                Bukkit.getLogger().severe("Flag conflict: 'deny-entry-players' already exists with wrong type.");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }
}
