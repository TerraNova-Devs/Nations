package de.terranova.nations.regions.rule.rules;

import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.regions.rule.RuleContext;
import de.terranova.nations.regions.rule.RuleRequirement;

import java.util.Set;

public class MustBeInsideParentRule<T extends Region & HasParent<P>, P extends Region> implements RegionRule {
    private final Class<T> childClass;
    private final Class<P> parentClass;

    public MustBeInsideParentRule(Class<T> childClass, Class<P> parentClass) {
        this.childClass = childClass;
        this.parentClass = parentClass;
    }

    @Override
    public boolean isAllowed(RuleContext ctx) {
        if (!childClass.isInstance(ctx.fakeRegionBeingPlaced)) return true;

        T child = childClass.cast(ctx.fakeRegionBeingPlaced);
        P parent = child.getParent();

        return parentClass.isInstance(parent) && child.isInside(parent);
    }

    @Override
    public String getErrorMessage() {
        return "Diese Region muss vollständig innerhalb eines gültigen Elternobjekts liegen.";
    }

    @Override
    public Set<RuleRequirement> getRequirements() {
        return Set.of(RuleRequirement.FAKE_REGION);
    }
}
