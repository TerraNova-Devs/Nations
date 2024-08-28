package de.terranova.nations.api;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.Settlement;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class SettlementAPI {

    public static Settlement getSettlement(UUID SettlementUUID) {
        return NationsPlugin.settlementManager.getSettlement(SettlementUUID);
    }

    public static Optional<Settlement> getSettlement(Player player) {
        return NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(player);
    }

    public static Optional<Settlement> getSettlement(Location location) {
        return NationsPlugin.settlementManager.checkIfLocationIsWithinClaim(location);
    }
}
