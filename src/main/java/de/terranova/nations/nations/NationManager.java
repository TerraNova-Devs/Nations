package de.terranova.nations.nations;

import de.terranova.nations.database.dao.NationsDAO;
import de.terranova.nations.pl3xmap.RegionLayer;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import java.util.*;

public class NationManager {
  private final Map<UUID, Nation> nations;

  public NationManager() {
    this.nations = new HashMap<>();
    loadNationsFromDatabase();
  }

  // Getters
  public Map<UUID, Nation> getNations() {
    return nations;
  }

  // Add a nation to the manager and database
  public void addNation(Nation nation) {
    nations.put(nation.getId(), nation);
    NationsDAO.createNation(nation);
    for (UUID settlementId : nation.getSettlements().keySet()) {
      Optional<SettleRegion> settleRegion = RegionManager.retrieveRegion("settle", settlementId);
      RegionLayer.updateRegion(settleRegion.get());
    }
  }

  // Remove a nation from the manager and database
  public void removeNation(UUID nationId) {
    Nation nation = nations.get(nationId);
    for (UUID settlementId : nation.getSettlements().keySet()) {
      Optional<SettleRegion> settleRegion = RegionManager.retrieveRegion("settle", settlementId);
      RegionLayer.updateRegion(settleRegion.get());
    }

    nations.remove(nationId);
    NationsDAO.deleteNation(nationId);
  }

  // Get a nation by UUID
  public Nation getNation(UUID nationId) {
    return nations.get(nationId);
  }

  // Get a nation by name
  public Nation getNationByName(String name) {
    for (Nation nation : nations.values()) {
      if (nation.getName().equalsIgnoreCase(name)) {
        return nation;
      }
    }
    return null;
  }

  public Nation getNationByMember(UUID memberId) {
    for (Nation nation : nations.values()) {
      if (nation.isMember(memberId)) {
        return nation;
      }
    }
    return null;
  }

  // Get the nation a settlement belongs to
  public Nation getNationBySettlement(UUID settlementId) {
    for (Nation nation : nations.values()) {
      if (nation.hasSettlement(settlementId)) {
        return nation;
      }
    }
    return null;
  }

  // Get nation's settlements
  public Map<UUID, SettlementRank> getNationSettlements(UUID nationId) {
    Nation nation = nations.get(nationId);
    if (nation != null) {
      return nation.getSettlements();
    }
    return Collections.emptyMap();
  }

  public Nation getNationByLeader(UUID leaderId) {
    for (Nation nation : nations.values()) {
      if (nation.getLeader().equals(leaderId)) {
        return nation;
      }
    }
    return null;
  }

  public boolean isSettlementInNation(UUID settlementId) {
    for (Nation nation : nations.values()) {
      if (nation.hasSettlement(settlementId)) {
        return true;
      }
    }
    return false;
  }

  // Load nations from the database
  private void loadNationsFromDatabase() {
    List<Nation> loadedNations = NationsDAO.getAllNations();
    for (Nation nation : loadedNations) {
      nations.put(nation.getId(), nation);
    }
  }

  // Save a nation to the database
  public void saveNation(Nation nation) {
    NationsDAO.saveNation(nation);
    nations.put(nation.getId(), nation);
    for (UUID settlementId : nation.getSettlements().keySet()) {
      Optional<SettleRegion> settleRegion = RegionManager.retrieveRegion("settle", settlementId);
      RegionLayer.updateRegion(settleRegion.get());
    }
  }

  public void addSettlementToNation(UUID nationId, UUID settlementId) {
    NationsDAO.addSettlementToNation(
        new SettlementNationRelation(settlementId, nationId, SettlementRank.CITY));
    Nation nation = getNation(nationId);
    nation.addSettlement(settlementId, SettlementRank.CITY);

    // Update region layer
    Optional<SettleRegion> settleRegion = RegionManager.retrieveRegion("settle", settlementId);
    RegionLayer.updateRegion(settleRegion.get());
  }

  public void removeSettlementFromNation(UUID nationId, UUID settlementId) {
    NationsDAO.removeSettlementFromNation(settlementId);
    Nation nation = getNation(nationId);
    nation.removeSettlement(settlementId);

    // Update region layer
    Optional<SettleRegion> settleRegion = RegionManager.retrieveRegion("settle", settlementId);
    RegionLayer.updateRegion(settleRegion.get());
  }
}
