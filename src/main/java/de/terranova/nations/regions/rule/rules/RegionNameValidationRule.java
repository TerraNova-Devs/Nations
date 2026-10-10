package de.terranova.nations.regions.rule.rules;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.rule.RegionRule;
import de.mcterranova.terranovaLib.utils.Chat;
import org.bukkit.entity.Player;

public class RegionNameValidationRule implements RegionRule {

  /** Names of towns and nations: 3 to 20 letters, digits and single _ inside. */
  public static final String NAME = "^(?!.*__)(?!_)(?!.*_$)(?!.*(.)\\1{3,})[a-zA-Z0-9_]{3,20}$";

  String regex;

  public RegionNameValidationRule(String regex) {
    this.regex = regex;
  }

  @Override
  public boolean isAllowed(
      Player p,
      String type,
      String regionName,
      ProtectedRegion regionBeingPlaced,
      Region explicitParent) {

    if (regionName.matches(regex)) {
      return true;
    }
    p.sendMessage(
        Chat.errorFade(
            "Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
    return false;
  }

  @Override
  public String getErrorMessage() {
    return "Der Name der Region ist nicht gültig";
  }
}
