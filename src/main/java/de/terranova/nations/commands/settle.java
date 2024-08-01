package de.terranova.nations.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.settlements.settlement;
import de.terranova.nations.worldguard.Vectore2;
import de.terranova.nations.worldguard.claim;
import de.terranova.nations.worldguard.settlementFlag;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        if (!p.hasPermission("terranova.nations.admin")) {
            return;
        }
        if (args[0].equalsIgnoreCase("create")) {
            if (!(args.length == 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle create <name>"));
                return;
            }
            if (args[1].length() > 20) {
                p.sendMessage(Chat.errorFade("Der Name darf nicht l\u00E4nger als 20 zeichen sein."));
                return;
            }
            String name = args[1];
            if (!plugin.settlementManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            if (claim.checkAreaForSettles(p)) {
                p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
                return;
            }
            //plugin.settlementManager.canSettle(p)
            if (true) {
                UUID settlementID = UUID.randomUUID();
                settlement newsettle = new settlement(settlementID, p.getUniqueId(), p.getLocation(), name);
                plugin.settlementManager.addSettlement(settlementID, newsettle);
                try {
                    SettleDBstuff.addSettlement(settlementID, name, new Vectore2(p.getLocation()), p.getUniqueId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                claim.createClaim(name, p, settlementID);
            } else {
                p.sendMessage(Chat.errorFade("Du hast leider keine Berechtigung eine Stadt zu gr\u00FCnden."));
            }
        }

        if (args[0].equalsIgnoreCase("tphere")) {
            Optional<settlement> settlement = plugin.settlementManager.checkIfPlayerIsWithinClaim(p);
            if (settlement.isPresent()) {
                AccessLevelEnum acess = plugin.settlementManager.getAcessLevel(p,settlement.get().id);
                if(acess.equals(AccessLevelEnum.MAJOR) || acess.equals(AccessLevelEnum.VICE)){
                    settlement.get().tpNPC(p.getLocation());
                }
            } else {
                p.sendMessage(Chat.errorFade("Zum teleportieren bitte innerhalb deines Claims stehen."));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("rename")) {
            if (!(args.length == 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle rename <name>"));
                return;
            }
            if (args[1].length() > 20) {
                p.sendMessage(Chat.errorFade("Der Name darf nicht l\u00E4nger als 20 zeichen sein."));
                return;
            }
            Optional<settlement> settlement = plugin.settlementManager.checkIfPlayerIsWithinClaim(p);
            if (settlement.isPresent()) {
                AccessLevelEnum access = plugin.settlementManager.getAcessLevel(p,settlement.get().id);
                if(access.equals(AccessLevelEnum.MAJOR) || access.equals(AccessLevelEnum.VICE)){
                    settlement.get().rename(args[1]);
                } else {
                    p.sendMessage(Chat.errorFade("Du hast nicht genuegend Berechtigung um diese Stadt umzubenennen."));
                }
            } else {
                p.sendMessage(Chat.errorFade("Zum umbenennen bitte innerhalb einer Stadt stehen."));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            if (!p.hasPermission("terranova.nations.claim")) {
                return;
            }

            Optional<ProtectedRegion> area = claim.checkSurrAreaForSettles(p);
            if (area.isPresent()) {
                ProtectedRegion protectedRegion = area.get();

                String settlementUUID = protectedRegion.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG);
                assert settlementUUID != null;
                AccessLevelEnum access = plugin.settlementManager.getAcessLevel(p,UUID.fromString(settlementUUID));


                if(access.equals(AccessLevelEnum.MAJOR) || access.equals(AccessLevelEnum.VICE)) {
                    claim.addToExistingClaim(p, protectedRegion);
                }



            }
        }

        if (args[0].equalsIgnoreCase("testt")) {
            if (!p.hasPermission("terranova.nations.admin")) {
                return;
            }

            for(settlement s : NationsPlugin.settlementManager.settlements.values()){
                System.out.println("values");
                for(UUID uuid : s.members.keySet()){
                    p.sendMessage("" + uuid.toString());
                }

            }


        }

        if (args[0].equalsIgnoreCase("test")) {
            if (!p.hasPermission("terranova.nations.admin")) {
                return;
            }

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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
