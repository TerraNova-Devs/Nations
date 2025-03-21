package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SettlementBuildingsDAO {
    private static final String SELECT_BUILT = """
       SELECT BuildingID 
       FROM settlement_buildings
       WHERE RUUID=? AND IsBuilt=1
    """;
    private static final String INSERT_OR_UPDATE = """
       INSERT INTO settlement_buildings (RUUID, BuildingID, IsBuilt)
       VALUES (?, ?, ?)
       ON DUPLICATE KEY UPDATE IsBuilt=VALUES(IsBuilt);
    """;

    private static final String SELECT_IS_BUILT = """
       SELECT IsBuilt FROM settlement_buildings
       WHERE RUUID=? AND BuildingID=?;
    """;

    public static boolean isBuilt(String ruuid, int buildingId) {
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_IS_BUILT)) {
            ps.setString(1, ruuid);
            ps.setInt(2, buildingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("IsBuilt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gibt alle BuildingIDs zurück, die "IsBuilt=1" für diese Stadt haben.
     */
    public static Set<Integer> getBuiltBuildings(String ruuid) {
        Set<Integer> result = new HashSet<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BUILT)) {
            ps.setString(1, ruuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt("BuildingID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void setBuilt(String ruuid, int buildingId, boolean built) {
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_OR_UPDATE)) {
            ps.setString(1, ruuid);
            ps.setInt(2, buildingId);
            ps.setBoolean(3, built);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
