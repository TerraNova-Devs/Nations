package de.terranova.nations.regions.grid;

import de.mcterranova.terranovaLib.utils.BiomeUtil;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionFactoryBase;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class SettleRegionFactory implements RegionFactoryBase {

  @Override
  public String getType() {
    return SettleRegion.REGION_TYPE;
  }

  @Override
  public Region createWithContext(RegionContext ctx) {
    if (!dryRunCreate(ctx)) {
      return null;
    }

    return new SettleRegion(
            ctx.name,
            UUID.randomUUID(),
            RegionClaimFunctions.getSChunkMiddle(ctx.location)
    );
  }

  @Override
  public boolean dryRunCreate(RegionContext ctx) {
    Player p = ctx.player;
    String name = ctx.name;

    if (!validate(ctx, name, null, null)) {
      return false;
    }

    if (isInBlacklistedBiome(ctx.location)) {
      p.sendMessage(Chat.errorFade(
              "Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"
      ));
      return false;
    }

    if (Region.isNameCached(name)) {
      p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
      return false;
    }

    if (RegionClaimFunctions.checkAreaForSettles(ctx.location)) {
      p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
      return false;
    }

    if (isTooCloseToAnotherSettlement(ctx.location)) {
      p.sendMessage(Chat.errorFade(
              "Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>2000<#FFD7FE> Blöcke Abstand muss eingehalten werden."
      ));
      p.sendMessage(Chat.errorFade(String.format(
              "Die nächste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.",
              (int) Math.floor(getClosestSettlementDistance(ctx.location))
      )));
      return false;
    }

    return true;
  }

  @Override
  public Region createFromArgs(List<String> args) {
    return new SettleRegion(
        args.getFirst(), UUID.fromString(args.get(1)), new Vectore2(args.get(2)));
  }

  private static boolean isTooCloseToAnotherSettlement(org.bukkit.Location location) {
    return getClosestSettlementDistance(location) < 2000;
  }

  private static double getClosestSettlementDistance(org.bukkit.Location location) {
    double minDistance = Double.MAX_VALUE;

    for (Vectore2 cached : GridRegion.locationCache) {
      double distance = claimCalc.abstand(cached, new Vectore2(location));
      if (distance < minDistance) {
        minDistance = distance;
      }
    }

    return minDistance;
  }

  public static boolean isInBlacklistedBiome(org.bukkit.Location location) {
    List<String> biomeTranslationKeys =
            List.of(
                    "minecraft:deep_ocean",
                    "minecraft:ocean",
                    "minecraft:warm_ocean",
                    "minecraft:frozen_ocean",
                    "minecraft:lukewarm_ocean",
                    "minecraft:cold_ocean",
                    "minecraft:deep_frozen_ocean",
                    "minecraft:deep_lukewarm_ocean",
                    "minecraft:deep_cold_ocean",
                    "minecraft:river",
                    "minecraft:beach",
                    "minecraft:snowy_beach",
                    "minecraft:frozen_river",
                    "terralith:gravel_beach",
                    "terralith:snowy_beach",
                    "terralith:oceanic_plateau",
                    "terralith:tropical_bay",
                    "terralith:rocky_coast",
                    "terralith:white_cliffs",
                    "terralith:sandstone_valley");

    return BiomeUtil.isBiomeInList(location, biomeTranslationKeys);
  }
}
