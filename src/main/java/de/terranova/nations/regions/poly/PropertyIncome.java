package de.terranova.nations.regions.poly;

import de.terranova.nations.database.dao.PropertyIncomeDAO;
import de.terranova.nations.regions.bank.InstantGenerator;

import java.time.Instant;
import java.util.UUID;

public class PropertyIncome {
    private String regionUUID;
    private String playerUUID;
    private String propertyID;
    private int income;
    private Instant timestamp;
    private boolean collected;

    public PropertyIncome(String regionUUID, String playerUUID, String propertyID, int income, Instant timestamp, boolean collected) {
        this.regionUUID = regionUUID;
        this.playerUUID = playerUUID;
        this.propertyID = propertyID;
        this.income = income;
        this.timestamp = timestamp;
        this.collected = collected;
    }

    public PropertyIncome(String regionUUID, String playerUUID, String propertyID, int income) {
        this.regionUUID = regionUUID;
        this.playerUUID = playerUUID;
        this.propertyID = propertyID;
        this.income = income;
        this.timestamp = InstantGenerator.generateInstant(propertyID);
        this.collected = false;
    }

    public String getRegionUUID() {
        return regionUUID;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public String getPropertyID() {
        return propertyID;
    }

    public int getIncome() {
        return income;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
        PropertyIncomeDAO.setCollected(regionUUID, playerUUID, collected);
    }
}
