package de.terranova.nations.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.settlements.settlement;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import de.terranova.nations.worldguard.settlementClaim;
import de.terranova.nations.worldguard.settlementFlag;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class settle implements BasicCommand, TabCompleter {

    NationsPlugin plugin;

    public settle(NationsPlugin plugin) {
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

        if (args[0].equalsIgnoreCase("create")) {
            if (!hasPermission(p, "nations.create")) return;

            if (!(args.length == 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle create <name>"));
                return;
            }
            if (args[1].length() > 20) {
                p.sendMessage(Chat.errorFade("Der Name darf nicht l\u00E4nger als 20 zeichen sein."));
                return;
            }
            if (!args[1].matches("[a-zA-Z0-9]*")) {
                p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen."));
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

            String name = args[1];
            if (!NationsPlugin.settlementManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            if (settlementClaim.checkAreaForSettles(p)) {
                p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
                return;
            }
            double abstand = Integer.MAX_VALUE;
            for (Vectore2 location : NationsPlugin.settlementManager.locations) {
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
            //plugin.settlementManager.canSettle(p)
            if (true) {
                p.sendMessage("ANGEKOMMEN");
                UUID settlementID = UUID.randomUUID();
                settlement newsettle = new settlement(settlementID, p.getUniqueId(), p.getLocation(), name);
                NationsPlugin.settlementManager.addSettlement(settlementID, newsettle);
                try {
                    SettleDBstuff.addSettlement(settlementID, name, new Vectore2(p.getLocation()), p.getUniqueId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                settlementClaim.createClaim(name, p, settlementID);
                NationsPlugin.settlementManager.addSettlementToPl3xmap(newsettle);
            } else {
                p.sendMessage(Chat.errorFade("Du hast leider keine Berechtigung eine Stadt zu gr\u00FCnden."));
            }
        }

        if (args[0].equalsIgnoreCase("tphere")) {
            if (!hasPermission(p, "nations.tphere")) return;
            Optional<settlement> settle = NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(p);

            if (settle.isPresent()) {
                Optional<AccessLevelEnum> access = NationsPlugin.settlementManager.getAccessLevel(p, settle.get().id);
                if(access.isEmpty()) return;
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
            if (!(args.length == 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle rename <name>"));
                return;
            }
            if (args[1].length() > 20) {
                p.sendMessage(Chat.errorFade("Der Name darf nicht l\u00E4nger als 20 zeichen sein."));
                return;
            }
            if (!args[1].matches("[a-zA-Z0-9]*")) {
                p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen."));
                return;
            }
            String name = args[1];
            if (!NationsPlugin.settlementManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            Optional<settlement> settle = NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(p);
            if (settle.isPresent()) {
                Optional<AccessLevelEnum> access = NationsPlugin.settlementManager.getAccessLevel(p, settle.get().id);
                if(access.isEmpty()) return;
                if (access.get().equals(AccessLevelEnum.MAJOR) || access.get().equals(AccessLevelEnum.VICE)) {
                    settle.get().rename(args[1]);
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
            Optional<ProtectedRegion> area = settlementClaim.checkSurrAreaForSettles(p);
            if (area.isPresent()) {
                ProtectedRegion protectedRegion = area.get();
                String settlementUUID = protectedRegion.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG);
                assert settlementUUID != null;
                Optional<AccessLevelEnum> access = NationsPlugin.settlementManager.getAccessLevel(p, UUID.fromString(settlementUUID));
                if(access.isEmpty()) return;
                if (access.get().equals(AccessLevelEnum.MAJOR) || access.get().equals(AccessLevelEnum.VICE)) {
                    settlement settle = NationsPlugin.settlementManager.getSettlement(UUID.fromString(settlementUUID));
                    double abstand = Integer.MAX_VALUE;
                    for (Vectore2 location : NationsPlugin.settlementManager.locations) {
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

                    if (NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(p).isPresent()) {
                        p.sendMessage(Chat.errorFade("Dieses Claim gehört bereits einer Stadt an."));
                        return;
                    }
                    if (settle.claims >= 9) {
                        p.sendMessage(Chat.errorFade("Du hast bereits die maximale Anzahl an Claims für dein Stadtlevel erreicht."));
                        return;
                    }
                    settlementClaim.addToExistingClaim(p, protectedRegion);

                    NationsPlugin.settlementManager.addSettlementToPl3xmap(settle);
                    settle.claims = settlementClaim.getClaimAnzahl(settle.id);
                }
            }
        }

        if (args[0].equalsIgnoreCase("forceclaim")) {
            if (!hasPermission(p, "nations.admin.forceclaim")) return;
            Optional<ProtectedRegion> area = settlementClaim.checkSurrAreaForSettles(p);
            if (area.isPresent()) {
                ProtectedRegion protectedRegion = area.get();
                String settlementUUID = protectedRegion.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG);
                assert settlementUUID != null;
                settlementClaim.addToExistingClaim(p, protectedRegion);
                settlement settle = NationsPlugin.settlementManager.getSettlement(UUID.fromString(settlementUUID));
                NationsPlugin.settlementManager.addSettlementToPl3xmap(settle);
                settle.claims = settlementClaim.getClaimAnzahl(settle.id);

            }
        }

        if (args[0].equalsIgnoreCase("addmember")) {
            if (!hasPermission(p, "nations.addmember")) return;
            Optional<Player> target = isPlayer(args[1],p);
            if(target.isEmpty()) return;
            Optional<settlement> settle = NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(p);
            if(settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settlementManager.getAccessLevel(p, settle.get().id);
            if(access.isEmpty()) return;
            if (!hasAccess(access.get(),List.of(AccessLevelEnum.MAJOR,AccessLevelEnum.VICE))) return;
            Optional<AccessLevelEnum> newAccess = null;
            try {
                newAccess = settle.get().promoteOrAdd(target.get());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(newAccess.isEmpty()) {
                p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den höchstmöglichen Rang erreicht.", target.get().displayName())));
                return;
            }
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde zum Rang %s befördert.",target.get().displayName(),newAccess.get())));
        }

        if (args[0].equalsIgnoreCase("removemember")) {
            if (!hasPermission(p, "nations.removemember")) return;
            Optional<Player> target = isPlayer(args[1],p);
            if(target.isEmpty()) return;
            Optional<settlement> settle = NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(p);
            if(settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settlementManager.getAccessLevel(p, settle.get().id);
            if(access.isEmpty()) return;
            if (!hasAccess(access.get(),List.of(AccessLevelEnum.MAJOR,AccessLevelEnum.VICE))) return;
            Optional<AccessLevelEnum> newAccess = null;
            try {
                newAccess = settle.get().demoteOrRemove(target.get());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(newAccess.isEmpty()) {
                p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den höchstmöglichen Rang erreicht.", target.get().displayName())));
                return;
            }
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde zum Rang %s befördert.",target.get().displayName(),newAccess.get())));
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
            if (!NationsPlugin.settlementManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            if (settlementClaim.checkAreaForSettles(p)) {
                p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
                return;
            }

            UUID settlementID = UUID.randomUUID();
            settlement newsettle = new settlement(settlementID, p.getUniqueId(), p.getLocation(), name);
            NationsPlugin.settlementManager.addSettlement(settlementID, newsettle);
            try {
                SettleDBstuff.addSettlement(settlementID, name, new Vectore2(p.getLocation()), p.getUniqueId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            settlementClaim.createClaim(name, p, settlementID);
            NationsPlugin.settlementManager.addSettlementToPl3xmap(newsettle);

        }

        if (args[0].equalsIgnoreCase("unshow")) {
            if (!hasPermission(p, "worldedit.analysis.sel")) return;
            Bukkit.getServer().dispatchCommand(p, "/sel");
        }

        if (args[0].equalsIgnoreCase("show")) {
            if (!hasPermission(p, "worldguard.region.select.*")) return;
            Bukkit.getServer().dispatchCommand(p, "rg sel");
        }

        if (args[0].equalsIgnoreCase("testt")) {
            if (!hasPermission(p, "nations.admin.testt")) return;

        }

        if (args[0].equalsIgnoreCase("test")) {
  
            if (!hasPermission(p, "nations.admin.test")) return;

            File file = new File(plugin.getDataFolder(), "level.yml");

            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setTagInspector(tag -> true);


            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setSplitLines(false);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Representer representer = new Representer(options);
            representer.addClassTag(Objective.class, Tag.MAP);

            Yaml yamlDumper = new Yaml(representer);
            Yaml yamlLoader = new Yaml(loaderOptions);

            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                List<Objective> objectives = yamlLoader.load(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            List<Objective> objectives = new LinkedList<>();
            Objective o = new Objective();
            o.setMaterial_a(Material.SADDLE);
            o.setMaterial_b(Material.SADDLE);
            o.setMaterial_c(Material.SADDLE);
            o.setMaterial_d(Material.SADDLE);
            o.setObjective_a(1);
            o.setObjective_b(1);
            o.setObjective_c(1);
            o.setObjective_d(1);
            objectives.add(o);
            Objective l = new Objective();
            l.setMaterial_a(Material.SADDLE);
            l.setMaterial_b(Material.SADDLE);
            l.setMaterial_c(Material.SADDLE);
            l.setMaterial_d(Material.SADDLE);
            l.setObjective_a(1);
            l.setObjective_b(1);
            l.setObjective_c(1);
            l.setObjective_d(1);
            objectives.add(l);

            try {
                FileWriter writer = new FileWriter(file);
                yamlDumper.dump(objectives, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            /* load specific class
            var loaderoptions = new LoaderOptions();
TagInspector taginspector =
    tag -> tag.getClassName().equals(User.class.getName());
loaderoptions.setTagInspector(taginspector);
Yaml yaml = new Yaml(new Constructor(User.class, loaderoptions));
             */


        }


    }

    private boolean hasAccess(AccessLevelEnum access, List<AccessLevelEnum> neededAcess) {
        for (AccessLevelEnum accessLevel : neededAcess) {
            if (accessLevel.equals(access)) return true;
        }
        return false;
    }

    private Optional<Player> isPlayer(String arg,Player p) {
        Player target = Bukkit.getPlayer(arg);
        if(target == null || !target.isOnline()) {
            p.sendMessage(Chat.errorFade(String.format("Der angegebene Spieler '%s' konnte nicht gefunden werden.",arg)));
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
