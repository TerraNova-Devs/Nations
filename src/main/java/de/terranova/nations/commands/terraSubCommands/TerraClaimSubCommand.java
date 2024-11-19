package de.terranova.nations.commands.terraSubCommands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.commands.SubCommand;
import de.terranova.nations.commands.TerraSelectCache;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TerraClaimSubCommand extends SubCommand implements BasicCommand {
    public TerraClaimSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Player p = isPlayer(commandSourceStack);
        if (p == null) return;
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return;

        if (args[0].equalsIgnoreCase("claim")) {

            AccessLevel access = cache.getAccess();
            if (!hasAccess(access, AccessLevel.VICE)) {
                p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung um diese Stadt zu erweitern."));
                return;
            }


            double abstand = Integer.MAX_VALUE;
            for (Vectore2 location : NationsPlugin.settleManager.locationCache) {
                if (cache.getSettle().location.equals(location)) continue;
                double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
                if (abstand == Integer.MAX_VALUE || abstand > abstandneu) {
                    abstand = abstandneu;
                }
            }
            if (abstand < 750) {
                p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>750<#FFD7FE> Bl\u00F6cke Abstand zum Stadtzentrum muss eingehalten werden."));
                p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
                return;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));
            if (set.size() != 0) {
                p.sendMessage(Chat.errorFade("Du kannst nicht auf der Region eines anderen Spielers claimen!."));
                p.sendMessage(Chat.errorFade("Überlappende Regionen: " + set));
                return;
            }

            if (cache.getSettle().claims >= cache.getSettle().getMaxClaims()) {
                p.sendMessage(Chat.errorFade("Du hast bereits die maximale Anzahl an Claims für dein Stadtlevel erreicht."));
                return;
            }

            RegionClaimFunctions.addToExistingClaim(p, cache.getSettle().region);
            NationsPlugin.settleManager.addSettlementToPl3xmap(cache.getSettle());
            cache.getSettle().claims = RegionClaimFunctions.getClaimAnzahl(cache.getSettle().id);
            cache.getSettle().region = cache.getSettle().getWorldguardRegion();
            p.sendMessage(Chat.greenFade("Deine Stadt wurde erfolgreich erweitert. (" + cache.getSettle().claims + "/" + cache.getSettle().getMaxClaims() + ")"));
        }
    }
}
