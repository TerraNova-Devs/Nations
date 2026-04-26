package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import de.mcterranova.terranovaLib.utils.Chat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ElytraListener implements Listener {

    public ElytraListener(JavaPlugin plugin) {
        ElytraHandler.init(plugin);
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!event.isGliding()) return;

        if (isElytraAllowed(player)) return;

        ElytraHandler.startLimiting(player);
    }

    @EventHandler
    public void onRocketUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.isGliding()) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FIREWORK_ROCKET) return;

        if (isElytraAllowed(player)) return;

        event.setCancelled(true);
        ElytraHandler.startLimiting(player);

        player.sendActionBar(Chat.yellowFade("Raketen sind in dieser Region verboten!"));
    }

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
}