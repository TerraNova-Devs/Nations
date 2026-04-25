package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import de.mcterranova.terranovaLib.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraHandler extends FlagValueChangeHandler<StateFlag.State> {

    public static final Factory FACTORY = new Factory();

    private static final double MAX_SAFE_HEIGHT_ABOVE_GROUND = 10.0;
    private static final double PUSH_DOWN_STRENGTH = 0.3;

    public static class Factory extends Handler.Factory<ElytraHandler> {
        @Override
        public ElytraHandler create(Session session) {
            return new ElytraHandler(session);
        }
    }

    public ElytraHandler(Session session) {
        super(session, ElytraFlag.ELYTRA_FLAG);
    }

    @Override
    protected void onInitialValue(
            LocalPlayer player,
            ApplicableRegionSet set,
            StateFlag.State value
    ) {
        if (value == StateFlag.State.DENY) {
            suppressElytra(player);
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
            suppressElytra(player);
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

    private void suppressElytra(LocalPlayer localPlayer) {
        Player player = BukkitAdapter.adapt(localPlayer);

        if (!player.isGliding()) return;

        double heightAboveGround = getHeightAboveGround(player);

        if (heightAboveGround > MAX_SAFE_HEIGHT_ABOVE_GROUND) {
            player.setVelocity(new Vector(0, -PUSH_DOWN_STRENGTH, 0));
        } else {
            player.setVelocity(new Vector(0, 0, 0));
            player.setGliding(false);
        }

        player.sendActionBar(Chat.yellowFade("Elytra ist in dieser Region verboten!"));
    }

    private double getHeightAboveGround(Player player) {
        org.bukkit.Location loc = player.getLocation();

        if (loc.getWorld() == null) return 0;

        int groundY = loc.getWorld().getHighestBlockYAt(loc);
        return loc.getY() - groundY;
    }
}