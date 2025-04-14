package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.access.PropertyAccessLevel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A DAO for reading/writing the property_access table.
 * Each row: RUUID, PUUID, access (string).
 */
public class PropertyAccessDAO {
    // We can keep prepared statements or keep them as static queries
    private static final Map<String, String> queries = new HashMap<>();

    static {
        // The queries are basically the same as in AccessDAO,
        // but referencing "property_access" instead of "access"
        queries.put("getAll",
                "SELECT * FROM `property_access` WHERE RUUID = ?;");
        queries.put("remove",
                "DELETE FROM `property_access` WHERE RUUID = ? AND PUUID = ?;");
        queries.put("add",
                "INSERT INTO `property_access` (RUUID, PUUID, access) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE access = VALUES(access);");
        queries.put("removeAll",
                "DELETE FROM `property_access` WHERE RUUID = ?;");
    }

    /**
     * Loads all members (PUUID -> PropertyAccessLevel) for a given property region ID.
     */
    public static Map<UUID, PropertyAccessLevel> getMembersAccess(UUID ruuid) {
        String sql = queries.get("getAll");
        Map<UUID, PropertyAccessLevel> map = new HashMap<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("PUUID"));
                PropertyAccessLevel lvl = PropertyAccessLevel.valueOf(rs.getString("access"));
                map.put(playerId, lvl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Sets or removes a player's access.
     * If 'access' is null, it removes the row.
     * Otherwise it upserts a row with the given rank.
     */
    public static void changeMemberAccess(UUID ruuid, UUID puuid, PropertyAccessLevel access) {
        if (access == null) {
            removeMemberAccess(ruuid, puuid);
            return;
        }
        String sql = queries.get("add");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ps.setString(2, puuid.toString());
            ps.setString(3, access.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a single player's access row from the DB.
     */
    private static void removeMemberAccess(UUID ruuid, UUID puuid) {
        String sql = queries.get("remove");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ps.setString(2, puuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes all access rows for the given property region ID.
     */
    public static void removeEveryAccess(UUID ruuid) {
        String sql = queries.get("removeAll");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
