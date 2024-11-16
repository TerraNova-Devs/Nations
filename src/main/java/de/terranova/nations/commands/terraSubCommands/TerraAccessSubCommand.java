package de.terranova.nations.commands.terraSubCommands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.commands.SubCommand;
import de.terranova.nations.commands.TerraSelectCache;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
//ask join and leave commands, update pl3xmap after changes
public class TerraAccessSubCommand extends SubCommand implements BasicCommand {
    public TerraAccessSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Player p = isPlayer(commandSourceStack);
        if(p == null) return;
        TerraSelectCache cache = hasSelect(p);
        if(cache == null) return;
        SettleDBstuff settleDB = new SettleDBstuff(cache.getSettle().id);

        if((args.length <= 3)) {
            p.sendMessage(Chat.errorFade("Bitte nutze /t user <add|remove|rank|resign> <username> (<rank>)"));
            return;
        }
        AccessLevel inputAccessLevel = null;
        if(args.length == 4) {
            for(AccessLevel level : AccessLevel.values()) {
                if(level.name().equalsIgnoreCase(args[3])) {
                    inputAccessLevel = level;
                    break;
                }
            }
            if(inputAccessLevel == null) {
                p.sendMessage(Chat.errorFade(String.format("Der Spieler %s konnte nicht gefunden werden", args[3])));
                return;
            }
        }
        Player target = Bukkit.getPlayer(args[2]);
        if(target == null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s konnte nicht gefunden werden", args[2])));
            return;
        }
        AccessLevel targetAccessLevel = null;
        if(cache.getSettle().accessLevel.containsKey(target.getUniqueId())){
            targetAccessLevel = cache.getSettle().accessLevel.get(target.getUniqueId());
        };

        if(!hasAccess(cache.getAccess(), AccessLevel.VICE)) {
            p.sendMessage("Um die Ränge innerhalb der Stadt zu ändern musst du mindestens Vizeanführer sein.");
            return;
        }

        if(args[1].equalsIgnoreCase("add")) {
            if(targetAccessLevel != null) {
                p.sendMessage(String.format("Der Spieler %s ist bereits Mitglied deiner Stadt.",target.getName()));
                return;
            }

            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.CITIZEN);
            RegionClaimFunctions.addOrRemoveFromSettlement(target, cache.getSettle(), true);
            cache.getSettle().accessLevel.put(target.getUniqueId(), AccessLevel.CITIZEN);

            target.sendMessage(Chat.greenFade(String.format("Du wurdest zu der Region %s als Mitglied hinzugefügt.",cache.getSettle().name)));
            p.sendMessage(Chat.greenFade(String.format("Du hast %s erfolgreich zur Stadt %s als Mitglied hinzugefügt.",target.getName(),cache.getSettle().name)));
        }
        else if(args[1].equalsIgnoreCase("remove")) {
            if(targetAccessLevel == null) {
                p.sendMessage(Chat.errorFade(String.format("Der Spieler %s kann nicht entfernt werden, er ist kein Mitglied deiner Stadt.",target.getName())));
                return;
            }

            settleDB.changeMemberAccess(target.getUniqueId(), null);
            RegionClaimFunctions.addOrRemoveFromSettlement(target, cache.getSettle(), false);
            cache.getSettle().accessLevel.remove(target.getUniqueId());

            target.sendMessage(Chat.redFade(String.format("Du wurdest von der Region %s als Mitglied entfernt.",cache.getSettle().name)));
            p.sendMessage(Chat.redFade(String.format("Du hast %s erfolgreich von der Stadt %s entfernt.",target.getName(),cache.getSettle().name)));
        }
        else if(args[1].equalsIgnoreCase("rank")) {
            if(!(args.length == 4)) {
                p.sendMessage(Chat.errorFade("Bitte nutze /t user rank <username> <rank>"));
                return;
            }
            if(targetAccessLevel == null) {
                p.sendMessage(String.format("Der Spieler %s ist kein Mitglied deiner Stadt und kann demnach nicht befördert werden.",target.getName()));
                return;
            }

            settleDB.changeMemberAccess(target.getUniqueId(), inputAccessLevel);
            cache.getSettle().accessLevel.replace(target.getUniqueId(), inputAccessLevel);

            target.sendMessage(Chat.redFade(String.format("Dein Rang wurde in der Region %s auf %s geändert.",cache.getSettle().name,inputAccessLevel.name())));
            p.sendMessage(Chat.redFade(String.format("Du hast %s erfolgreich auf den Rang %s gestuft.",target.getName(),inputAccessLevel.name())));
        }
        else if(args[1].equalsIgnoreCase("resign")) {
            if(!hasAccess(cache.getAccess(), AccessLevel.MAJOR)) {
                p.sendMessage("Du müsst Bürgermeister einer Stadt sein um diese an einen anderen Spieler überschreiben zu können");
                return;
            }
            if(targetAccessLevel == null || !hasAccess(targetAccessLevel, AccessLevel.VICE)) {
                p.sendMessage("Du kannst deine Stadt aus Sicherheitsgründen nur an einen Vizeanführer übergeben.");
                return;
            }
            boolean hasConsent = false;
            if(!hasConsent) {
                p.sendMessage("Dein gegenüber hat dem Transfer der Stadt nicht zugestimmt.");
                return;
            }
            settleDB.changeMemberAccess(p.getUniqueId(), null);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.MAJOR);
            target.sendMessage(Chat.redFade(String.format("Dir wurde erfolgreich die Stadt %s übertragen.",cache.getSettle().name)));
            p.sendMessage(Chat.redFade(String.format("Du hast %s erfolgreich die Stadt %s übertragen.",target.getName(),cache.getSettle().name)));
        } else {
            p.sendMessage(Chat.errorFade("Bitte nutze /t user <add|remove|rank|resign> <username> (<rank>)"));
            return;
        }
    }
}
