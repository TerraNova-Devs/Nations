package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.access.AccessLevel;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public abstract class SubCommand {

    public String permission;

    public SubCommand(String permission) {
        this.permission = permission;
    }

    public static boolean hasPermission(Player p, String permission) {
        if (p.hasPermission(permission)) return true;
        p.sendMessage(Chat.errorFade(String.format("Dir fehlt zum Ausführen des Befehles die Permission '%s'.", permission)));
        return false;
    }

    protected Player isPlayer(CommandSourceStack stack) {
        if (!(stack.getSender() instanceof Player p)) {
            stack.getSender().sendMessage("Du musst für diesen Command ein Spieler sein!");
            return null;
        }
        return p;
    }

    public boolean hasAccess(AccessLevel access, AccessLevel neededAcess) {
        return access.getWeight() >= neededAcess.getWeight();
    }

    public TerraSelectCache hasSelect(Player p) {
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

    protected Collection<String> filterSuggestions(Collection<String> suggestions, String input) {
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }

}
