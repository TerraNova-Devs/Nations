package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;

public class TypeHandler extends FlagValueChangeHandler<String> {
    public static final RegionHandler.Factory FACTORY = new RegionHandler.Factory();

    public static class Factory extends Handler.Factory<RegionHandler> {
        public RegionHandler create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new RegionHandler(session);
        }
    }

    // construct with your desired flag to track changes
    public TypeHandler(Session session) {
        super(session, TypeFlag.NATIONS_TYPE);
    }

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


}
