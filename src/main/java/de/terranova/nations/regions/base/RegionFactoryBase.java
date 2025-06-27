package de.terranova.nations.regions.base;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.rule.RuleValidator;
import de.terranova.nations.utils.Chat;

import java.util.List;

public interface RegionFactoryBase {

    String getType();

    // For new Creations facilitates more information
    Region createWithContext(RegionContext ctx);

    // To load least necessary from saved data
    Region createFromArgs(List<String> args);

    default boolean validate(RegionContext ctx,String name, ProtectedRegion fakeRegion, Region parent) {
        return RuleValidator.validate(ctx.player, getType(), name, fakeRegion ,parent);
    }
}
