package de.terranova.nations.api;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.PropertyTypeClasses.SettlementPropertyType;
import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

public class SettleAPI {

    public static Optional<SettlementPropertyType> getSettle(UUID SettlementUUID) {
        return NationsPlugin.settleManager.getSettle(SettlementUUID);
    }

    public static Optional<SettlementPropertyType> getSettle(Location location) {
        return NationsPlugin.settleManager.getSettle(location);
    }
}
