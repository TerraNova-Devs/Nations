package de.terranova.nations.commands;

import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public class NationCreateSubCommand implements BasicCommand {
    private final NationManager nationManager;

    public NationCreateSubCommand(NationManager nationManager) {
        this.nationManager = nationManager;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        Player player = (Player) stack.getSender();

        if (args.length < 2) {
            player.sendMessage("Usage: /nation create <name>");
            return;
        }

        String nationName = args[1];

        // Check if nation name is available
        if (nationManager.getNationByName(nationName) != null) {
            player.sendMessage("A nation with that name already exists.");
            return;
        }

        // Create the nation
        Nation nation = new Nation(nationName, player.getUniqueId());
        nationManager.addNation(nation);
        nationManager.saveNation(nation);

        player.sendMessage("Nation " + nationName + " created successfully!");
    }
}
