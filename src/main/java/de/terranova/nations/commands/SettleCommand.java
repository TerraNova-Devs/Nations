package de.terranova.nations.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.Settle;
import de.terranova.nations.worldguard.SettleClaim;
import de.terranova.nations.worldguard.SettleFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

public class SettleCommand implements BasicCommand, TabCompleter {

    NationsPlugin plugin;

    public SettleCommand(NationsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {


        if (!(stack.getSender() instanceof Player p)) {
            stack.getSender().sendMessage("Du musst für diesen Command ein Spieler sein!");
            return;
        }

        if (args.length == 0) {
            p.sendMessage(Chat.cottonCandy("Nations Plugin est. 13.07.2024 | written by gerryxn  | Version 1.0.0 | Copyright TerraNova."));
            return;
        }

        if (args[0].equalsIgnoreCase("testt")) {
            if (!hasPermission(p, "nations.admin.testt")) return;
            double x = (double) Integer.parseInt(args[1]) /100;
            double y = x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
            p.sendMessage(Chat.stringToComponent("" + y));
        }





        if (args[0].equalsIgnoreCase("test")) {
            if (!hasPermission(p, "nations.admin.test")) return;
            Settle settle = NationsPlugin.settleManager.getSettle(p.getLocation()).get();
            p.sendMessage("" + settle.location.asString() + ": " + settle.name);
            for (Vectore2 loc : NationsPlugin.settleManager.locations) {
                p.sendMessage(loc.asString());
            }
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!hasPermission(p, "nations.create")) return;

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


            UUID settlementID = UUID.randomUUID();
            SettleClaim.createClaim(name, p, settlementID);
            Settle newsettle = new Settle(settlementID, p.getUniqueId(), p.getLocation(), name);

            NationsPlugin.settleManager.addSettlement(settlementID, newsettle);
            SettleDBstuff.addSettlement(settlementID,name, new Vectore2(p.getLocation()), p.getUniqueId());
            p.sendMessage(Chat.greenFade("Deine Stadt " + newsettle.name + " wurde erfolgreich gegründet."));
            NationsPlugin.settleManager.addSettlementToPl3xmap(newsettle);

        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (!hasPermission(p, "nations.remove")) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
            if (access.isEmpty()) return;
            if (!access.get().equals(AccessLevelEnum.MAJOR)) return;
            NationsPlugin.settleManager.removeSettlement(settle.get().id);
            p.sendMessage(Chat.greenFade("Die Stadt " + settle.get().name + " wurde erfolgreich entfernt."));
        }

        if (args[0].equalsIgnoreCase("forceremove")) {
            if (!hasPermission(p, "nations.admin.remove")) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            NationsPlugin.settleManager.removeSettlement(settle.get().id);
            p.sendMessage(Chat.greenFade("Die Stadt " + settle.get().name + " wurde erfolgreich entfernt."));
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
            return;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            if (!hasPermission(p, "nations.claim")) return;
            Optional<ProtectedRegion> area = SettleClaim.checkSurrAreaForSettles(p);
            if (area.isEmpty()) {
                return;
            }
            ProtectedRegion protectedRegion = area.get();
            String settlementUUID = protectedRegion.getFlag(SettleFlag.SETTLEMENT_UUID_FLAG);
            assert settlementUUID != null;
            Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, UUID.fromString(settlementUUID));
            if (access.isEmpty()) {
                p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung um diese Stadt zu erweitern."));
                return;
            }
            if (!(access.get().equals(AccessLevelEnum.MAJOR) || access.get().equals(AccessLevelEnum.VICE))) {
                p.sendMessage(Chat.errorFade("Du benötigst einen höheren Rang um diese Stadt zu erweitern."));
                return;
            }
            Settle settle = NationsPlugin.settleManager.getSettle(UUID.fromString(settlementUUID)).get();
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
            if (!hasPermission(p, "nations.admin.forceclaim")) return;
            Optional<ProtectedRegion> area = SettleClaim.checkSurrAreaForSettles(p);
            if (area.isPresent()) {
                ProtectedRegion protectedRegion = area.get();
                String settlementUUID = protectedRegion.getFlag(SettleFlag.SETTLEMENT_UUID_FLAG);
                assert settlementUUID != null;
                SettleClaim.addToExistingClaim(p, protectedRegion);
                Settle settle = NationsPlugin.settleManager.getSettle(UUID.fromString(settlementUUID)).get();
                NationsPlugin.settleManager.addSettlementToPl3xmap(settle);
                settle.claims = SettleClaim.getClaimAnzahl(settle.id);

            }
        }

