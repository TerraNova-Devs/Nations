package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.Chat;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DenyEntryPlayersHandler extends FlagValueChangeHandler<Set<String>> {

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<DenyEntryPlayersHandler> {
        @Override
        public DenyEntryPlayersHandler create(Session session) {
            return new DenyEntryPlayersHandler(session);
        }
    }

    public DenyEntryPlayersHandler(Session session) {
        super(session, DenyEntryPlayersFlag.DENY_ENTRY_PLAYERS);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        if (entered.isEmpty() && exited.isEmpty() && from.getExtent().equals(to.getExtent())) {
            return true;
        }

        Player bukkitPlayer = BukkitAdapter.adapt(player);
        UUID uuid = bukkitPlayer.getUniqueId();

        for (ProtectedRegion region : entered) {
            Set<String> deniedUUIDs = region.getFlag(DenyEntryPlayersFlag.DENY_ENTRY_PLAYERS);
            if (deniedUUIDs != null && deniedUUIDs.contains(uuid.toString())) {
                if(bukkitPlayer.isOp()){
                    bukkitPlayer.sendMessage(Chat.redFade("Du bist von diesem Grundstück gebannt op bypass"));
                    return true;
                }
                bukkitPlayer.sendMessage(Chat.errorFade("Du darfst dieses Gebiet nicht betreten, da der besitzer dich gebannt hat."));
                // Teleport back to "from" location
                bukkitPlayer.teleport(BukkitAdapter.adapt(from));
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, Set<String> value) {
        // Not needed
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
                                 Set<String> currentValue, Set<String> lastValue, MoveType moveType) {
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
                                    Set<String> lastValue, MoveType moveType) {
        return true;
    }
}