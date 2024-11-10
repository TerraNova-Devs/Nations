package de.terranova.nations.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class NewSettleHandler extends FlagValueChangeHandler<String> {

    public static final Factory FACTORY = new Factory();


    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, String value) {

    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String currentValue, String lastValue, MoveType moveType) {
        return false;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String lastValue, MoveType moveType) {
        return false;
    }

    public static class Factory extends Handler.Factory<NewSettleHandler> {
        @Override
        public NewSettleHandler create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new NewSettleHandler(session);
        }
    }

    protected NewSettleHandler(Session session) {
        super(session, SettleFlag.SETTLEMENT_UUID_FLAG);
    }

}
