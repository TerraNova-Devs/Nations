package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.Settle;
import de.terranova.nations.worldguard.SettleClaim;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SettleCreateSubCommand extends SubCommand implements BasicCommand {

    SettleCreateSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Player p = isPlayer(commandSourceStack);
        if(p == null) return;
        if(!hasPermission(p, permission)) return;

        if (args[0].equalsIgnoreCase("create")) {
            if (!(args.length >= 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle rename <name>"));
                return;
            }
            String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length)));
            if (!name.matches("^[a-zA-Z0-9_]{1,20}$")) {
                p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
                return;
            }
            List<String> biomeblacklist = new ArrayList<>(Arrays.asList("RIVER", "DEEP_COLD_OCEAN", "COLD_OCEAN", "DEEP_LUKEWARM_OCEAN", "LUKEWARM_OCEAN", "OCEAN", "DEEP_OCEAN", "WARM_OCEAN", "DEEP_WARM_OCEAN", "BEACH", "GRAVEL_BEACH", "SNOWY_BEACH"));
            String currentbiome = p.getWorld().getBiome(p.getLocation()).toString();
            for (String biome : biomeblacklist) {
                if (biome.equalsIgnoreCase(currentbiome)) {
                    p.sendMessage(Chat.errorFade("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"));
                    return;
                }
            }
            if (!NationsPlugin.settleManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            if (SettleClaim.checkAreaForSettles(p)) {
                p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
                return;
            }
            double abstand = Integer.MAX_VALUE;
            for (Vectore2 location : NationsPlugin.settleManager.locations) {
                double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
                if (abstand == Integer.MAX_VALUE || abstand > abstandneu) {
                    abstand = abstandneu;

                }
            }
            if (abstand < 2000) {
                p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>2000<#FFD7FE> Bl\u00F6cke Abstand muss eingehalten werden."));
                p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
                return;
            }

            Settle newsettle = new Settle(name, p);

            NationsPlugin.settleManager.addSettlement(newsettle.id, newsettle);
            SettleDBstuff.addSettlement(newsettle.id, name, new Vectore2(p.getLocation()), p.getUniqueId());
            p.sendMessage(Chat.greenFade("Deine Stadt " + newsettle.name + " wurde erfolgreich gegründet."));
            NationsPlugin.settleManager.addSettlementToPl3xmap(newsettle);

        }

        if (args[0].equalsIgnoreCase("rename")) {
            if (!hasPermission(p, "nations.rename")) return;
            if (!(args.length >= 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle rename <name>"));
                return;
            }
            String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length)));
            if (!name.matches("^[a-zA-Z0-9_]{1,20}$")) {
                p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
                return;
            }
            if (!NationsPlugin.settleManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isPresent()) {
                Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
                if (access.isEmpty()) return;
                if (access.get().equals(AccessLevelEnum.MAJOR)) {
                    settle.get().rename(name);
                    p.sendMessage(name);
                } else {
                    p.sendMessage(Chat.errorFade("Du hast nicht genuegend Berechtigung um diese Stadt umzubenennen."));
                }
            } else {
                p.sendMessage(Chat.errorFade("Zum umbenennen bitte innerhalb einer Stadt stehen."));
            }
        }
        if (args[0].equalsIgnoreCase("tphere")) {
            if (!hasPermission(p, "nations.tphere")) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());

            if (settle.isPresent()) {
                Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
                if (access.isEmpty()) return;
                if (access.get().equals(AccessLevelEnum.MAJOR) || access.get().equals(AccessLevelEnum.VICE)) {
                    settle.get().tpNPC(p.getLocation());
                }
            } else {
                p.sendMessage(Chat.errorFade("Zum teleportieren bitte innerhalb deines Claims stehen."));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("unshow")) {
            if (!hasPermission(p, "worldedit.analysis.sel")) return;
            Bukkit.getServer().dispatchCommand(p, "/sel");
        }

        if (args[0].equalsIgnoreCase("show")) {
            if (!hasPermission(p, "worldguard.region.select.*")) return;
            Bukkit.getServer().dispatchCommand(p, "rg sel");
        }
    }
}
