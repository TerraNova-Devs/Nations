package de.terranova.nations.commands;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationManager;
import de.terranova.nations.settlements.Settle;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.Optional;

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

        Optional<Settle> settle = NationsPlugin.settleManager.getPlayersSettlement(player.getUniqueId());

        if(settle.isEmpty())
            return;

        // Check if settle is already in a nation
        for (Nation nation : nationManager.getNations().values()) {
            if (nation.getSettlements().contains(settle.get().id)) {
                player.sendMessage("You are already a member of a nation.");
                return;
            }
        }

        // Create the nation
        Nation nation = new Nation(nationName, player.getUniqueId());
        nationManager.addNation(nation, settle.get().id);

        player.sendMessage("Nation " + nationName + " created successfully!");
    }
}
