package de.terranova.nations.nations;

import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;

import java.util.*;

public class Nation {
    private UUID id;
    private String name;
    private UUID leader; // UUID of the nation's leader
    private Set<UUID> settlements; // UUIDs of settlements in the nation
    private Map<UUID, NationRelationType> relations; // Relations with other nations

    // Constructor for creating a new nation
    public Nation(String name, UUID leaderId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.leader = leaderId;
        this.settlements = new HashSet<>();
        this.relations = new HashMap<>();
    }

    // Constructor for loading a nation from the database with existing data
    public Nation(UUID id, String name, UUID leaderId, Set<UUID> settlements, Map<UUID, NationRelationType> relations) {
        this.id = id;
        this.name = name;
        this.leader = leaderId;
        this.settlements = settlements != null ? settlements : new HashSet<>();
        this.relations = relations != null ? relations : new HashMap<>();
    }

    // Getters and setters

    // ID
    public UUID getId() {
        return id;
    }

    // In case you need to set the ID (e.g., when loading from database)
    public void setId(UUID id) {
        this.id = id;
    }

    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Leader
    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    // Settlements
    public Set<UUID> getSettlements() {
        return settlements;
    }

    public void setSettlements(Set<UUID> settlements) {
        this.settlements = settlements;
    }

    public void addSettlement(UUID settlementId) {
        settlements.add(settlementId);
    }

    public void removeSettlement(UUID settlementId) {
        settlements.remove(settlementId);
    }

    // Relations
    public Map<UUID, NationRelationType> getRelations() {
        return relations;
    }

    public void setRelations(Map<UUID, NationRelationType> relations) {
        this.relations = relations;
    }

    public void setRelation(UUID nationId, NationRelationType relation) {
        relations.put(nationId, relation);
    }

    public NationRelationType getRelation(UUID nationId) {
        return relations.getOrDefault(nationId, NationRelationType.NEUTRAL);
    }

    // Additional methods

    // Check if a settlement is part of the nation
    public boolean hasSettlement(UUID settlementId) {
        return settlements.contains(settlementId);
    }

    public boolean isMember(UUID playerId) {
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(playerId);
        if(settleOpt.isPresent()){
            SettleRegion settle = settleOpt.get();
            return settlements.contains(settle.getId());
        }
        return false;
    }
}
