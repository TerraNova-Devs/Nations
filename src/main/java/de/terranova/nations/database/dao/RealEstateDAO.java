package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateData;

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
    }

    public static void upsertRealEstate(RealEstateAgent agent) {
        String sql = queries.get("upsertRealEstate");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, agent.getRegion().getId().toString());
            ps.setString(2, agent.getRegionLandlord().toString());
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
    
    public static RealEstateData getRealEstateById(UUID ruuid) {
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
                    return new RealEstateData(puuid, isForBuy, buyPrice, isForRent, rentPrice, timestamp);
                }
            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to fetch real estate by id: " + e.getMessage());
        }
        return new RealEstateData(null, false, 0, false, 0, null);
    }
}
