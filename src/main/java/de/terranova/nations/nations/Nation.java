package de.terranova.nations.nations;

import java.util.*;

public class Nation {
    private UUID id;
    private String name;
    private UUID leader; // UUID of the nation's leader
    private Set<UUID> settlements; // UUIDs of settlements in the nation
    private Set<UUID> members; // UUIDs of players in the nation
    private Map<UUID, NationRelationType> relations; // Relations with other nations

    public Nation(String name, UUID leaderId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.leader = leaderId;
        this.settlements = new HashSet<>();
        this.members = new HashSet<>();
        this.relations = new HashMap<>();
        // Add the leader to the members list
        this.members.add(leaderId);
    }

    // Getters and setters

    // Methods to add/remove settlements
    public void addSettlement(UUID settlementId) {
        settlements.add(settlementId);
    }

    public void removeSettlement(UUID settlementId) {
        settlements.remove(settlementId);
    }

    // Methods to manage members
    public void addMember(UUID playerId) {
        members.add(playerId);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    // Methods to manage relations
    public void setRelation(UUID nationId, NationRelationType relation) {
        relations.put(nationId, relation);
    }

    public NationRelationType getRelation(UUID nationId) {
        return relations.getOrDefault(nationId, NationRelationType.NEUTRAL);
    }
}
