package de.terranova.nations.settlements;

import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.settlementClaim;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class settlement {

    public final UUID id;

    public String name;
    public final Vectore2 location;

    public int level;
    public HashMap<UUID, AccessLevelEnum> members = new HashMap<>();

    public int claims;

    //Beim neu erstellen
    public settlement(UUID settlementUUID, UUID owner, Location location, String name) {

        this.id = settlementUUID;
        this.name = name;

        this.location = settlementClaim.getSChunkMiddle(location);

        this.level = 0;
        this.members.put(owner, AccessLevelEnum.MAJOR);

        this.claims = 1;

        createNPC(name, location,settlementUUID);
    }

    //Von der Datenbank
    public settlement(UUID settlementUUID, HashMap<UUID, AccessLevelEnum> members, Vectore2 location, String name, int level) {
        this.id = settlementUUID;
        this.name = name;
        this.location = location;
        this.level = level;
        this.members = members;
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

    public Collection<UUID> getEveryMemberWithCertainAccessLevel(AccessLevelEnum access){
        Collection<UUID> output = new ArrayList<>();
        for(UUID uuid : members.keySet()){
            if(members.get(uuid).equals(access)){
                output.add(uuid);
            }
        }
        return output;
    }

    public void levelUP() {
        this.level++;
    }


}

