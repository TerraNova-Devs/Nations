package de.terranova.nations.professions;

import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class ObjectiveManager {


    public static void handleEvent(Player player, String action, String object, long amount) {
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(player.getUniqueId());

        if (settleOpt.isEmpty()) {
            return;
        }
        SettleRegion settle = settleOpt.get();

        System.out.println(settle.getId());

        ProfessionProgressManager mgr = ProfessionProgressManager.loadForSettlement(settle.getId());
        List<ProfessionObjective> objectives= ProfessionManager.getObjectivesForProfession(mgr.activeProfessionId);
        objectives.stream()
                .filter(o -> o.getAction().equalsIgnoreCase(action))
                .filter(o -> o.getObject().equalsIgnoreCase(object))
                .forEach(o -> {
                    mgr.setObjectiveProgress(o.getObjectiveId(), mgr.getObjectiveProgress(o.getObjectiveId()) + amount);
                });
    }
}
