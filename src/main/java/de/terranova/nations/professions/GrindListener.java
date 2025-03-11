package de.terranova.nations.professions;

import de.terranova.nations.database.dao.SettlementObjectiveProgressDAO;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Optional;
import java.util.UUID;

public class GrindListener implements Listener {

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            // 1) Herausfinden, zu welcher Stadt der Spieler gehört
            UUID playerId = event.getPlayer().getUniqueId();
            Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(playerId);
            if (settleOpt.isEmpty()) return;

            SettleRegion settle = settleOpt.get();
            String ruuid = settle.getId().toString();

            // 2) Finde heraus, welche ProfessionObjectivs relevant sind
            //    - "Action" = "FISH", "Object" = "COD" o.Ä.
            // In der Praxis würdest du checken, was genau gefangen wurde:
            // hier: Hardcode => objectiveId=XYZ
            int objectiveId = 123;

            // 3) +1 auf den Fortschritt
            SettlementObjectiveProgressDAO.addProgress(ruuid, objectiveId, 1);
        }
    }
}
