package de.terranova.nations.regions.npc;

import de.terranova.nations.citizens.SettleTrait;
import de.terranova.nations.regions.bank.BankHolder;
import de.terranova.nations.regions.base.GridRegionType;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.base.RegionTypeListener;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class NPCr implements RegionTypeListener {

    RegionType regionType;
    private NPC npc;

    public NPCr(RegionType regionType) {
        if(!(regionType instanceof NPCHolder)) throw new IllegalArgumentException();
        this.regionType = regionType;
        if(!verifyNPC()){
            if(regionType instanceof GridRegionType gridRegionType){
                this.npc = createNPC(regionType.getName(), gridRegionType.getLocation().asLocation());
            }
        }
        regionType.addListener(this);
    }

    public NPC createNPC(String name, Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(name, NPCSkins.BEGGAR.getSkinSign(), NPCSkins.BEGGAR.getSkinTexture());

        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.toggle();

        SettleTrait settleTrait = npc.getOrAddTrait(SettleTrait.class);
        settleTrait.setUUID(regionType.getId());

        npc.setAlwaysUseNameHologram(true);
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", name.replaceAll("_", " ")));
        npc.spawn(location);
        return npc;
    }

    public boolean verifyNPC() {
        if (this.npc == null) {
            boolean beenFound = false;
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (!npc.hasTrait(SettleTrait.class)) continue;
                if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(regionType.getId())) {
                    this.npc = npc;
                    beenFound = true;
                    break;
                }
            }
            return beenFound;
        }
        return true;
    }

    public void tpNPC(Location location) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettleTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(regionType.getId())) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }

    }

    public void reskinNpc(NPCSkins skin) {
        for (net.citizensnpcs.api.npc.NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettleTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(regionType.getId())) {
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                skinTrait.setSkinPersistent(skin.name(), skin.getSkinSign(), skin.getSkinTexture());
            }
        }
    }

    public void hologramNPC(String[] lines) {
        verifyNPC();

        HologramTrait hologramTrait = npc.getOrAddTrait(HologramTrait.class);
        hologramTrait.clear();

        for(String line : lines){
            hologramTrait.addLine(line);
        }
    }

    public void remove() {
        verifyNPC();
        this.npc.destroy();
    }

    public NPC getNPC() {
        return this.npc;
    }

    @Override
    public void onRegionTypeRenamed(String newRegionName) {
        renameNPC(newRegionName);
    }

    @Override
    public void onRegionTypeRemoved(){
        remove();
    }

    public void renameNPC(String name) {
        verifyNPC();
        this.npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", name.replaceAll("_", " ")));

    }
}
