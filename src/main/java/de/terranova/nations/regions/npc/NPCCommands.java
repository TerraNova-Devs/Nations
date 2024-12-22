package de.terranova.nations.regions.npc;

import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.access.Access;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.base.TerraSelectCache;
import de.terranova.nations.regions.grid.SettleRegionType;
import org.bukkit.Material;
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
