package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.worldguard.math.Vectore2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GridRegionDAO {

    private static final Map<String, String> queries = new HashMap<>();

    static {
        queries.put("insert", "INSERT INTO `grid_regions` (`RUUID`, `name`, `type`, `location`) VALUES (?, ?, ?, ?);");
        queries.put("remove", "DELETE FROM `grid_regions` WHERE `RUUID` = ?;");
        queries.put("updateName", "UPDATE `grid_regions` SET `name` = ? WHERE `RUUID` = ?;");
        queries.put("fetchByType", "SELECT * FROM `grid_regions` WHERE `type` = ?;");
    }

    public static void insertGridRegion(GridRegion gridRegion) {
        String sql = queries.get("insert");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gridRegion.getId().toString());
            ps.setString(2, gridRegion.getName());
            ps.setString(3, gridRegion.getType());
            ps.setString(4, gridRegion.getLocation().asString());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to insert grid region: " + gridRegion.getName());
        }
    }

    public static void removeRegion(UUID ruuid) {
        String sql = queries.get("remove");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to remove grid region: " + ruuid.toString());
        }
    }

    public static void updateRegionName(UUID ruuid, String name) {
        String sql = queries.get("updateName");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, ruuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to update grid region name: " + name);
        }
    }

    public static Map<UUID, Region> fetchRegionsByType(String type) {
        String sql = queries.get("fetchByType");
        Map<UUID, Region> gridRegions = new HashMap<>();
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("RUUID"));
                gridRegions.put(id, Region.retrieveRegion(rs.getString("type"), rs.getString("name"), id, new Vectore2(rs.getString("location"))).get());
            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to fetch regions by type: " + type);
        }
        return gridRegions;
    }
}