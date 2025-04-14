package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.poly.PropertyIncome;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyIncomeDAO {
    private static final Map<String, String> queries = new HashMap<>();

//    --
//    CREATE TABLE IF NOT EXISTS `property_incomes`
//            (
//            `RUUID`     varchar(36) NOT NULL,
//    `PUUID`     varchar(36) NOT NULL,
//    `PropertyID` varchar(36) NOT NULL,
//    `income`    int NOT NULL DEFAULT 0,
//            `timestamp` timestamp(6) NOT NULL,
//    `collected`    TINYINT(1)  NOT NULL DEFAULT 0,
//    PRIMARY KEY (`RUUID`, `PUUID`, `timestamp`),
//    FOREIGN KEY (`RUUID`) REFERENCES `grid_regions` (`RUUID`) ON DELETE CASCADE
//) DEFAULT CHARSET = utf8
//    COLLATE = utf8_unicode_ci;
//--
    static {
        queries.put("insert", "INSERT INTO `property_incomes` (`RUUID`, `PUUID`, `PropertyID`, `income`, `timestamp`, `collected`) VALUES (?, ?, ?, ?, ?, ?);");
        queries.put("remove", "DELETE FROM `property_incomes` WHERE `RUUID` = ?;");
        queries.put("setCollected", "UPDATE `property_incomes` SET `collected` = ? WHERE `RUUID` = ? AND `PUUID` = ?;");
        queries.put("getIncomes", "SELECT * FROM `property_incomes` WHERE `RUUID` = ? AND `PUUID` = ?;");
    }

    public static void insertIncome(PropertyIncome income) {
        String sql = queries.get("insert");
        try (var conn = NationsPlugin.hikari.dataSource.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, income.getRegionUUID());
            ps.setString(2, income.getPlayerUUID());
            ps.setString(3, income.getPropertyID());
            ps.setInt(4, income.getIncome());
            ps.setTimestamp(5, Timestamp.from(income.getTimestamp()));
            ps.setBoolean(6, income.isCollected());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeIncome(String ruuid) {
        String sql = queries.get("remove");
        try (var conn = NationsPlugin.hikari.dataSource.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCollected(String ruuid, String puuid, boolean collected) {
        String sql = queries.get("setCollected");
        try (var conn = NationsPlugin.hikari.dataSource.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, collected);
            ps.setString(2, ruuid);
            ps.setString(3, puuid);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<PropertyIncome> getIncomes(String ruuid, String puuid) {
        String sql = queries.get("getIncomes");
        try (var conn = NationsPlugin.hikari.dataSource.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid);
            ps.setString(2, puuid);
            var rs = ps.executeQuery();
            List<PropertyIncome> incomes = new ArrayList<>();
            while (rs.next()) {
                PropertyIncome income = new PropertyIncome(
                        rs.getString("RUUID"),
                        rs.getString("PUUID"),
                        rs.getString("PropertyID"),
                        rs.getInt("income"),
                        Instant.parse(rs.getString("timestamp")),
                        rs.getBoolean("collected")
                );
                incomes.add(income);
            }
            return incomes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
