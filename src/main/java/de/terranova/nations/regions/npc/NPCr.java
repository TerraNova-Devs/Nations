package de.terranova.nations.regions.npc;

import de.terranova.nations.citizens.SettleTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class NPCr {

    private UUID id;
    private NPC npc;

    public NPCr(String name, Location loc, UUID id) {
        createNPC(name, loc);
        this.id = id;
    }

    public NPCr(UUID id) {
        this.id = id;
    }



    public NPC createNPC(String name, Location location) {
        net.citizensnpcs.api.npc.NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(name, NPCSkins.BEGGAR.getSkinSign(), NPCSkins.BEGGAR.getSkinTexture());

        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.toggle();

        SettleTrait settleTrait = npc.getOrAddTrait(SettleTrait.class);
        settleTrait.setUUID(id);

        npc.setAlwaysUseNameHologram(true);
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", name.replaceAll("_", " ")));
        npc.spawn(location);
        return npc;
    }

    public void getCitizensNPCbySUUID() {
        if (this.npc == null) {
            for (net.citizensnpcs.api.npc.NPC npc : CitizensAPI.getNPCRegistry()) {

                if (!npc.hasTrait(SettleTrait.class)) continue;
                if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(id)) {
                    this.npc = npc;
                }
            }
        }
    }

    public void tpNPC(Location location) {
        for (net.citizensnpcs.api.npc.NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettleTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(id)) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }

    }

    public void reskinNpc(NPCSkins skin) {
        for (net.citizensnpcs.api.npc.NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettleTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(id)) {
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                skinTrait.setSkinPersistent(skin.name(), skin.getSkinSign(), skin.getSkinTexture());
            }
        }
    }

    public void renameNPC(String name) {

        getCitizensNPCbySUUID();
        this.npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", name.replaceAll("_", " ")));

    }

    public void removeNPC() {
        getCitizensNPCbySUUID();
        this.npc.destroy();
    }

    public NPC getNPC() {
        return this.npc;
    }

}
