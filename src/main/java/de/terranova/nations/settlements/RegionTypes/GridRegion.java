package de.terranova.nations.settlements.RegionTypes;

import de.terranova.nations.settlements.SettleManager;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.Location;

public interface GridRegion {

    default int getMaxClaims() {
        int claims = 9;
        if (this.getLevel() <= 1) return claims;
        for (int i = 0; i <= this.getLevel() - 2; i++) claims += SettleManager.claimsPerLevel.get(i);
        return claims;
    }

    int getLevel();

    Vectore2 getLocation();

    int getClaims();

    void setClaims(int claims);

}
