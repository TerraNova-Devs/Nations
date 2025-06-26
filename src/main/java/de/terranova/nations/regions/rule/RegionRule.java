package de.terranova.nations.regions.rule;

public interface RegionRule {
    boolean isAllowed(RuleContext ctx);
    String getErrorMessage();
    Set<RuleRequirement> getRequirements();
}
