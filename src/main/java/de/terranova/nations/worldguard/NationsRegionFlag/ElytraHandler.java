package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import de.mcterranova.terranovaLib.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElytraHandler extends FlagValueChangeHandler<StateFlag.State> {

    public static final Factory FACTORY = new Factory();

    private static final double MAX_SAFE_HEIGHT_ABOVE_GROUND = 40.0;
    private static final double PUSH_DOWN_STRENGTH = 0.2;
    private static final double HORIZONTAL_SLOWDOWN = 0.9;

    private static final Map<UUID, BukkitTask> ACTIVE_LIMITERS = new HashMap<>();
    private static JavaPlugin plugin;

    public static class Factory extends Handler.Factory<ElytraHandler> {
        @Override
        public ElytraHandler create(Session session) {
            return new ElytraHandler(session);
        }
    }

    public ElytraHandler(Session session) {
        super(session, ElytraFlag.ELYTRA_FLAG);
    }

    public static void init(JavaPlugin javaPlugin) {
        plugin = javaPlugin;
    }

    public static void startLimiting(Player player) {
        if (plugin == null) {
            Bukkit.getLogger().warning("ElytraHandler was not initialized. Call ElytraHandler.init(plugin) first.");
            return;
        }

        UUID uuid = player.getUniqueId();

        if (ACTIVE_LIMITERS.containsKey(uuid)) return;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player onlinePlayer = Bukkit.getPlayer(uuid);

            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                stopLimiting(uuid);
                return;
            }

            if (!onlinePlayer.isGliding()) {
                stopLimiting(uuid);
                return;
            }

            if (isElytraAllowed(onlinePlayer)) {
                stopLimiting(uuid);
                return;
            }

            limitGlideHeight(onlinePlayer);
        }, 0L, 1L);

        ACTIVE_LIMITERS.put(uuid, task);
    }

    private static void stopLimiting(UUID uuid) {
        BukkitTask task = ACTIVE_LIMITERS.remove(uuid);

        if (task != null) {
            task.cancel();
        }
    }

    @Override
    protected void onInitialValue(
            LocalPlayer player,
            ApplicableRegionSet set,
            StateFlag.State value
    ) {
        if (value == StateFlag.State.DENY) {
            startLimiting(BukkitAdapter.adapt(player));
        }
    }

    @Override
    protected boolean onSetValue(
            LocalPlayer player,
            Location from,
            Location to,
            ApplicableRegionSet toSet,
            StateFlag.State currentValue,
            StateFlag.State lastValue,
            MoveType moveType
    ) {
        if (currentValue == StateFlag.State.DENY) {
            startLimiting(BukkitAdapter.adapt(player));
        }

        return true;
    }

    @Override
    protected boolean onAbsentValue(
            LocalPlayer player,
            Location from,
            Location to,
            ApplicableRegionSet toSet,
            StateFlag.State lastValue,
            MoveType moveType
    ) {
        return true;
    }

    private static void limitGlideHeight(Player player) {
        double heightAboveGround = getHeightAboveGround(player);

        if (heightAboveGround <= MAX_SAFE_HEIGHT_ABOVE_GROUND) return;

        Vector velocity = player.getVelocity();

        player.setVelocity(new Vector(
                velocity.getX() * HORIZONTAL_SLOWDOWN,
                velocity.getY() - PUSH_DOWN_STRENGTH,
                velocity.getZ() * HORIZONTAL_SLOWDOWN
        ));

        player.sendActionBar(Chat.yellowFade("Du fliegst zu hoch für diese Region!"));
    }

    private static boolean isElytraAllowed(Player player) {
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) return true;

        ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint()
        );

        StateFlag.State state = regions.queryState(null, ElytraFlag.ELYTRA_FLAG);
        return state != StateFlag.State.DENY;
    }

    private static double getHeightAboveGround(Player player) {
        org.bukkit.Location loc = player.getLocation();
        World world = loc.getWorld();

        if (world == null) return 0;

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        for (int y = loc.getBlockY(); y >= world.getMinHeight(); y--) {
            Block block = world.getBlockAt(x, y, z);

            if (!block.isPassable()) {
                return loc.getY() - y - 1;
            }
        }

        return loc.getY() - world.getMinHeight();
    }
}