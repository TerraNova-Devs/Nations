package de.terranova.nations.settlements;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.UUID;

public class settlement {

    public final String name;
    private final playerdata owner;
    private final Location location;
    public ArrayList<Double> claims = new ArrayList<Double>();
    public int level;

    private int npcid;

    public settlement(UUID uuid, Location location, String name) {
        this.owner = new playerdata(uuid);
        this.name = name;
        this.location = location;
        this.level = 100;
        createNPC(name);
    }

    public boolean canSettle() {
        return owner.canSettle;
    }

    private void createNPC(String name) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(name, TownSkins.BEGGAR.getSkinSign(), TownSkins.BEGGAR.getSkinTexture());

        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.toggle();

        SettlementTrait settlementTrait = npc.getOrAddTrait(SettlementTrait.class);
        settlementTrait.setUUID(name);

        HologramTrait hologram = npc.getOrAddTrait(HologramTrait.class);
        hologram.addLine(String.format("<#B0EB94>Level: [%s]", this.level));

        npc.setAlwaysUseNameHologram(true);
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name));
        npc.spawn(location);
        this.npcid = npc.getId();
    }

    public void tpNPC(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        npc.teleport(location, PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
    }

    public void reskinNpc(TownSkins skin) {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(skin.name(), skin.getSkinSign(), skin.getSkinTexture());
    }

    public void levelUP() {
        this.level++;
    }

}

