package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import de.mcterranova.terranovaLib.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class ElytraListener implements Listener {

    private static final double MAX_SAFE_HEIGHT_ABOVE_GROUND = 10.0;
    private static final double PUSH_DOWN_STRENGTH = 0.3;
    private static final int VELOCITY_CANCEL_TICKS = 20; // how long to suppress speed (1 second)

    private final Plugin plugin;

    public ElytraListener(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Cancels elytra activation if the region denies it.
     */
    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!event.isGliding()) return; // only care about activation
        if (isElytraAllowed(player)) return;

        event.setCancelled(true);
        player.sendActionBar(Chat.yellowFade("Elytra ist in dieser Region verboten!"));
    }

    /**
     * Suppresses velocity and pushes the player down if they somehow manage to glide in a denied region.
     * Also handles the case where a player enters a denied region while already gliding.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) return;
        if (isElytraAllowed(player)) return;

        // Cancel their velocity entirely
        player.setVelocity(new Vector(0, 0, 0));

        // Check height above ground
        double heightAboveGround = getHeightAboveGround(player);
        if (heightAboveGround > MAX_SAFE_HEIGHT_ABOVE_GROUND) {
            // Push them gently downward
            player.setVelocity(new Vector(0, -PUSH_DOWN_STRENGTH, 0));
        } else {
            // Close to ground — just stop gliding
            player.setGliding(false);
        }

        player.sendActionBar(Chat.yellowFade("Elytra ist in dieser Region verboten!"));
    }

    /**
     * Returns true if the nations-elytra flag is set to DENY at the player's current location.
     */
    private boolean isElytraAllowed(Player player) {
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

    /**
     * Approximates height above ground by ray-casting downward.
     */
    private double getHeightAboveGround(Player player) {
        org.bukkit.Location loc = player.getLocation();
        org.bukkit.World world = loc.getWorld();
        if (world == null) return 0;

        int groundY = world.getHighestBlockYAt(loc);
        return loc.getY() - groundY;
    }
}