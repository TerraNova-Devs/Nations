package de.terranova.nations.regions.rule.rules;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.rule.RegionRule;
import org.bukkit.entity.Player;

public class WithinParentRegionRule<T extends Region & HasParent<P>, P extends Region>
    implements RegionRule {

  private final Class<T> childClass;
  private final Class<P> parentClass;

  public WithinParentRegionRule(Class<T> childClass, Class<P> parentClass) {
    this.childClass = childClass;
    this.parentClass = parentClass;
  }

  @Override
  public boolean isAllowed(
      Player p,
      String type,
      String regionName,
      ProtectedRegion regionBeingPlaced,
      Region explicitParent) {
    if (!parentClass.isInstance(explicitParent)) return false;
    return explicitParent.isCompletelyWithin2D(regionBeingPlaced);
  }

  @Override
  public String getErrorMessage() {
    return "Diese Region muss vollständig innerhalb eines gültigen Elternobjekts liegen.";
  }
}
