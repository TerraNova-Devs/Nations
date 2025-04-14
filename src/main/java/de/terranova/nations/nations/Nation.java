package de.terranova.nations.nations;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.dao.NationsDAO;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.ItemStackSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Nation {
    private UUID id;
    private String name;
    private Map<UUID, SettlementRank> settlements; // UUIDs of settlements in the nation
    private Map<UUID, NationRelationType> relations; // Relations with other nations
    private Map<UUID, NationPlayerRank> playerRanks; // Player ranks in the nation

    private String bannerBase64;

    // Constructor for creating a new nation
    public Nation(String name, UUID leaderId, UUID settleId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.settlements = new HashMap<>();
        this.settlements.put(settleId, SettlementRank.CAPITAL);
        this.relations = new HashMap<>();
        NationsPlugin.nationManager.getNations().forEach((id, nation) -> {
            nation.setRelation(this.id, NationRelationType.NEUTRAL);
            NationsPlugin.nationManager.saveNation(nation);
            setRelation(nation.getId(), NationRelationType.NEUTRAL);
        });
        this.playerRanks = new HashMap<>();
        this.playerRanks.put(leaderId, NationPlayerRank.LEADER);
        this.bannerBase64 = null;
    }

    // Constructor for loading a nation from the database with existing data
    public Nation(UUID id, String name, Map<UUID, SettlementRank> settlements, Map<UUID, NationRelationType> relations, Map<UUID, NationPlayerRank> playerRanks, String bannerBase64) {
        this.id = id;
        this.name = name;
        this.settlements = settlements != null ? settlements : new HashMap<>();
        this.relations = relations != null ? relations : new HashMap<>();
        this.playerRanks = playerRanks != null ? playerRanks : new HashMap<>();
        this.bannerBase64 = bannerBase64;
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
        return playerRanks.entrySet().stream()
                .filter(entry -> entry.getValue() == NationPlayerRank.LEADER)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Set<UUID> getViceLeaders() {
        return playerRanks.entrySet().stream()
                .filter(entry -> entry.getValue() == NationPlayerRank.VICE_LEADER)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<UUID> getCouncil() {
        return playerRanks.entrySet().stream()
                .filter(entry -> entry.getValue() == NationPlayerRank.COUNCIL)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void setLeader(UUID leader) {
        this.playerRanks.values().remove(NationPlayerRank.LEADER);
        this.playerRanks.put(leader, NationPlayerRank.LEADER);
    }

    // Settlements
    public Map<UUID, SettlementRank> getSettlements() {
        return settlements;
    }
    public UUID getCapital() {
        return settlements.entrySet().stream()
                .filter(entry -> entry.getValue() == SettlementRank.CAPITAL)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public void setSettlements(Map<UUID, SettlementRank> settlements) {
        this.settlements = settlements;
    }

    public void addSettlement(UUID settlementId, SettlementRank rank) {
        settlements.put(settlementId, rank);
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

    public void broadcast(String message) {
        settlements.forEach((settlementId, rank) -> {
            Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", settlementId);
            if (settleOpt.isEmpty()) return;
            SettleRegion settle = settleOpt.get();
            settle.getAccess().broadcast(message);
        });
    }

    // Additional methods

    // Check if a settlement is part of the nation
    public boolean hasSettlement(UUID settlementId) {
        return settlements.containsKey(settlementId);
    }

    public boolean isMember(UUID playerId) {
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(playerId);
        if(settleOpt.isPresent()){
            SettleRegion settle = settleOpt.get();
            return settlements.containsKey(settle.getId());
        }
        return false;
    }

    public boolean isLeader(UUID playerId) {
        return playerRanks.get(playerId) == NationPlayerRank.LEADER;
    }

    public void setPlayerRank(UUID playerId, NationPlayerRank rank) {
        playerRanks.put(playerId, rank);
    }

    public Map<UUID, NationPlayerRank> getPlayerRanks() {
        return playerRanks;
    }

    public void removePlayerRank(UUID playerId) {
        playerRanks.remove(playerId);
        NationsDAO.removePlayerRankFromNation(playerId);
    }

    public void setPlayerRanks(Map<UUID, NationPlayerRank> playerRanks) {
        this.playerRanks = playerRanks;
    }

    public NationPlayerRank getPlayerRank(UUID playerId) {
        return playerRanks.getOrDefault(playerId, NationPlayerRank.MEMBER);
    }

    public ItemStack getBanner() {
        return ItemStackSerializer.getItemStackFromBase64String(bannerBase64);
    }

    public void setBanner(ItemStack banner) {
        this.bannerBase64 =  ItemStackSerializer.getBase64StringFromItemStack(banner);
    }

    public String getBannerBase64() {
        return bannerBase64;
    }

    public void setBannerBase64(String bannerBase64) {
        this.bannerBase64 = bannerBase64;
    }
}