        if (args[0].equalsIgnoreCase("addmember")) {
            if (!hasPermission(p, "nations.addmember")) return;
            Optional<Player> target = isPlayer(args[1], p);
            if (target.isEmpty()) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
            if (access.isEmpty()) return;
            if (!hasAccess(access.get(), List.of(AccessLevelEnum.MAJOR, AccessLevelEnum.VICE))) return;
            Optional<AccessLevelEnum> newAccess;
            try {
                newAccess = settle.get().promoteOrAdd(target.get(), p);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (newAccess.isEmpty()) return;
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde zum Rang %s befördert.", PlainTextComponentSerializer.plainText().serialize(target.get().displayName()), newAccess.get())));
        }

        if (args[0].equalsIgnoreCase("removemember")) {
            if (!hasPermission(p, "nations.removemember")) return;
            Optional<Player> target = isPlayer(args[1], p);
            if (target.isEmpty()) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
            if (access.isEmpty()) return;
            if (!hasAccess(access.get(), List.of(AccessLevelEnum.MAJOR, AccessLevelEnum.VICE))) return;
            Optional<AccessLevelEnum> newAccess = null;
            try {
                newAccess = settle.get().demoteOrRemove(target.get(), p);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (newAccess.isEmpty() || newAccess.get().equals(AccessLevelEnum.REMOVE)) return;
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde zum Rang %s degradiert.", PlainTextComponentSerializer.plainText().serialize(target.get().displayName()), newAccess.get())));
        }

        if (args[0].equalsIgnoreCase("forcecreate")) {
            if (!hasPermission(p, "nations.admin.forcecreate")) return;
            if (!(args.length == 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle create <name>"));
                return;
            }
            if (!args[1].matches("[a-zA-Z0-9]*")) {
                p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen."));
                return;
            }
            String name = args[1];
            if (!NationsPlugin.settleManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            if (SettleClaim.checkAreaForSettles(p)) {
                p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
                return;
            }

            UUID settlementID = UUID.randomUUID();
            Settle newsettle = new Settle(settlementID, p.getUniqueId(), p.getLocation(), name);
            NationsPlugin.settleManager.addSettlement(settlementID, newsettle);

            SettleDBstuff.addSettlement(settlementID, name, new Vectore2(p.getLocation()), p.getUniqueId());

            SettleClaim.createClaim(name, p, settlementID);
            NationsPlugin.settleManager.addSettlementToPl3xmap(newsettle);

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

    private boolean hasAccess(AccessLevelEnum access, List<AccessLevelEnum> neededAcess) {
        for (AccessLevelEnum accessLevel : neededAcess) {
            if (accessLevel.equals(access)) return true;
        }
        return false;
    }

    private Optional<Player> isPlayer(String arg, Player p) {
        Player target = Bukkit.getPlayer(arg);
        if (target == null || !target.isOnline()) {
            p.sendMessage(Chat.errorFade(String.format("Der angegebene Spieler '%s' konnte nicht gefunden werden.", arg)));
            return Optional.empty();
        }
        return Optional.of(target);
    }

    private boolean hasPermission(Player p, String permission) {
        if (p.hasPermission(permission)) return true;
        p.sendMessage(Chat.errorFade(String.format("Dir fehlt zum Ausführen des Befehles die Permission '%s'.", permission)));
        return false;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
