package de.terranova.nations.settlements;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

import java.util.*;

public class settlementManager {

  HashMap<UUID, ArrayList<settlement>> settlements;

  public settlementManager() {
    settlements = new HashMap<>();
  }

  public boolean canSettle(UUID uuid) {
    if (!settlements.containsKey(uuid)) {
      return true;
    }

    for (settlement settlement : settlements.get(uuid)) {
      if (settlement != null) {
        if (settlement.canSettle()) {
          break;
        } else {
          return false;
        }
      }
    }
    return true;
  }

  public void addSettlement(UUID uuid, settlement settlement) {

    if (!settlements.containsKey(uuid)) {
      ArrayList<settlement> s = new ArrayList<>();
      settlements.put(uuid, s);
    }

    ArrayList<settlement> s = settlements.get(uuid);
    s.add(settlement);
    settlements.replace(uuid, s);
  }

  public int howManySettlements(UUID uuid) {
    if (!settlements.containsKey(uuid)) {
      return 0;
    }
    return settlements.get(uuid).size();
  }

  public boolean isNameAvaible(String name) {

    for (ArrayList<settlement> settlements : settlements.values()) {
      for (settlement settlement : settlements) {
        if (Objects.equals(settlement.name, name)) {
          return false;
        }
      }
    }
    return true;
  }

  public Optional<settlement> checkIfPlayerIsInsideHisClaim(Player player) {
    if (!settlements.containsKey(player.getUniqueId())) {
      return Optional.empty();
    }
    LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regions = container.get(lp.getWorld());
    assert regions != null;
    RegionQuery query = container.createQuery();
    ApplicableRegionSet set = query.getApplicableRegions(lp.getLocation());
    for (ProtectedRegion each : set) {
      for (settlement s : settlements.get(player.getUniqueId())) {
        if (Objects.equals(each.getId(), s.name)) {
          return Optional.of(s);
        }
      }
    }
    return Optional.empty();
  }

  public void changeSkin(Player p, settlement settlement) {
    //new TownAdmSkinGUI(p,settlement.level);
  }

}
