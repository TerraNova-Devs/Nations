package de.terranova.nations.regions.rule;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionRegistry;
import de.terranova.nations.utils.Chat;
import org.bukkit.entity.Player;

public class RuleValidator {

  public static boolean validate(
      Player p,
      String type,
      String regionName,
      ProtectedRegion regionBeingPlaced,
      Region explicitParent) {
    RuleSet rules = RegionRegistry.getRuleSet(type);

    for (RegionRule rule : rules.getRules()) {
      if (!rule.isAllowed(p, type, regionName, regionBeingPlaced, explicitParent)) {
        p.sendMessage(Chat.errorFade(rule.getErrorMessage()));
        return false;
      }
    }
    return true;
  }
}
