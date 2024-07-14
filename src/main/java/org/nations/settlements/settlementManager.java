package org.nations.settlements;

import java.util.HashMap;
import java.util.UUID;

public class settlementManager {

    HashMap<UUID, settlement[]> settlements = new HashMap<>();

    public settlementManager() {

    }

    public boolean canSettle(UUID uuid) {
        if (!settlements.containsKey(uuid)) {
            return true;
        }

        for (settlement settlement : settlements.get(uuid)) {
            if (settlement != null) {
                if (settlement.canSettle()) {
                    break;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean addSettlement(UUID uuid, settlement settlement) {

        if (settlements.containsKey(uuid)) {
            settlement[] s = settlements.get(uuid);
            for (int i = 0; i < s.length; i++) {
                if (s[i] == null) {
                    s[i] = settlement;
                    return true;
                }
                return false;
            }
        }
        settlement[] s = new settlement[]{settlement, null, null, null, null};
        settlements.put(uuid, s);
        return true;
    }

}
