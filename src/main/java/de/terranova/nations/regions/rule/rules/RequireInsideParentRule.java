package de.terranova.nations.regions.rule.rules;

import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.regions.rule.RuleContext;
import de.terranova.nations.regions.rule.RuleRequirement;

import java.util.Set;

public class RequireInsideParentRule implements RegionRule {
    private final String requiredParentType;

    public RequireInsideParentRule(String requiredParentType) {
        this.requiredParentType = requiredParentType;
    }

    @Override
    public boolean isAllowed(RuleContext ctx) {
        return ctx.parent != null &&
                ctx.parent.getType().equals(requiredParentType) &&
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
