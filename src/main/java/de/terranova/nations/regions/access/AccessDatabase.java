package de.terranova.nations.regions.access;

import de.terranova.nations.NationsPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static de.terranova.nations.NationsPlugin.plugin;

public class AccessDatabase {

    private final Map<String, String> queries = new HashMap<>();
    private final String ruuid;

    public AccessDatabase(UUID ruuid) {
        this.ruuid = ruuid.toString();
        try {
            final String[] rawQueries = new String(
                    Objects.requireNonNull(plugin.getResource("database/access.sql")).readAllBytes(),
                    StandardCharsets.UTF_8
            ).split("-- ");

            for (String part : rawQueries) {
                String[] lines = part.trim().split("\n", 2);
                if (lines.length == 2) {
                    queries.put(lines[0].trim(), lines[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<UUID, AccessLevel> getMembersAccess() {
        String sql = queries.get("get all members access");
        if (sql == null) throw new IllegalArgumentException("Query not found!");
        HashMap<UUID, AccessLevel> access = new HashMap<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, ruuid);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                access.put(UUID.fromString(rs.getString("PUUID")), AccessLevel.valueOf(rs.getString("access")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
        return access;
    }

    public void changeMemberAccess(UUID PUUID, AccessLevel access) {
        queries.forEach((y,x) -> {
            System.out.println(y + "<------------->" +  x);
        });
        if (access == null) {
            String sql = queries.get("remove a members access");
            if (sql == null) throw new IllegalArgumentException("Query not found!");
            try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
                 PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, ruuid);
                statement.setString(2, PUUID.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
            }
        } else {
            String sql = queries.get("add a members access");
            if (sql == null) throw new IllegalArgumentException("Query not found!");
            try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
                 PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, ruuid);
                statement.setString(2, PUUID.toString());
                statement.setString(3, access.name());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
            }
        }
    }
    public void removeEveryAccess() {
        String sql = queries.get("remove every members access");
        if (sql == null) throw new IllegalArgumentException("Query not found!");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, ruuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }
}
