package de.terranova.nations.regions.npc;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.commands.CommandAnnotation;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.access.AccessControlled;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.base.TerraSelectCache;
import de.terranova.nations.regions.bank.BankHolder;
import de.terranova.nations.regions.grid.SettleRegionType;
import org.bukkit.entity.Player;

import java.util.Optional;

import static de.terranova.nations.commands.NationCommandUtil.hasSelect;

public class NPCCommands {
    @CommandAnnotation(
            domain = "npc.move",
            permission = "nations.bank.balance",
            description = "Checks the bank balance",
            usage = "/terra npc move"
    )
    public static boolean moveNPC(Player p, String[] args) {
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return false;

        Optional<SettleRegionType> settle = RegionManager.retrieveRegion("settle", p.getLocation());
        if(settle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Bitte gehe sicher dass du innerhalb von deiner Stadt geclaimten bereich stehst."));
            return false;
        }

        if(!settle.get().getAccess().hasAccess(settle.get().getAccess().getAccessLevel(p.getUniqueId()), AccessLevel.VICE)){
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung den NPC zu verschieben."));
            return false;
        }

        settle.get().getNPC().tpNPC(p.getLocation());
      return false;
    }



}
