package de.nekyia.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import de.nekyia.nations.regions.grid.SettleRegionType;
import de.nekyia.nations.regions.RegionManager;
import de.nekyia.nations.utils.Chat;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class RegionHandler extends FlagValueChangeHandler<String> {
    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<RegionHandler> {
        public RegionHandler create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new RegionHandler(session);
        }
    }

    // construct with your desired flag to track changes
    public RegionHandler(Session session) {
        super(session, RegionFlag.REGION_UUID_FLAG);
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

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        if (entered.isEmpty() && exited.isEmpty() && from.getExtent().equals(to.getExtent())) {
            return true;
        }

        CraftPlayer p = (CraftPlayer) BukkitAdapter.adapt(player);

        if (entered.isEmpty()) {
            for (ProtectedRegion region : exited) {
                String flag = region.getFlag(RegionFlag.REGION_UUID_FLAG);
                if (flag == null || flag.equals(RegionFlag.DefaultValue)) return true;
                Optional<SettleRegionType> osettle = RegionManager.retrieveRegion("settle", UUID.fromString(flag));
                if (osettle.isEmpty()) return true;
                SettleRegionType settle = osettle.get();
                p.sendActionBar(Chat.greenFade(String.format("Du hast %s verlassen.", settle.getName().replaceAll("_", " "))));
            }

        } else {
            for (ProtectedRegion region : entered) {
                String flag = region.getFlag(RegionFlag.REGION_UUID_FLAG);
                if (flag == null || flag.equals(RegionFlag.DefaultValue)) return true;
                Optional<SettleRegionType> osettle = RegionManager.retrieveRegion("settle", UUID.fromString(flag));
                if (osettle.isEmpty()) return true;
                SettleRegionType settle = osettle.get();
                p.sendActionBar(Chat.greenFade(String.format("Du hast %s betreten.", settle.getName().replaceAll("_", " "))));
            }
        }
        return true;
    }

}
