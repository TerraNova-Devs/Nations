package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateListing;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RealEstateDAO {
    private static final Map<String, String> queries = new HashMap<>();

    static {
        queries.put("selectRealEstateById",
                "SELECT * FROM `nations_realestate` WHERE `RUUID` = ?;");
        queries.put("upsertRealEstate",
                "INSERT INTO `nations_realestate` (`RUUID`, `PUUID`, `isForBuy`, `buyPrice`, `isForRent`, `rentPrice`, `timestamp`) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "`PUUID` = VALUES(`PUUID`), " +
                        "`isForBuy` = VALUES(`isForBuy`), " +
                        "`buyPrice` = VALUES(`buyPrice`), " +
                        "`isForRent` = VALUES(`isForRent`), " +
                        "`rentPrice` = VALUES(`rentPrice`), " +
                        "`timestamp` = VALUES(`timestamp`);");
        queries.put("removeRealEstate",
                "DELETE FROM `nations_realestate` WHERE `RUUID` = ?;");

        queries.put("upsertHolding",
                "INSERT INTO `nations_holdings` (`PUUID`, `amount`) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE `amount` = VALUES(`amount`);");
        queries.put("removeHolding",
                "DELETE FROM `nations_holdings` WHERE `PUUID` = ?;");
        queries.put("selectHoldingByPUUID",
                "SELECT `PUUID`, `amount` FROM `nations_holdings`;");
    }

    public static void upsertRealEstate(RealEstateAgent agent) {
        String sql = queries.get("upsertRealEstate");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, agent.getRegion().getId().toString());
            ps.setString(2, agent.getLandlord().toString());
            ps.setBoolean(3, agent.isForBuy());
            ps.setInt(4, agent.getBuyPrice());
            ps.setBoolean(5, agent.isForRent());
            ps.setInt(6, agent.getRentPrice());
            ps.setTimestamp(7, Timestamp.from(agent.getTimestamp()));

            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to upsert real estate entry: " + e.getMessage());
        }
    }

    public static void removeRealEstate(RealEstateAgent agent) {
        String sql = queries.get("removeRealEstate");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, agent.getRegion().getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to remove real estate entry: " + e.getMessage());
        }
    }
    
    public static RealEstateListing getRealEstateById(UUID ruuid) {
        String sql = queries.get("selectRealEstateById");

        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ruuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID puuid = UUID.fromString(rs.getString("PUUID"));
                    boolean isForBuy = rs.getBoolean("isForBuy");
                    int buyPrice = rs.getInt("buyPrice");
                    boolean isForRent = rs.getBoolean("isForRent");
                    int rentPrice = rs.getInt("rentPrice");
                    Instant timestamp = rs.getTimestamp("timestamp").toInstant();
                    return new RealEstateListing(puuid, isForBuy, buyPrice, isForRent, rentPrice, timestamp);
                }
            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to fetch real estate by id: " + e.getMessage());
        }
        return new RealEstateListing(null, false, 0, false, 0, null);
    }
    public static void upsertHolding(UUID puuid, int amount) {
        if (amount == 0){
            removeHolding(puuid);
            return;
        }
        String sql = queries.get("upsertHolding");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, puuid.toString());
            ps.setInt(2, amount);

            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to upsert holding: " + e.getMessage());
        }
    }

    public static void removeHolding(UUID puuid) {
        String sql = queries.get("removeHolding");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, puuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to remove holding: " + e.getMessage());
        }
    }

    public static void loadAllHoldings() {
        String sql = queries.get("selectHoldingByPUUID");

        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            RealEstateListing.holdings.clear(); // clear existing cache
            while (rs.next()) {
                try {
                    UUID puuid = UUID.fromString(rs.getString("PUUID"));
                    int amount = rs.getInt("amount");
                    RealEstateListing.holdings.put(puuid, amount);
                } catch (IllegalArgumentException e) {
                    NationsPlugin.plugin.getLogger().warning("Skipping invalid PUUID in holdings: " + rs.getString("PUUID"));
                }
            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to load holdings: " + e.getMessage());
        }
    }
}
