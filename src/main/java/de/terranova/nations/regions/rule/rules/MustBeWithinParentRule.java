package de.terranova.nations.regions.rule.rules;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.worldguard.BoundaryClaimFunctions;
import org.bukkit.entity.Player;

public class MustBeWithinParentRule<T extends Region & HasParent<P>, P extends Region> implements RegionRule {

    private final Class<T> childClass;
    private final Class<P> parentClass;

    public MustBeWithinParentRule(Class<T> childClass, Class<P> parentClass) {
        this.childClass = childClass;
        this.parentClass = parentClass;
    }

    @Override
    public boolean isAllowed(Player p,Class<? extends Region> regionClass,String regionName, ProtectedRegion regionBeingPlaced, Region explicitParent) {
        if (!regionClass.getTypeName().equals(childClass.getTypeName())) return false;
        if (!parentClass.isInstance(explicitParent)) return false;

        ProtectedRegion childRegion = regionBeingPlaced;
        ProtectedRegion parentRegion = explicitParent.getWorldguardRegion();

        return BoundaryClaimFunctions.doRegionsOverlap2D(childRegion, parentRegion);
    }

    @Override
    public String getErrorMessage() {
        return "Diese Region muss vollständig innerhalb eines gültigen Elternobjekts liegen.";
    }

}
