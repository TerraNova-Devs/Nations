package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class TerraRegionCommand implements BasicCommand {

    private final Map<String, BasicCommand> subCommands = new HashMap<>();

    NationsPlugin plugin;

    public TerraRegionCommand(NationsPlugin plugin) {
        this.plugin = plugin;
        subCommands.put("create", new TerraRegionSubCommand("nations.create"));
        subCommands.put("remove", new TerraRegionSubCommand("nations.remove"));
        subCommands.put("select", new TerraSelectSubCommand("nations.select"));
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

        subCommands.get(args[0]).execute(stack, args);

    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        List<String> RegionTypes = List.of("settle","outpost","property");

        if(args.length == 0) return subCommands.keySet();
        if(args[0].equalsIgnoreCase("create")) {

        }
        if(args[0].equalsIgnoreCase("select")) {
            return NationsPlugin.settleManager.nameCache;
        }

        return List.of();
    }


}