package de.terranova.nations.worldguard;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import javax.swing.plaf.nimbus.State;
import java.awt.*;
import java.util.Objects;
import java.util.Set;

public class settlementHandler extends FlagValueChangeHandler<settlementFlag> {
    public static final Factory FACTORY = new Factory();

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, settlementFlag value) {

    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, settlementFlag currentValue, settlementFlag lastValue, MoveType moveType) {
        return false;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, settlementFlag lastValue, MoveType moveType) {
        return false;
    }

    public static class Factory extends Handler.Factory<settlementHandler> {
        @Override
        public settlementHandler create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new settlementHandler(session);
        }
    }
    // construct with your desired flag to track changes
    public settlementHandler(Session session) {
        super(session, (Flag) settlementFlag.SETTLEMENT_UUID_FLAG);
    }
    // ... override handler methods here
    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        if (entered.isEmpty() && exited.isEmpty() && from.getExtent().equals(to.getExtent())) {
            return true; // no changes to flags if regions didn't change
        }

        for (ProtectedRegion region : entered){
            if(Objects.requireNonNull(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG)).isEmpty()){
                CraftPlayer p = (CraftPlayer) player;
                ServerGamePacketListenerImpl connection = p.getHandle().connection;
                p.sendMessage(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG));

                //IChatBaseComponent titleJSON = ChatSerializer.a("{'text': '" + title + "'}");
                //IChatBaseComponent subtitleJSON = ChatSerializer.a("{'text': '" + subtitle + "'}");
                //PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleJSON, fadeIn, stay, fadeOut);
                //PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, subtitleJSON);
                //connection.sendPacket(titlePacket);
                //connection.sendPacket(subtitlePacket);

            }
        }

        return true;
    }

}
