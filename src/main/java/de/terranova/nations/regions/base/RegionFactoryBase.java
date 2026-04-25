package de.terranova.nations.regions.base;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.rule.RuleValidator;
import java.util.List;

public interface RegionFactoryBase {

  String getType();

  Region createWithContext(RegionContext ctx);

  Region createFromArgs(List<String> args);

  default boolean dryRunCreate(RegionContext ctx) {
    return validate(ctx, ctx.name, null, null);
  }

  default boolean validate(
          RegionContext ctx, String name, ProtectedRegion fakeRegion, Region parent) {
    return RuleValidator.validate(ctx.player, getType(), name, fakeRegion, parent);
  }
}