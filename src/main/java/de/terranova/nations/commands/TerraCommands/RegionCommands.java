package de.terranova.nations.commands.TerraCommands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.commands.CommandAnnotation;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.base.GridRegionType;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

import static de.terranova.nations.commands.NationCommandUtil.hasAccess;
import static de.terranova.nations.commands.NationCommandUtil.hasSelect;


public class RegionCommands {

    @CommandAnnotation(
            domain = "region.create.$ARGUMENT.$ARGUMENT",
            permission = "nations.region.create",
            description = "Creates a new region",
            usage = "/terra region create <name>",
            tabCompletion = {"$REGISTERED_REGION_TYPES"}
    )
    public static boolean createRegion(Player p, String[] args) {
        String type = args[2].toLowerCase();
        String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 3, args.length)));

        if(!RegionType.registry.containsKey(type)){
            p.sendMessage(Chat.errorFade(String.format("Bitte benutze nur folgende Regionstypen: %s", RegionType.registry.keySet())));
            return false;
        }

        Optional<RegionType> regionTypeOpt = RegionType.createRegionType(type, name, p);
        if (regionTypeOpt.isPresent()) {
            //RegionType regionType = regionTypeOpt.get();
            p.sendMessage(Chat.greenFade("Region " + name + " wurde erfolgreich gegründet."));
        } else {
            p.sendMessage(Chat.errorFade("Die Erstellung der Region wurde abgebrochen."));
        }

        return true;
    }

    @CommandAnnotation(
            domain = "region.delete",
            permission = "nations.region.delete",
            description = "Removes an existing region",
            usage = "/terra region remove <name>",
            tabCompletion = {"remove"}
    )
    public static boolean removeRegion(Player p, String[] args) {
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return false;
        RegionType region = cache.getRegion();
        AccessLevel playerAccess = cache.getAccess();

        if (region == null) {
            p.sendMessage(Chat.errorFade("Keine ausgewählte Region gefunden."));
            return false;
        }

        if (playerAccess == null || !hasAccess(playerAccess, AccessLevel.MAJOR)) {
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
            usage = "/terra region claim",
            tabCompletion = {"claim"}
    )
    public static boolean claimRegion(Player p, String[] args) {
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return false;

        if(!(cache.getRegion() instanceof GridRegionType region)){
            p.sendMessage(Chat.errorFade("Du kannst die ausgewählte region nicht durch claimen erweitern"));
            return false;
        }
        AccessLevel access = cache.getAccess();
        if (!hasAccess(access, AccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung um diese Stadt zu erweitern."));
            return false;
        }


        double abstand = Integer.MAX_VALUE;
        for (Vectore2 location : NationsPlugin.settleManager.locationCache) {
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
        if(cache.getRegion() instanceof SettleRegionType){
            NationsPlugin.settleManager.addSettlementsToPl3xmap();
        }
        region.setClaims(RegionClaimFunctions.getClaimAnzahl(cache.getRegion().getId()));
        p.sendMessage(Chat.greenFade("Deine Stadt wurde erfolgreich erweitert. (" + region.getClaims() + "/" + region.getMaxClaims() + ")"));

        return true;
    }
}