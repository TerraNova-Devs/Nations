package de.terranova.nations.settlements;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.worldguard.settlementFlag;
import org.bukkit.entity.Player;

import java.util.*;

public class settlementManager {

    public HashMap<UUID, settlement> settlements;
    HashMap<UUID, playerdata> playerdata;

    public settlementManager() {
        settlements = new HashMap<>();
    }

    public boolean canSettle(UUID uuid) {



        if (!playerdata.containsKey(uuid)) {
            return false;
        }

        for (playerdata playerdata : playerdata.values()) {
            if (playerdata != null) {
                if (playerdata.canSettle()) {
                    break;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public void setSettlements(HashMap<UUID, settlement> settlements) {
        this.settlements = settlements;
    }

    public void addSettlement(UUID uuid, settlement settlement) {
        settlements.put(uuid, settlement);
    }

    public boolean isNameAvaible(String name) {

        for (settlement settlements : settlements.values()) {

                if (Objects.equals(settlements.name.toLowerCase(), name.toLowerCase())) {
                    return false;
                }
        }
        return true;
    }

    public AccessLevelEnum getAcessLevel(Player p, UUID settlementUUID) {
        return settlements.get(settlementUUID).members.get(p.getUniqueId());
    }


    public Optional<settlement> checkIfPlayerIsWithinClaim(Player player) {
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(lp.getWorld());
        assert regions != null;
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(lp.getLocation());
        for (ProtectedRegion each : set) {
            for (settlement settle : settlements.values()) {
                UUID settlementUUID = UUID.fromString(Objects.requireNonNull(each.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG)));
                if(settle.id.equals(settlementUUID)) {
                    return Optional.of(settlements.get(settlementUUID));
                }
            }
        }
        return Optional.empty();
    }

    public boolean canSettle(Player p) {
        for (settlement settlement : settlements.values()) {
            for (AccessLevelEnum acess : settlement.members.values()) {
                if(acess.equals(AccessLevelEnum.MAJOR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void changeSkin(Player p, settlement settlement) {
        //new TownAdmSkinGUI(p,settlement.level);
    }

}
