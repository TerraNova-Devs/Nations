package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionRegistry;
import de.terranova.nations.worldguard.math.Vectore2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GridRegionDAO {

    private static final Map<String, String> queries = new HashMap<>();

    static {
        queries.put("insert", "INSERT INTO `grid_regions` (`RUUID`, `name`, `type`, `location`) VALUES (?, ?, ?, ?);");
        queries.put("remove", "DELETE FROM `grid_regions` WHERE `RUUID` = ?;");
        queries.put("updateName", "UPDATE `grid_regions` SET `name` = ? WHERE `RUUID` = ?;");
        queries.put("fetchByType", "SELECT * FROM `grid_regions` WHERE `type` = ?;");
        queries.put("fetchParent","SELECT `PUUID` FROM `parent_table` WHERE `RUUID` = ?;");
        queries.put("insertParent", "INSERT INTO `parent_table` (`RUUID`, `PUUID`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `PUUID` = VALUES(`PUUID`);");
    }

    public static void insertRegion(GridRegion gridRegion) {
        String sql = queries.get("insert");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to remove grid region: " + ruuid.toString());
        }
    }

    public static void updateRegionName(UUID ruuid, String name) {
        String sql = queries.get("updateName");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("RUUID");
                UUID uuid = UUID.fromString(id);
                UUID parentId = fetchParent(uuid);
                if(parentId == null) {
                    gridRegions.put(
                            UUID.fromString(id),
                            RegionRegistry.createFromArgs(rs.getString("type"),
                                    List.of(rs.getString("name"),
                                    id,
                                    new Vectore2(rs.getString("location")).asString())));
                } else {
                    gridRegions.put(
                            UUID.fromString(id),
                            RegionRegistry.createFromArgs(rs.getString("type"),
                                    List.of(rs.getString("name"),
                                    id,
                                    new Vectore2(rs.getString("location")).asString(),
                                    parentId.toString())));
                }

            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to fetch regions by type: " + type);
        }
        return gridRegions;
    }

    public static UUID fetchParent(UUID regionId) {
        String sql = queries.get("fetchParent");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, regionId.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return UUID.fromString(rs.getString("PUUID"));
            }

            // No parent found — that's OK, so do nothing
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("SQL error while fetching parent for region: " + regionId);
            e.printStackTrace();
        }

        return null; // no parent
    }
    public static void insertParent(UUID regionId, UUID parentId) {
        String sql = queries.get("insertParent");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, regionId.toString());
            ps.setString(2, parentId.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to insert parent for region " + regionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}