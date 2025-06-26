package de.terranova.nations.regions.rule.rules;

import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.rule.RegionRule;
import de.terranova.nations.regions.rule.RuleContext;
import de.terranova.nations.regions.rule.RuleRequirement;

import java.util.Set;

import java.util.Set;

public class NoSelfOverlapRule implements RegionRule {
    @Override
    public boolean isAllowed(RuleContext ctx) {
        for (Region r : ctx.nearbyRegions) {
            if (r.getType().equals(ctx.type) && ctx.fakeRegionBeingPlaced.overlaps(r)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getErrorMessage() {
        return "Diese Region würde eine andere Region gleichen Typs überschneiden.";
    }

    @Override
    public Set<RuleRequirement> getRequirements() {
        return Set.of(RuleRequirement.NEARBY_REGIONS, RuleRequirement.FAKE_REGION);
    }
}