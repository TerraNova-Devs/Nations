package de.nekyia.nations.regions.npc;

import de.nekyia.nations.regions.RegionManager;
import de.nekyia.nations.regions.access.Access;
import de.nekyia.nations.regions.access.AccessLevel;
import de.nekyia.nations.regions.base.TerraSelectCache;
import de.nekyia.nations.regions.grid.SettleRegionType;
import de.nekyia.nations.utils.Chat;
import de.nekyia.nations.utils.commands.CommandAnnotation;
import org.bukkit.entity.Player;

import java.util.Optional;


public class NPCCommands {

    public NPCCommands(){

    }

    @CommandAnnotation(
            domain = "npc.move",
            permission = "nations.npc.move",
            description = "Moves the npc to your location",
            usage = "/terra npc move"
    )
    public boolean moveNPC(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;

        Optional<SettleRegionType> settle = RegionManager.retrieveRegion("settle", p.getLocation());
        if(settle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Bitte gehe sicher dass du innerhalb von deiner Stadt geclaimten bereich stehst."));
            return false;
        }

        if(!Access.hasAccess(settle.get().getAccess().getAccessLevel(p.getUniqueId()), AccessLevel.VICE)){
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung den NPC zu verschieben."));
            return false;
        }

        settle.get().getNPC().tpNPC(p.getLocation());
      return false;
    }



}
