package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class SettleCommand implements BasicCommand, TabCompleter {

    private final Map<String, BasicCommand> subCommands = new HashMap<>();

    NationsPlugin plugin;

    public SettleCommand(NationsPlugin plugin) {
        this.plugin = plugin;
        subCommands.putAll(Map.of("test", new SettleTestSubCommand("nations.test"), "test2", new SettleTestSubCommand("nations.test2")));
        subCommands.putAll(Map.of( "show", new SettleCreateSubCommand("nations.show"), "unshow", new SettleCreateSubCommand("nations.show") ,
                "create", new SettleCreateSubCommand("nations.create"), "rename", new SettleCreateSubCommand("nations.rename"),
                "tphere", new SettleCreateSubCommand("nations.tphere")));
        subCommands.put("member", new SettleMemberSubCommand("nations.member"));
        subCommands.putAll(Map.of("claim", new SettleClaimSubCommand("nations.claim"),"forceclaim", new SettleClaimSubCommand("nations.admin")));
        subCommands.putAll(Map.of("re", new SettleRemoveSubCommand("nations.remove"),"forceremove", new SettleRemoveSubCommand("nations.admin")));
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> literals = new ArrayList<>(subCommands.keySet());
        Collections.sort(literals);
        return literals;
    }
}
