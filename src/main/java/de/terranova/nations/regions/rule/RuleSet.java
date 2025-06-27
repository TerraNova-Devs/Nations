package de.terranova.nations.regions.rule;

import de.terranova.nations.regions.rule.rules.NoSelfOverlapRule;

import java.util.ArrayList;
import java.util.List;

public class RuleSet {

    private static final List<RegionRule> DEFAULT_RULES = List.of(
            new NoSelfOverlapRule(false)
    );

    private final List<RegionRule> customRules;
    private final List<Class<? extends RegionRule>> excludedDefaultRules;

    public RuleSet(List<RegionRule> customRules, List<Class<? extends RegionRule>> excludedDefaultRules) {
        this.customRules = customRules;
        this.excludedDefaultRules = excludedDefaultRules;
    }

    public static RuleSet defaultRules() {
        return new RuleSet(new ArrayList<>(), new ArrayList<>());
    }

    public RuleSet addRule(RegionRule rule) {
        List<RegionRule> newRules = new ArrayList<>(customRules);
        newRules.add(rule);
        return new RuleSet(newRules, excludedDefaultRules);
    }

    public RuleSet excludeDefaultRule(Class<? extends RegionRule> ruleClass) {
        List<Class<? extends RegionRule>> newExcluded = new ArrayList<>(excludedDefaultRules);
        newExcluded.add(ruleClass);
        return new RuleSet(customRules, newExcluded);
    }

    public List<RegionRule> getRules() {
        List<RegionRule> combined = new ArrayList<>();

        for (RegionRule rule : DEFAULT_RULES) {
            if (!excludedDefaultRules.contains(rule.getClass())) {
                combined.add(rule);
            }
        }

        combined.addAll(customRules);
        return combined;
    }
}
