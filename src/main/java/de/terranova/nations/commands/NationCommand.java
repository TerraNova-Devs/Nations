package de.terranova.nations.commands;

import de.terranova.nations.nations.NationManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class NationCommand implements BasicCommand {
    private final Map<String, BasicCommand> subCommands = new HashMap<>();
    private final NationManager nationManager;

    public NationCommand(NationManager nationManager) {
        this.nationManager = nationManager;
        subCommands.put("create", new NationCreateSubCommand(nationManager));
        subCommands.put("invite", new NationInviteSubCommand(nationManager));
        subCommands.put("accept", new NationAcceptSubCommand(nationManager));
        subCommands.put("setrelation", new NationSetRelationSubCommand(nationManager));
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage("You must be a player to use this command.");
            return;
        }

        if (args.length == 0) {
            // Display help or nation info
            return;
        }

        BasicCommand command = subCommands.get(args[0].toLowerCase());
        if (command != null) {
            command.execute(stack, args);
        } else {
            player.sendMessage("Unknown nation command.");
        }
    }
}
