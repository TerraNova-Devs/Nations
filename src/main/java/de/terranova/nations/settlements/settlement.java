package de.terranova.nations.settlements;

import de.terranova.nations.worldguard.Vectore2;
import de.terranova.nations.worldguard.claim;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;

public class settlement {

    public final UUID id;

    public String name;
    public final Vectore2 location;

    public int level;
    public HashMap<UUID, AccessLevelEnum> members = new HashMap<UUID, AccessLevelEnum>();

    public settlement(UUID settlementUUID, UUID owner, Location location, String name) {

        this.id = settlementUUID;
        this.name = name;

        this.location = claim.getSChunkMiddle(location);

        this.level = 100;
        this.members.put(owner, AccessLevelEnum.MAJOR);

        createNPC(name, location,settlementUUID);
    }

    public settlement(UUID settlementUUID, HashMap<UUID, AccessLevelEnum> members, Vectore2 location, String name, int level) {
        this.id = settlementUUID;
        this.name = name;
        this.location = location;
        this.level = level;
        this.members = members;
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
            System.out.println(npc.getOrAddTrait(SettlementTrait.class).getUUID() + " <o-o> " + this.id);
            if(npc.getOrAddTrait(SettlementTrait.class).getUUID() == this.id) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
            }
        }

    }

    public void reskinNpc(TownSkins skin) {
        for (NPC npc : CitizensAPI.getNPCRegistry()){
            if(!npc.hasTrait(SettlementTrait.class)) {
                continue;
            }
            if(npc.getOrAddTrait(SettlementTrait.class).getUUID() == this.id) {
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
            if(npc.getOrAddTrait(SettlementTrait.class).getUUID() == this.id) {
                npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name));
            }
        }
    }

    public void levelUP() {
        this.level++;
    }


}

