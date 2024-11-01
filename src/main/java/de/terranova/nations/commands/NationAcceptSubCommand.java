package de.terranova.nations.commands;

import de.terranova.nations.database.NationDBStuff;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationManager;
import de.terranova.nations.nations.SettlementNationRelation;
import de.terranova.nations.nations.SettlementRank;
import de.terranova.nations.settlements.Settle;
import de.terranova.nations.settlements.SettleManager;
import de.terranova.nations.settlements.AccessLevelEnum;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NationAcceptSubCommand implements BasicCommand {
    private final NationManager nationManager;
    private final SettleManager settleManager;

    public NationAcceptSubCommand(NationManager nationManager, SettleManager settleManager) {
        this.nationManager = nationManager;
        this.settleManager = settleManager;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        Player player = (Player) stack.getSender();

        // Get the settlement the player is a high-ranking member of
        Settle playerSettlement = null;
        for (Settle settle : settleManager.settlements.values()) {
            AccessLevelEnum accessLevel = settle.membersAccess.get(player.getUniqueId());
            if (accessLevel == AccessLevelEnum.MAJOR || accessLevel == AccessLevelEnum.VICE || accessLevel == AccessLevelEnum.COUNCIL) {
                playerSettlement = settle;
                break;
            }
        }

        if (playerSettlement == null) {
            player.sendMessage("You are not a high-ranking member of any settlement.");
            return;
        }

        UUID settlementId = UUID.fromString(playerSettlement.name);

        // Check if there is a pending invitation
        if (!nationManager.hasInvitation(settlementId)) {
            player.sendMessage("Your settlement has no pending nation invitations.");
            return;
        }

        UUID nationId = nationManager.getInvitation(settlementId);
        Nation nation = nationManager.getNation(nationId);

        if (nation == null) {
            player.sendMessage("The nation you were invited to no longer exists.");
            nationManager.removeInvitation(settlementId);
            return;
        }

        // Check if the settlement is already part of a nation
        if (nationManager.isSettlementInNation(settlementId)) {
            player.sendMessage("Your settlement is already part of a nation.");
            nationManager.removeInvitation(settlementId);
            return;
        }

        // Add the settlement to the nation
        SettlementNationRelation relation = new SettlementNationRelation(settlementId, nationId, SettlementRank.CITY);
        nation.addSettlement(settlementId);
        nationManager.saveNation(nation);
        // Save the settlement-nation relation to the database
        NationDBStuff.addSettlementToNation(relation);

        nationManager.removeInvitation(settlementId);

        player.sendMessage("Your settlement has joined the nation '" + nation.getName() + "'.");
    }
}
