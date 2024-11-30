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

public interface NPCHolder {
 NPCr getNPC();

}
