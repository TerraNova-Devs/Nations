package de.terranova.nations.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.PropertyTypeClasses.SettlementPropertyType;
import de.terranova.nations.worldguard.SettleClaim;
import de.terranova.nations.worldguard.SettleFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class SettleClaimSubCommand extends SubCommand implements BasicCommand {

    SettleClaimSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        Player p = isPlayer(commandSourceStack);
        if(p == null) return;
        if(!hasPermission(p, permission)) return;

        if (args[0].equalsIgnoreCase("claim")) {
            if (!hasPermission(p, "nations.claim")) return;
            Optional<ProtectedRegion> area = SettleClaim.checkSurrAreaForSettles(p);
            if (area.isEmpty()) {
                return;
            }
            ProtectedRegion protectedRegion = area.get();
            String settlementUUID = protectedRegion.getFlag(SettleFlag.SETTLEMENT_UUID_FLAG);
            assert settlementUUID != null;
            Optional<AccessLevel> access = NationsPlugin.settleManager.getAccessLevel(p, UUID.fromString(settlementUUID));
            if (access.isEmpty()) {
                p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung um diese Stadt zu erweitern."));
                return;
            }
            if (!(access.get().equals(AccessLevel.MAJOR) || access.get().equals(AccessLevel.VICE))) {
                p.sendMessage(Chat.errorFade("Du benötigst einen höheren Rang um diese Stadt zu erweitern."));
                return;
            }
            SettlementPropertyType settle = NationsPlugin.settleManager.getSettle(UUID.fromString(settlementUUID)).get();
            double abstand = Integer.MAX_VALUE;
            for (Vectore2 location : NationsPlugin.settleManager.locations) {
                if (settle.location.equals(location)) continue;
                double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
                if (abstand == Integer.MAX_VALUE || abstand > abstandneu) {
                    abstand = abstandneu;
                }
            }
            if (abstand < 750) {
                p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>750<#FFD7FE> Bl\u00F6cke Abstand muss eingehalten werden."));
                p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
                return;
            }
            if (NationsPlugin.settleManager.getSettle(p.getLocation()).isPresent()) {
                p.sendMessage(Chat.errorFade("Dieses Claim gehört bereits einer Stadt an."));
                return;
            }
            if (settle.claims >= settle.getMaxClaims()) {
                p.sendMessage(Chat.errorFade("Du hast bereits die maximale Anzahl an Claims für dein Stadtlevel erreicht."));
                return;
            }

            SettleClaim.addToExistingClaim(p, protectedRegion);
            NationsPlugin.settleManager.addSettlementToPl3xmap(settle);
            settle.claims = SettleClaim.getClaimAnzahl(settle.id);
            settle.region = settle.getWorldguardRegion();
            p.sendMessage(Chat.greenFade("Deine Stadt wurde erfolgreich erweitert. (" + settle.claims + "/" + settle.getMaxClaims() + ")"));
        }

        if (args[0].equalsIgnoreCase("forceclaim")) {
            if(NationsPlugin.debug) {
                NationsPlugin.logger.info("Claimer: " + p.getName());
            }

            if (!hasPermission(p, "nations.admin.forceclaim")) return;
            Optional<ProtectedRegion> area = SettleClaim.checkSurrAreaForSettles(p);
            if (area.isPresent()) {
                ProtectedRegion protectedRegion = area.get();
                String settlementUUID = protectedRegion.getFlag(SettleFlag.SETTLEMENT_UUID_FLAG);
                assert settlementUUID != null;
                SettleClaim.addToExistingClaim(p, protectedRegion);
                SettlementPropertyType settle = NationsPlugin.settleManager.getSettle(UUID.fromString(settlementUUID)).get();
                NationsPlugin.settleManager.addSettlementToPl3xmap(settle);
                settle.claims = SettleClaim.getClaimAnzahl(settle.id);

            }
        }
    }
}
