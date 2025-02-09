package de.terranova.nations.regions.base;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.pl3xmap.RegionLayer;
import de.terranova.nations.regions.access.Access;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;



public class RegionCommands {

    @CommandAnnotation(
            domain = "region.create.$REGISTERED_REGION_TYPES.%<name>",
            permission = "nations.region.create",
            description = "Creates a new region",
            usage = "/terra region create <type> <name>"
    )
    public static boolean createRegion(Player p, String[] args) {
        String type = args[2].toLowerCase();
        String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 3, args.length)));

        if(!Region.registry.containsKey(type)){
            p.sendMessage(Chat.errorFade(String.format("Bitte benutze nur folgende Regionstypen: %s", Region.registry.keySet())));
            return false;
        }

        Optional<Region> regionTypeOpt = Region.createRegion(type, name ,p);
        if (regionTypeOpt.isPresent()) {
            p.sendMessage(Chat.greenFade("Region " + name + " wurde erfolgreich gegründet."));
        } else {
            p.sendMessage(Chat.errorFade("Die Erstellung der Region wurde abgebrochen."));
        }

        return true;
    }

    @CommandAnnotation(
            domain = "region.remove",
            permission = "nations.region.delete",
            description = "Removes an existing region",
            usage = "/terra region remove"
    )
    public static boolean removeRegion(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;
        Region region = cache.getRegion();
        AccessLevel playerAccess = cache.getAccess();

        if (region == null) {
            p.sendMessage(Chat.errorFade("Keine ausgewählte Region gefunden."));
            return false;
        }

        if (playerAccess == null || !Access.hasAccess(playerAccess, AccessLevel.MAJOR)) {
            p.sendMessage(Chat.errorFade("You do not have the required access level to remove this settlement."));
            return false;
        }

        region.remove();
        p.sendMessage(Chat.greenFade("Die Stadt " + region.getName() + " wurde erfolgreich entfernt."));

        return true;
    }

    @CommandAnnotation(
            domain = "region.claim",
            permission = "nations.region.claim",
            description = "Adds Claimes to GridRegions",
            usage = "/terra region claim"
    )
    public static boolean claimRegion(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;

        if(!(cache.getRegion() instanceof GridRegion region)){
            p.sendMessage(Chat.errorFade("Du kannst die ausgewählte region nicht durch claimen erweitern"));
            return false;
        }
        AccessLevel access = cache.getAccess();
        if (!Access.hasAccess(access, AccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung um diese Stadt zu erweitern."));
            return false;
        }


        double abstand = Integer.MAX_VALUE;
        for (Vectore2 location : GridRegion.locationCache) {
            if (region.getLocation().equals(location)) continue;
            double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (abstand == Integer.MAX_VALUE || abstand > abstandneu) {
                abstand = abstandneu;
            }
        }
        if (abstand < 750) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>750<#FFD7FE> Bl\u00F6cke Abstand zum Stadtzentrum muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
            return false;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));
        if (set.size() != 0) {
            p.sendMessage(Chat.errorFade("Du kannst nicht auf der Region eines anderen Spielers claimen!."));
            p.sendMessage(Chat.errorFade("Überlappende Regionen: " + set));
            return false;
        }

        if (region.getClaims() >= region.getMaxClaims()) {
            p.sendMessage(Chat.errorFade("Du hast bereits die maximale Anzahl an Claims für dein Stadtlevel erreicht."));
            return false;
        }

        RegionClaimFunctions.addToExistingClaim(p, cache.getRegion().getWorldguardRegion());

        region.setClaims(RegionClaimFunctions.getClaimAnzahl(cache.getRegion().getId()));
        if(cache.getRegion() instanceof SettleRegion settle){
            RegionLayer.updateRegion(settle);
        }
        p.sendMessage(Chat.greenFade("Deine Stadt wurde erfolgreich erweitert. (" + region.getClaims() + "/" + region.getMaxClaims() + ")"));

        return true;
    }
    @CommandAnnotation(
            domain = "region.rename.%<name>",
            permission = "nations.region.rename",
            description = "Renames a Region",
            usage = "/terra region rename <name>"
    )
    public static boolean renameRegion(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;
        AccessLevel access = cache.getAccess();
        if (!Access.hasAccess(access, AccessLevel.MAJOR)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung um diese Stadt zu erweitern."));
            return false;
        }
        String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 2, args.length))).toLowerCase();
        if (!name.matches("^(?!.*__)(?!_)(?!.*_$)(?!.*(.)\\1{3,})[a-zA-Z0-9_]{3,20}$")) {
            p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
            return false;
        }

        if (Region.getNameCache().contains(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return false;
        }

        cache.getRegion().rename(name);
        return true;
    }




}
