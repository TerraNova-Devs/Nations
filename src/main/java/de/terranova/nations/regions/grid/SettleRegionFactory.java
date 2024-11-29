package de.terranova.nations.regions.grid;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.RegionFactory;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.List;

public class SettleRegionFactory implements RegionFactory {

    @Override
    public RegionType create(String name, Player p) {
        // Perform all necessary validations before creation
        if (!isValidName(name, p)) {
            p.sendMessage(Chat.errorFade("Invalid name for settlement."));
            return null;  // Return null to indicate creation failure.
        }

        if (isInBlacklistedBiome(p)) {
            p.sendMessage(Chat.errorFade("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"));
            return null;
        }

        if (!NationsPlugin.settleManager.isNameAvaible(name)) {
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
        SettleRegionType settlement = new SettleRegionType(name, p);
        settlement.postInit(p);
        return settlement;
    }

    private static boolean isTooCloseToAnotherSettlement(Player p) {
        return getClosestSettlementDistance(p) < 2000;
    }

    private static double getClosestSettlementDistance(Player p) {
        double minDistance = Integer.MAX_VALUE;
        for (Vectore2 location : NationsPlugin.settleManager.locationCache) {
            double distance = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    private boolean isValidName(String name, Player p) {
        if (name.matches("^[a-zA-Z0-9_]{1,20}$")) {
            return true;
        }
        p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
        return false;
    }

    private static boolean isInBlacklistedBiome(Player p) {
        List<org.bukkit.block.Biome> biomeblacklist = List.of(
                org.bukkit.block.Biome.DEEP_OCEAN, org.bukkit.block.Biome.OCEAN, org.bukkit.block.Biome.WARM_OCEAN
                , org.bukkit.block.Biome.FROZEN_OCEAN, org.bukkit.block.Biome.LUKEWARM_OCEAN, org.bukkit.block.Biome.COLD_OCEAN
                , org.bukkit.block.Biome.DEEP_FROZEN_OCEAN, org.bukkit.block.Biome.DEEP_LUKEWARM_OCEAN, org.bukkit.block.Biome.DEEP_COLD_OCEAN
                , org.bukkit.block.Biome.RIVER, org.bukkit.block.Biome.BEACH, Biome.SNOWY_BEACH);
        return biomeblacklist.contains(p.getLocation().getBlock().getBiome());
    }
}