package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.commands.terraSubCommands.*;
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
        subCommands.put("user", new TerraAccessSubCommand("nations.access"));
        subCommands.put("claim", new TerraClaimSubCommand("nations.claim"));
        subCommands.put("manage", new TerraManageSubCommand("nations.manage"));
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
        if(subCommands.containsKey(args[0])) {
            subCommands.get(args[0]).execute(stack, args);
        }


    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        if (args.length == 0) {
            // No arguments provided, suggest all subcommands
            return subCommands.keySet();
        }

        if (args.length == 1) {
            // Suggest matching subcommands based on the first argument
            return filterSuggestions(subCommands.keySet(), args[0]);
        }

        if (subCommands.containsKey(args[0])) {
            // Delegate suggestion handling to the relevant subcommand, if any
            BasicCommand subCommand = subCommands.get(args[0]);
            if (subCommand != null) {
                return subCommand.suggest(commandSourceStack, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        // Fallback to empty list for unsupported or invalid input
        return List.of();
    }

    private Collection<String> filterSuggestions(Collection<String> suggestions, String input) {
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }

}