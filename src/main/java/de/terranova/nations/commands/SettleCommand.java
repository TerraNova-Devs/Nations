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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
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
