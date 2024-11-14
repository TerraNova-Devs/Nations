package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.settlements.AccessLevel;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public abstract class SubCommand {

    String permission;

    SubCommand(String permission) {
        this.permission = permission;
    }

    boolean hasPermission(Player p, String permission) {
        if (p.hasPermission(permission)) return true;
        p.sendMessage(Chat.errorFade(String.format("Dir fehlt zum Ausführen des Befehles die Permission '%s'.", permission)));
        return false;
    }

    Player isPlayer(CommandSourceStack stack) {
        if (!(stack.getSender() instanceof Player p)) {
            stack.getSender().sendMessage("Du musst für diesen Command ein Spieler sein!");
            return null;
        }
        return p;
    }

    boolean hasAccess(AccessLevel access, List<AccessLevel> neededAcess) {
        for (AccessLevel accessLevel : neededAcess) {
            if (accessLevel.equals(access)) return true;
        }
        return false;
    }

    TerraSelectCache hasSelect(Player p) {
        if(TerraSelectCache.selectCache.containsKey(p.getUniqueId())) return TerraSelectCache.selectCache.get(p.getUniqueId());
        p.sendMessage(Chat.errorFade("Bitte nutze für die Aktion erst ./t select <Stadtname> umd die zu betreffende Stadt auszuwählen."));
        return null;
    }

    Optional<Player> isPlayer(String arg, Player p) {
        Player target = Bukkit.getPlayer(arg);
        if (target == null || !target.isOnline()) {
            p.sendMessage(Chat.errorFade(String.format("Der angegebene Spieler '%s' konnte nicht gefunden werden.", arg)));
            return Optional.empty();
        }
        return Optional.of(target);
    }

}
