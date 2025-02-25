package de.terranova.nations.nations;

import de.terranova.nations.database.dao.NationsAccessDAO;

import java.util.*;

public class NationManager {
    private Map<UUID, Nation> nations;
    private Map<UUID, UUID> pendingInvitations; // Key: Settlement UUID, Value: Nation UUID

    public NationManager() {
        this.nations = new HashMap<>();
        this.pendingInvitations = new HashMap<>();
        loadNationsFromDatabase();
    }

    // Getters
    public Map<UUID, Nation> getNations() {
        return nations;
    }

    // Add a nation to the manager and database
    public void addNation(Nation nation, UUID suuid) {
        nation.addSettlement(suuid);
        nations.put(nation.getId(), nation);
        saveNation(nation);
        SettlementNationRelation relation = new SettlementNationRelation(suuid, nation.getId(), SettlementRank.CAPITAL);
        NationsAccessDAO.addSettlementToNation(relation);
    }

    // Remove a nation from the manager and database
    public void removeNation(UUID nationId) {
        nations.remove(nationId);
        NationsAccessDAO.deleteNation(nationId);
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
    public Set<UUID> getNationSettlements(UUID nationId) {
        Nation nation = nations.get(nationId);
        if (nation != null) {
            return nation.getSettlements();
        }
        return Collections.emptySet();
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
        List<Nation> loadedNations = NationsAccessDAO.getAllNations();
        for (Nation nation : loadedNations) {
            nations.put(nation.getId(), nation);
        }
    }

    // Save a nation to the database
    public void saveNation(Nation nation) {
        NationsAccessDAO.saveNation(nation);
    }

    // Invitation methods
    public void inviteSettlement(UUID settlementId, UUID nationId) {
        pendingInvitations.put(settlementId, nationId);
    }

    public boolean hasInvitation(UUID settlementId) {
        return pendingInvitations.containsKey(settlementId);
    }

    public UUID getInvitation(UUID settlementId) {
        return pendingInvitations.get(settlementId);
    }

    public void removeInvitation(UUID settlementId) {
        pendingInvitations.remove(settlementId);
    }
}
