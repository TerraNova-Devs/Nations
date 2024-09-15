package de.terranova.nations.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import de.terranova.nations.NationsPlugin;
import org.bukkit.Bukkit;

import java.util.Objects;

import static org.bukkit.Bukkit.getServer;

public class SettleFlag {

    public static StringFlag SETTLEMENT_UUID_FLAG;


    public static void registerSettlementFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StringFlag flag = new StringFlag("nations-settlement-uuid", "");
            registry.register(flag);
            SETTLEMENT_UUID_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("nations-settlement-uuid");
            if (existing instanceof StringFlag) {
                //SWAP TO UUID FLAG
                SETTLEMENT_UUID_FLAG = (StringFlag) existing;
            } else {
                NationsPlugin plugin = (NationsPlugin) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Nations"));
                plugin.getLogger().info("Nations settlement flag already exists by another instance");
                plugin.getLogger().info("NATIONS DISABLED TO AVOID MAJOR ERROR");
                getServer().getPluginManager().disablePlugin(plugin);
            }
        }
    }


}
