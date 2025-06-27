package de.terranova.nations.regions.rule.rules;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionRegistry;
import de.terranova.nations.regions.modules.HasChildren;
import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.worldguard.BoundaryClaimFunctions;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NoSelfOverlapRule implements RegionRule {


    @Override
    public boolean isAllowed(Player p, Class<? extends Region> regionClass,String regionName, ProtectedRegion regionBeingPlaced, Region explicitParent) {
        List<Region> relevantRegions;

        // Ist das Grid belegt?
        if(explicitParent == null && BoundaryRegion.class.isAssignableFrom(regionClass)) {
            return RegionClaimFunctions.checkAreaForSettles(p);
        }

        // Hast die Region Childrensets?
        boolean hasInsideParentRule = RegionRegistry.getRuleSet(regionClass.getTypeName()).getRules().stream()
                .anyMatch(rule -> rule instanceof MustBeWithinParentRule<?, ?>);

        if (hasInsideParentRule && explicitParent instanceof HasChildren) {
            // Case 1: Wenn die Region Children hat nur gegen diese Testen
            relevantRegions = ((HasChildren) explicitParent).getChildrenByType(regionClass.getTypeName());
        } else {
            // Case 2: Gegen alle Regionen mit dem Typen Testen
            relevantRegions = new ArrayList<>(RegionManager.retrieveAllCachedRegions(regionClass.getTypeName()).values());
        }

        for (Region existing : relevantRegions) {
            if (BoundaryClaimFunctions.doRegionsOverlap2D(regionBeingPlaced, existing.getWorldguardRegion())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getErrorMessage() {
        return "Diese Region darf sich nicht mit einer anderen Region desselben Typs überschneiden.";
    }

}