package de.terranova.nations.regions.rule;

import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionRegistry;
import de.terranova.nations.utils.Chat;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RuleValidator {

    public static boolean validate(RegionContext ctx, String regionType, Region regionBeingPlaced, Region explicitParent) {
        RuleSet rules = RegionRegistry.getRuleSet(regionType);

        Set<RuleRequirement> requirements = rules.getRules().stream()
                .flatMap(rule -> rule.getRequirements().stream())
                .collect(Collectors.toSet());

        Region parent = requirements.contains(RuleRequirement.PARENT_REGION)
                ? explicitParent
                : null;

        if (requirements.contains(RuleRequirement.PARENT_REGION) && parent == null) {
            ctx.player.sendMessage(Chat.errorFade("Fehlende Elterregion für Regeln vom Typ '" + regionType + "'."));
            return false;
        }

        List<Region> nearby = requirements.contains(RuleRequirement.NEARBY_REGIONS)
                ? RegionClaimFunctions.getRegionsNear(ctx.location, 50)
                : Collections.emptyList();

        RuleContext ruleCtx = new RuleContext(
                regionType, ctx.player, ctx.location, parent, nearby, regionBeingPlaced
        );

        for (RegionRule rule : rules.getRules()) {
            if (!rule.isAllowed(ruleCtx)) {
                ctx.player.sendMessage(Chat.errorFade(rule.getErrorMessage()));
                return false;
            }
        }
        return true;
    }

    public static boolean validate(RegionContext ctx, String regionType, Region regionBeingPlaced) {
        Region parentGuess = RegionClaimFunctions.getRegionAt(ctx.location);
        return validate(ctx, regionType, regionBeingPlaced, parentGuess);
    }
}