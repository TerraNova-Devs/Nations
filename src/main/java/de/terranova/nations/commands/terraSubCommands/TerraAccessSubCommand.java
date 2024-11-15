package de.terranova.nations.commands.terraSubCommands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.commands.SubCommand;
import de.terranova.nations.commands.TerraSelectCache;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevel;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        AccessLevel targetAccessLevel = null;
        if(args.length == 4) {
            for(AccessLevel level : AccessLevel.values()) {
                if(level.name().equalsIgnoreCase(args[3])) {
                    targetAccessLevel = level;
                    break;
                }
            }
            if(targetAccessLevel == null) {
                p.sendMessage(Chat.errorFade(String.format("Der Spieler %s konnte nicht gefunden werden", args[2])));
                return;
            }
        }
        Player target = Bukkit.getPlayer(args[2]);
        if(target == null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s konnte nicht gefunden werden", args[2])));
            return;
        }
        AccessLevel targetAccess = null;
        if(cache.getSettle().accessLevel.containsKey(target.getUniqueId())){
            targetAccess = cache.getSettle().accessLevel.get(target.getUniqueId());
        };

        if(!hasAccess(cache.getAccess(), AccessLevel.VICE)) {
            p.sendMessage("Um die Ränge innerhalb der Stadt zu ändern musst du mindestens Vizeanführer sein.");
            return;
        }

        if(args[1].equalsIgnoreCase("add")) {
            if(targetAccess != null) {
                p.sendMessage(String.format("Der Spieler %s ist bereits Mitglied deiner Stadt.",target.getName()));
                return;
            }
        }
        else if(args[1].equalsIgnoreCase("remove")) {
            if(targetAccess == null) {
                p.sendMessage(String.format("Der Spieler %s kann nicht entfernt werden, er ist kein Mitglied deiner Stadt.",target.getName()));
                return;
            }
        }
        else if(args[1].equalsIgnoreCase("rank")) {
            if(!(args.length == 4)) {
                p.sendMessage(Chat.errorFade("Bitte nutze /t user rank <username> <rank>"));
                return;
            }
            if(targetAccess == null) {
                p.sendMessage(String.format("Der Spieler %s ist kein Mitglied deiner Stadt und kann demnach nicht befördert werden.",target.getName()));
                return;
            }
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.MAJOR);

        }
        else if(args[1].equalsIgnoreCase("resign")) {
            if(!hasAccess(cache.getAccess(), AccessLevel.MAJOR)) {
                p.sendMessage("Du müsst Bürgermeister einer Stadt sein um diese an einen anderen Spieler überschreiben zu können");
                return;
            }
            if(targetAccess == null || !hasAccess(targetAccess, AccessLevel.VICE)) {
                p.sendMessage("Du kannst deine Stadt aus Sicherheitsgründen nur an einen Vizeanführer übergeben.");
                return;
            }
            settleDB.changeMemberAccess(p.getUniqueId(), null);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.MAJOR);
        } else {
            p.sendMessage(Chat.errorFade("Bitte nutze /t user <add|remove|rank|resign> <username> (<rank>)"));
            return;
        }
    }
}
