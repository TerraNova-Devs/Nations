package de.terranova.nations.regions.rule;

import java.util.Set;

public interface RegionRule {
    boolean isAllowed(RuleContext ctx);
    String getErrorMessage();
    Set<RuleRequirement> getRequirements();
}
