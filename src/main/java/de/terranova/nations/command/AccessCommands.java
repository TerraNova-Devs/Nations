package de.terranova.nations.command;

import de.terranova.nations.command.commands.CommandAnnotation;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessControlled;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.base.TerraSelectCache;
import de.terranova.nations.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AccessCommands {

    public AccessCommands(){

    }

    static Map<UUID, UUID> invites = new HashMap<>();

    @CommandAnnotation(
            domain = "access.invite.$ONLINEPLAYERS",
            permission = "nations.access.ranks",
            description = "Lädt den angegebenen Spieler in deine Stadt ein.",
            usage = "/terra access invite <player>"
    )
    public boolean invitePlayer(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        AccessControlled access = getAccessControlledRegion(p, cache);
        if (access == null) return false;

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
        if(invites.containsKey(target.getUniqueId())){
            if(invites.get(target.getUniqueId()).equals(cache.getRegion().getId())){
                p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist bereits eingeladen.", target.getName())));
                return false;
            }
        }
        invites.put(target.getUniqueId(), cache.getRegion().getId());
        p.sendMessage(Chat.greenFade("Du hast " + target.getName() + " erfolgreich in die Stadt " + cache.getRegion().getName() + " eigeladen."));
        target.sendMessage(Chat.cottonCandy("Du wurdest von " + p.getName() + " in die Stadt " + cache.getRegion().getName() + " eigeladen."));
        return true;
    }

    @CommandAnnotation(
            domain = "access.accept",
            permission = "nations.access.ranks",
            description = "Nimmt die Einladung einer Stadt an.",
            usage = "/terra access accept"
    )
    public boolean acceptPlayer(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        AccessControlled access = getAccessControlledRegion(p, cache);
        if (access == null) return false;

        if(!invites.containsKey(p.getUniqueId())){
            p.sendMessage(Chat.errorFade("Es wurden keine Einladungen für dich gefunden!"));
            return false;
        }
        if(!invites.get(p.getUniqueId()).equals(cache.getRegion().getId())){
            p.sendMessage(Chat.errorFade("Für die von dir ausgewählte Region wurden keine Einladungen für dich gefunden!"));
            return false;
        }

        access.getAccess().broadcast(p.getName() + " ist erfolgreich der Stadt " + cache.getRegion().getName() + " beigetreten.",AccessLevel.CITIZEN);
        cache.getRegion().addMember(p.getUniqueId());
        access.getAccess().setAccessLevel(p.getUniqueId(), AccessLevel.CITIZEN);
        p.sendMessage(Chat.greenFade("Du bist erfolgreich der Stadt " + cache.getRegion().getName() + " beigetreten."));

        TerraSelectCache.renewSelect(p);
        return true;
    }

    @CommandAnnotation(
            domain = "access.remove.$REGION_ACCESS_USERS",
            permission = "nations.access.ranks",
            description = "Entfernt den ausgewählten Spieler von deiner Region",
            usage = "/terra remove <player>"
    )
    public boolean removePlayer(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        AccessControlled access = getAccessControlledRegion(p, cache);
        if (access == null) return false;

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

        if(access.getAccess().getAccessLevel(target.getUniqueId()).getWeight() >= access.getAccess().getAccessLevel(p.getUniqueId()).getWeight()){
            p.sendMessage(Chat.errorFade("Du kannst keinen Spieler entfernen der höher gleich du im Rang ist."));
            return false;
        }

        if(!Access.hasAccess(access.getAccess().getAccessLevel(p.getUniqueId()), AccessLevel.VICE)){
            p.sendMessage(Chat.errorFade("Du musst mindestens Vize sein um einen Spieler von der Stadt zu entfernen."));
            return false;
        }

        cache.getRegion().removeMember(target.getUniqueId());
        access.getAccess().removeAccess(target.getUniqueId());
        access.getAccess().broadcast(target.getName() + " wurde von " + p.getName() + " der Stadt " + cache.getRegion().getName() + " verwiesen.",AccessLevel.CITIZEN);
        TerraSelectCache.renewSelect(target);
        return true;
    }

    @CommandAnnotation(
            domain = "access.leave",
            permission = "nations.access.ranks",
            description = "Verlässt eine Stadt.",
            usage = "/terra leave"
    )
    public boolean leave(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        AccessControlled access = getAccessControlledRegion(p, cache);
        if (access == null) return false;

        if(Access.hasAccess(cache.getAccess(), AccessLevel.MAJOR) || Access.hasAccess(cache.getAccess(), AccessLevel.ADMIN)){
            p.sendMessage(Chat.errorFade("Der Major kann seine Stadt nicht verlassen!"));
            return false;
        }

        cache.getRegion().removeMember(p.getUniqueId());
        access.getAccess().removeAccess(p.getUniqueId());
        access.getAccess().broadcast(p.getName() + " hat die Stadt " + cache.getRegion().getName() + " verlassen.",AccessLevel.CITIZEN);
        TerraSelectCache.renewSelect(p);
        return true;
    }

    @CommandAnnotation(
            domain = "access.rank.$REGION_ACCESS_USERS.$RANKS",
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

        AccessLevel newAccess = getAccessLevelFromArgs(p, args, 3);
        if (newAccess == null) return false;

        if (access.getAccess().getAccessLevel(target.getUniqueId()) == null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist kein Mitglied deiner Stadt.", target.getName())));
            return false;
        }

        if(newAccess.equals(AccessLevel.MAJOR) || newAccess.equals(AccessLevel.ADMIN)) {
            p.sendMessage(Chat.errorFade("Du kannst den Stadtbesitzer nicht ändern."));
            return false;
        }

        if(target.getUniqueId() == p.getUniqueId() && cache.getAccess().equals(AccessLevel.MAJOR)) {
            p.sendMessage(Chat.errorFade("Du kannst deinen eigenen Rang nicht ändern!"));
            return false;
        }

        if(access.getAccess().getAccessLevel(target.getUniqueId()) == newAccess) {
            p.sendMessage(Chat.errorFade("Der Spieler " + target.getName() + " ist bereits auf dem Rang " + newAccess.name() +"."));
            return false;
        }

        if(access.getAccess().getAccessLevel(target.getUniqueId()).getWeight() >= access.getAccess().getAccessLevel(p.getUniqueId()).getWeight()){
            p.sendMessage(Chat.errorFade("Du kannst nicht den Rang eines Spielers ändern der höher oder gleich ist als deiner selbst."));
            return false;
        }

        if(!Access.hasAccess(access.getAccess().getAccessLevel(p.getUniqueId()), AccessLevel.VICE)){
            p.sendMessage(Chat.errorFade("Du musst mindestens Vize sein um access level ändern zu können."));
            return false;
        }

        access.getAccess().setAccessLevel(target.getUniqueId(), newAccess);
        p.sendMessage(Chat.greenFade(String.format("Du hast %s erfolgreich auf den Rang %s gestuft.", target.getName(), newAccess.name())));
        TerraSelectCache.renewSelect(target);
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
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[index]);
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
}
