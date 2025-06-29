package de.terranova.nations.regions.rule.rules;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessControlled;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.rule.RegionRule;
import org.bukkit.entity.Player;

import java.util.Optional;

public class AccessLevelRule implements RegionRule {

    AccessLevel accessLevel;

    public AccessLevelRule(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean isAllowed(Player p, String type, String regionName, ProtectedRegion regionBeingPlaced, Region explicitParent) {
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (!settleOpt.isPresent()) {
            return accessLevel == null;
        } else if (accessLevel == null) {
            return false;
        }
        AccessControlled accessControlled = settleOpt.get();
        return Access.hasAccess(accessControlled.getAccess().getAccessLevel(p.getUniqueId()), accessLevel);
    }

    @Override
    public String getErrorMessage() {
        return "Du hast nicht die Berechtigung, um Grundstücke zu erstellen.";
    }
}
