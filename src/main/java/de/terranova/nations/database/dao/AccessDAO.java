package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.modules.access.AccessLevel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccessDAO {

    private static final Map<String, String> queries = new HashMap<>();

    static {
        queries.put("getAll", "SELECT * FROM `access` WHERE RUUID = ?;");
        queries.put("remove", "DELETE FROM `access` WHERE RUUID = ? AND PUUID = ?;");
        queries.put("add", "INSERT INTO `access` (RUUID, PUUID, access) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE access = VALUES(access);");
        queries.put("removeAll", "DELETE FROM `access` WHERE `RUUID` = ?;");
    }

    public static Map<UUID, AccessLevel> getMembersAccess(UUID ruuid) {
        String sql = queries.get("getAll");
        Map<UUID, AccessLevel> access = new HashMap<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, ruuid.toString());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                access.put(UUID.fromString(rs.getString("PUUID")), AccessLevel.valueOf(rs.getString("access")));
            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to get members access: " + ruuid.toString());
        }
        return access;
    }

    public static void changeMemberAccess(UUID ruuid, UUID puuid, AccessLevel access) {
        String sql = access == null ? queries.get("remove") : queries.get("add");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, ruuid.toString());
            statement.setString(2, puuid.toString());
            if (access != null) {
                statement.setString(3, access.name());
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to change member access: " + puuid.toString());
        }
    }

    public static void removeEveryAccess(UUID ruuid) {
        String sql = queries.get("removeAll");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, ruuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to remove every access: " + ruuid.toString());
        }
    }
}
