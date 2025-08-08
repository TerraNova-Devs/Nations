package de.terranova.nations.nations;

import java.util.UUID;

public class SettlementNationRelation {
  private UUID settlementId;
  private UUID nationId;
  private SettlementRank rank;

  public SettlementNationRelation(UUID settlementId, UUID nationId, SettlementRank rank) {
    this.settlementId = settlementId;
    this.nationId = nationId;
    this.rank = rank;
  }

  // Getters and setters
  public UUID getSettlementId() {
    return settlementId;
  }

  public void setSettlementId(UUID settlementId) {
    this.settlementId = settlementId;
  }

  public UUID getNationId() {
    return nationId;
  }

  public void setNationId(UUID nationId) {
    this.nationId = nationId;
  }

  public SettlementRank getRank() {
    return rank;
  }

  public void setRank(SettlementRank rank) {
    this.rank = rank;
  }
}
