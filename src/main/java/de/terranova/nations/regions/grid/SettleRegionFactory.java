package de.terranova.nations.regions.grid;

import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionFactoryBase;
import de.terranova.nations.utils.BiomeUtil;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class SettleRegionFactory implements RegionFactoryBase {

    @Override
    public String getType() {
        return SettleRegion.REGION_TYPE;
    }

    @Override
    public Region createWithContext(RegionContext ctx) {
        Player p = ctx.player;
        String name = ctx.name;

        if (!validate(ctx, name, null, null)) {
            return null;
        }

        if (isInBlacklistedBiome(p)) {
            p.sendMessage(Chat.errorFade("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"));
            return null;
        }

        if (Region.isNameCached(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return null;
        }

        if (RegionClaimFunctions.checkAreaForSettles(p)) {
            p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
            return null;
        }

        if (isTooCloseToAnotherSettlement(p)) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>2000<#FFD7FE> Blöcke Abstand muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die nächste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(getClosestSettlementDistance(p)))));
            return null;
        }

        // If all conditions are met, create the new SettleRegionType instance
        return new SettleRegion(name, UUID.randomUUID(), RegionClaimFunctions.getSChunkMiddle(p.getLocation()));
    }

    @Override
    public Region createFromArgs(List<String> args) {
        return new SettleRegion(
                args.getFirst(),
                UUID.fromString(args.get(1)),
                new Vectore2(args.get(2))
        );
    }

    private static boolean isTooCloseToAnotherSettlement(Player p) {
        return getClosestSettlementDistance(p) < 2000;
    }

    private static double getClosestSettlementDistance(Player p) {
        double minDistance = Integer.MAX_VALUE;
        for (Vectore2 location : GridRegion.locationCache) {
            double distance = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    public static boolean isInBlacklistedBiome(Player player) {
        List<String> biomeTranslationKeys = List.of(
                //Minecraft biomes
                "minecraft:deep_ocean", "minecraft:ocean", "minecraft:warm_ocean",
                "minecraft:frozen_ocean", "minecraft:lukewarm_ocean", "minecraft:cold_ocean",
                "minecraft:deep_frozen_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:deep_cold_ocean",
                "minecraft:river", "minecraft:beach", "minecraft:snowy_beach",
                //Terralith biomes
                "terralith:gravel_beach", "terralith:snowy_beach", "terralith:oceanic_plateau",
                "terralith:tropical_bay", "terralith:rocky_coast", "terralith:white_cliffs",
                "terralith:sandstone_valley"
        );

        return BiomeUtil.isBiomeInList(player.getLocation(), biomeTranslationKeys);
    }

}