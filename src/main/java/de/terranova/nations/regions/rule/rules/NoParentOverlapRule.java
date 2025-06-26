package de.terranova.nations.regions.rule.rules;

import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.regions.rule.RuleContext;
import de.terranova.nations.regions.rule.RuleRequirement;

import java.util.Set;

public class NoParentOverlapRule implements RegionRule {
    private final String regionType;

    public NoParentOverlapRule(String regionType) {
        this.regionType = regionType;
    }

    @Override
    public boolean isAllowed(RuleContext ctx) {
        if (!ctx.type.equals(regionType)) return true;
        return ctx.parent != null && !ctx.fakeRegionBeingPlaced.overlaps(ctx.parent);
    }

    @Override
    public String getErrorMessage() {
        return "Die Region darf sich nicht mit den Rändern des Elternobjekts überschneiden.";
    }

    @Override
    public Set<RuleRequirement> getRequirements() {
        return Set.of(RuleRequirement.PARENT_REGION, RuleRequirement.FAKE_REGION);
    }
}