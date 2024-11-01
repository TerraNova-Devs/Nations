package de.terranova.nations.commands;

import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationManager;
import de.terranova.nations.settlements.Settle;
import de.terranova.nations.settlements.SettleManager;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.mcterranova.terranovaLib.utils.Chat;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NationInviteSubCommand implements BasicCommand {
    private final NationManager nationManager;
    private final SettleManager settleManager;

    public NationInviteSubCommand(NationManager nationManager, SettleManager settleManager) {
        this.nationManager = nationManager;
        this.settleManager = settleManager;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        Player player = (Player) stack.getSender();

        if (args.length < 2) {
            player.sendMessage("Usage: /nation invite <settlementName>");
            return;
        }

        String settlementName = args[1];
        Settle settlement = settleManager.getSettlementByName(settlementName);

        if (settlement == null) {
            player.sendMessage("Settlement not found.");
            return;
        }

        // Get player's nation
        Nation playerNation = nationManager.getNationByLeader(player.getUniqueId());

        if (playerNation == null) {
            player.sendMessage("You are not the leader of a nation.");
            return;
        }

        // Check if settlement is already in a nation
        if (nationManager.isSettlementInNation(UUID.fromString(settlement.name))) {
            player.sendMessage("This settlement is already part of a nation.");
            return;
        }

        // Send an invitation
        nationManager.inviteSettlement(UUID.fromString(settlement.name), playerNation.getId());

        player.sendMessage("You have invited the settlement '" + settlementName + "' to join your nation.");

        // Notify higher-ranked members of the settlement
        settlement.notifyHighRankMembers("Your settlement has been invited to join the nation '" + playerNation.getName() + "'. Use '/nation accept' to accept the invitation.");
    }
}
