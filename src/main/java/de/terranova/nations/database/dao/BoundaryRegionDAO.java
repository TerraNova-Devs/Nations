package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.terranova.nations.database.dao.GridRegionDAO.fetchParent;

public class BoundaryRegionDAO {
    private static final Map<String, String> queries = new HashMap<>();

    static {
        queries.put("insert", "INSERT INTO `poly_regions` (`RUUID`, `name`, `type`) VALUES (?, ?, ?);");
        queries.put("remove", "DELETE FROM `poly_regions` WHERE `RUUID` = ?;");
        queries.put("updateName", "UPDATE `poly_regions` SET `name` = ? WHERE `RUUID` = ?;");
        queries.put("fetchByType", "SELECT * FROM `poly_regions` WHERE `type` = ?;");
    }

    public static void insertRegion(BoundaryRegion region) {
        String sql = queries.get("insert");
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, region.getId().toString());
            ps.setString(2, region.getName());
            ps.setString(3, region.getType());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to insert grid region: " + region.getName());
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
                            RegionRegistry.createFromArgs(
                                    rs.getString("type"),
                                    List.of(rs.getString("name"),
                                            id)));
                } else {
                    gridRegions.put(
                            UUID.fromString(id),
                            RegionRegistry.createFromArgs(
                                    rs.getString("type"),
                                    List.of(rs.getString("name"),
                                            id,
                                            parentId.toString())));
                }

            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to fetch regions by type: " + type);
        }
        return gridRegions;
    }
}
