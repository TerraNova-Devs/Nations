package de.terranova.nations.regions.access;

import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.base.TerraSelectCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class AccessCommands {

    public AccessCommands(){

    }

    @CommandAnnotation(
            domain = "access.add.$ONLINEPLAYERS",
            permission = "nations.access.ranks",
            description = "Fügt den ausgewählten Spieler deiner Region hinzu",
            usage = "/terra add <player>"
    )
    public boolean addPlayer(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        AccessControlled access = getAccessControlledRegion(p, cache);
        if (access == null) return false;

        access.getAccess();
        if (!Access.hasAccess(cache.getAccess(), AccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Um die Ränge innerhalb der Stadt zu ändern musst du mindestens Vizeanführer sein."));
            return false;
        }

        Player target = getTargetPlayer(p, args, 2);
        if (target == null) return false;

        if (access.getAccess().getAccessLevel(target.getUniqueId()) != null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist bereits Mitglied deiner Stadt.", target.getName())));
            return false;
        }

        cache.getRegion().addMember(target.getUniqueId());
        access.getAccess().setAccessLevel(target.getUniqueId(), AccessLevel.CITIZEN);

        sendSuccessMessages(p, target, cache.getRegion().getName(), "hinzugefügt");
        return true;
    }

    @CommandAnnotation(
            domain = "access.remove.$ONLINEPLAYERS",
            permission = "nations.access.ranks",
            description = "Entfernt den ausgewählten Spieler von deiner Region",
            usage = "/terra remove <player>"
    )
    public boolean removePlayer(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        AccessControlled access = getAccessControlledRegion(p, cache);
        if (access == null) return false;

        access.getAccess();
        if (!Access.hasAccess(cache.getAccess(), AccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Um die Ränge innerhalb der Stadt zu ändern musst du mindestens Vizeanführer sein."));
            return false;
        }

        Player target = getTargetPlayer(p, args, 2);
        if (target == null) return false;

        if (access.getAccess().getAccessLevel(target.getUniqueId()) == null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist kein Mitglied deiner Stadt.", target.getName())));
            return false;
        }

        cache.getRegion().removeMember(target.getUniqueId());
        access.getAccess().removeAccess(target.getUniqueId());
        sendSuccessMessages(p, target, cache.getRegion().getName(), "entfernt");
        return true;
    }

    @CommandAnnotation(
            domain = "access.rank.$ONLINEPLAYERS.$RANKS",
            permission = "nations.access.ranks",
            description = "Setzt den Rang eines Spielers",
            usage = "/terra rank <player> <rank>"
    )
    public boolean rankPlayer(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        AccessControlled access = getAccessControlledRegion(p, cache);
        if (access == null) return false;

        access.getAccess();
        if (!Access.hasAccess(cache.getAccess(), AccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Um die Ränge innerhalb der Stadt zu ändern musst du mindestens Vizeanführer sein."));
            return false;
        }

        Player target = getTargetPlayer(p, args, 2);
        if (target == null) return false;

        AccessLevel newRank = getAccessLevelFromArgs(p, args, 3);
        if (newRank == null) return false;

        if (access.getAccess().getAccessLevel(target.getUniqueId()) == null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist kein Mitglied deiner Stadt.", target.getName())));
            return false;
        }

        if(newRank.equals(AccessLevel.MAJOR) || newRank.equals(AccessLevel.ADMIN)) {
            p.sendMessage(Chat.errorFade("Du kannst den Stadtbesitzer nicht ändern."));
            return false;
        }

        if(target.getUniqueId() == p.getUniqueId() && cache.getAccess().equals(AccessLevel.MAJOR)) {
            p.sendMessage(Chat.errorFade("Du kannst deinen eigenen Rang nicht ändern!"));
            return false;
        }

        access.getAccess().setAccessLevel(target.getUniqueId(), newRank);
        p.sendMessage(Chat.greenFade(String.format("Du hast %s erfolgreich auf den Rang %s gestuft.", target.getName(), newRank.name())));
        return true;
    }

    private AccessControlled getAccessControlledRegion(Player p, TerraSelectCache cache) {
        if (cache == null) return null;
        if (!(cache.getRegion() instanceof AccessControlled access)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Ränge"));
            return null;
        }
        return access;
    }

    private Player getTargetPlayer(Player p, String[] args, int index) {
        if (args.length <= index) {
            p.sendMessage(Chat.errorFade("Bitte gib den Spielernamen an."));
            return null;
        }
        Player target = Bukkit.getPlayer(args[index]);
        if (target == null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s konnte nicht gefunden werden", args[index])));
        }
        return target;
    }

    private AccessLevel getAccessLevelFromArgs(Player p, String[] args, int index) {
        if (args.length <= index) {
            p.sendMessage(Chat.errorFade("Bitte gib ein gültiges AccessLevel an."));
            return null;
        }
        for (AccessLevel level : AccessLevel.values()) {
            if (level.name().equalsIgnoreCase(args[index])) {
                return level;
            }
        }
        p.sendMessage(Chat.errorFade(String.format("Das AccessLevel %s konnte nicht gefunden werden", args[index])));
        return null;
    }

    private void sendSuccessMessages(Player p, Player target, String regionName, String action) {
        target.sendMessage(Chat.greenFade(String.format("Du wurdest von der Region %s %s.", regionName, action)));
        p.sendMessage(Chat.greenFade(String.format("Du hast %s erfolgreich von der Stadt %s %s.", target.getName(), regionName, action)));
    }
}
