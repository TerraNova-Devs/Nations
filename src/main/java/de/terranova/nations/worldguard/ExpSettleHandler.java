package de.terranova.nations.worldguard;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.EntryFlag;
import com.sk89q.worldguard.session.handler.Handler;

import java.util.Set;

public class ExpSettleHandler extends Handler {
    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<ExpSettleHandler> {
        @Override
        public ExpSettleHandler create(Session session) {
            return new ExpSettleHandler(session);
        }
    }

    public ExpSettleHandler(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {

        return false;
    }
}
