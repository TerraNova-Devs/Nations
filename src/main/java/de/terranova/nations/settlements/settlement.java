package de.terranova.nations.settlements;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.settlementClaim;
import de.terranova.nations.worldguard.settlementFlag;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.sql.SQLException;
import java.util.*;

public class settlement {

    public final UUID id;

    public String name;
    public final Vectore2 location;

    public int level;
    public HashMap<UUID, AccessLevelEnum> membersAccess = new HashMap<>();

    public int claims;

    //Beim neu erstellen
    public settlement(UUID settlementUUID, UUID owner, Location location, String name) {

        this.id = settlementUUID;
        this.name = name;

        this.location = settlementClaim.getSChunkMiddle(location);
        NationsPlugin.settlementManager.locations.add(this.location);

        this.level = 0;
        this.membersAccess.put(owner, AccessLevelEnum.MAJOR);

        this.claims = 1;

        createNPC(name, location,settlementUUID);
    }

    //Von der Datenbank
    public settlement(UUID settlementUUID, HashMap<UUID, AccessLevelEnum> membersAccess, Vectore2 location, String name, int level) {
        this.id = settlementUUID;
        this.name = name;
        this.location = settlementClaim.getSChunkMiddle(location);
        NationsPlugin.settlementManager.locations.add(settlementClaim.getSChunkMiddle(location));
        this.level = level;
        this.membersAccess = membersAccess;
        this.claims = settlementClaim.getClaimAnzahl(settlementUUID);
    }

    private void createNPC(String name, Location location, UUID settlementUUID) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(name, TownSkins.BEGGAR.getSkinSign(), TownSkins.BEGGAR.getSkinTexture());

        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.toggle();

        SettlementTrait settlementTrait = npc.getOrAddTrait(SettlementTrait.class);
        settlementTrait.setUUID(settlementUUID);

        HologramTrait hologram = npc.getOrAddTrait(HologramTrait.class);
        hologram.addLine(String.format("<#B0EB94>Level: [%s]", this.level));

        npc.setAlwaysUseNameHologram(true);
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name));
        npc.spawn(location);
    }

    public void tpNPC(Location location) {
        for (NPC npc : CitizensAPI.getNPCRegistry()){
            if(!npc.hasTrait(SettlementTrait.class)) {
                continue;
            }
            if(npc.getOrAddTrait(SettlementTrait.class).getUUID().equals(this.id)) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
            }
        }

    }

    public void reskinNpc(TownSkins skin) {
        for (NPC npc : CitizensAPI.getNPCRegistry()){
            if(!npc.hasTrait(SettlementTrait.class)) {
                continue;
            }
            if(npc.getOrAddTrait(SettlementTrait.class).getUUID().equals(this.id)) {
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                skinTrait.setSkinPersistent(skin.name(), skin.getSkinSign(), skin.getSkinTexture());
            };
        }
    }

    public void rename(String name) {
        for (NPC npc : CitizensAPI.getNPCRegistry()){
            if(!npc.hasTrait(SettlementTrait.class)) {
                continue;
            }
            if(npc.getOrAddTrait(SettlementTrait.class).getUUID().equals(this.id)) {
                npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name));
            }
        }

    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(AccessLevelEnum access){
        Collection<UUID> output = new ArrayList<>();
        for(UUID uuid : membersAccess.keySet()){
            if(membersAccess.get(uuid).equals(access)){
                output.add(uuid);
            }
        }
        return output;
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(AccessLevelEnum access){
        Collection<String> output = new ArrayList<>();
        for(UUID uuid : membersAccess.keySet()){
            if(membersAccess.get(uuid).equals(access)){
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                if(p.getName() == null) continue;
                output.add(p.getName());
            }
        }
        return output;
    }

    public Optional<AccessLevelEnum> promoteOrAdd(Player target, Player p) throws SQLException {
        if(!this.membersAccess.containsKey(target.getUniqueId())) {
            this.membersAccess.put(target.getUniqueId(),AccessLevelEnum.CITIZEN);
            SettleDBstuff.changeMemberAccess(this.id,target.getUniqueId(),AccessLevelEnum.CITIZEN);
            settlementClaim.addOrRemoveFromSettlement(target,this,true);
            return Optional.of(AccessLevelEnum.CITIZEN);
        }
        AccessLevelEnum accessLevelEnum = this.membersAccess.get(target.getUniqueId());
        if(accessLevelEnum.equals(AccessLevelEnum.CITIZEN)) {
            this.membersAccess.replace(target.getUniqueId(),AccessLevelEnum.COUNCIL);
            SettleDBstuff.changeMemberAccess(this.id,target.getUniqueId(),AccessLevelEnum.COUNCIL);
            return Optional.of(AccessLevelEnum.COUNCIL);
        }
        if(accessLevelEnum.equals(AccessLevelEnum.COUNCIL)) {
            this.membersAccess.replace(target.getUniqueId(),AccessLevelEnum.VICE);
            SettleDBstuff.changeMemberAccess(this.id,target.getUniqueId(),AccessLevelEnum.VICE);
            return Optional.of(AccessLevelEnum.VICE);
        }
        if(accessLevelEnum.equals(AccessLevelEnum.VICE) || accessLevelEnum.equals(AccessLevelEnum.MAJOR)) p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den h\u00F6chstm\u00F6glichen Rang erreicht.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));;
        return Optional.empty();
    }

    public Optional<AccessLevelEnum> demoteOrRemove(Player target, Player p) throws SQLException {
        if(!this.membersAccess.containsKey(target.getUniqueId()) || this.membersAccess.get(target.getUniqueId()).equals(AccessLevelEnum.MAJOR)) return Optional.empty();
        AccessLevelEnum accessLevelEnum = this.membersAccess.get(target.getUniqueId());
        if(accessLevelEnum.equals(AccessLevelEnum.CITIZEN)) {
            this.membersAccess.remove(target.getUniqueId());
            SettleDBstuff.changeMemberAccess(this.id,target.getUniqueId(),AccessLevelEnum.REMOVE);
            settlementClaim.addOrRemoveFromSettlement(target,this,false);
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde von deiner Stadt entfernt.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
            return Optional.of(AccessLevelEnum.REMOVE);
        }
        if(accessLevelEnum.equals(AccessLevelEnum.COUNCIL) ) {
            this.membersAccess.replace(target.getUniqueId(),AccessLevelEnum.CITIZEN);
            SettleDBstuff.changeMemberAccess(this.id,target.getUniqueId(),AccessLevelEnum.CITIZEN);
            return Optional.of(AccessLevelEnum.CITIZEN);
        }
        if(accessLevelEnum.equals(AccessLevelEnum.VICE) ) {
            this.membersAccess.replace(target.getUniqueId(),AccessLevelEnum.COUNCIL);
            SettleDBstuff.changeMemberAccess(this.id,target.getUniqueId(),AccessLevelEnum.COUNCIL);
            return Optional.of(AccessLevelEnum.COUNCIL);
        }
        p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den h\u00F6chstm\u00F6glichen Rang erreicht.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
        return Optional.empty();
    }

    public void levelUP() {
        this.level++;
    }

    public ProtectedRegion getWorldguardRegion() {

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        assert regions != null;
        for (ProtectedRegion region : regions.getRegions().values()) {
            if (region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG) == null) continue;
            UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG)));
            if (this.id.equals(settlementUUID)) {
                return region;
            }

        }
        return null;
    }


}

