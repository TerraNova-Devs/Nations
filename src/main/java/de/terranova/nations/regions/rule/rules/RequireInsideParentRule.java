package de.terranova.nations.regions.rule.rules;

import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.regions.rule.RuleContext;
import de.terranova.nations.regions.rule.RuleRequirement;

import java.util.Set;

public class RequireInsideParentRule implements RegionRule {
    private final String regionType;
    private final String parentType;

    public RequireInsideParentRule(String regionType, String parentType) {
        this.regionType = regionType;
        this.parentType = parentType;
    }

    @Override
    public boolean isAllowed(RuleContext ctx) {
        if (!ctx.type.equals(regionType)) return true;
        return ctx.parent != null &&
                ctx.parent.getType().equals(parentType) &&
                ctx.fakeRegionBeingPlaced.isInside(ctx.parent);
    }

    @Override
    public String getErrorMessage() {
        return "Diese Region muss vollständig innerhalb eines gültigen Elternelements liegen.";
    }

    @Override
    public Set<RuleRequirement> getRequirements() {
        return Set.of(RuleRequirement.PARENT_REGION, RuleRequirement.FAKE_REGION);
    }
}
