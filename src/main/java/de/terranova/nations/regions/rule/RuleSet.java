package de.terranova.nations.regions.rule;

import java.util.ArrayList;
import java.util.List;

public class RuleSet {
    private final List<RegionRule> rules;

    public RuleSet(List<RegionRule> rules) {
        this.rules = rules;
    }

    public static RuleSet defaultRules() {
        return new RuleSet(new ArrayList<>());
    }

    public RuleSet addRule(RegionRule rule) {
        List<RegionRule> newRules = new ArrayList<>(rules);
        newRules.add(rule);
        return new RuleSet(newRules);
    }

    public List<RegionRule> getRules() {
        return rules;
    }
}
