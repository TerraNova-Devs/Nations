package de.terranova.nations.regions.modules.rank;

import de.terranova.nations.NationsPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

import static de.terranova.nations.NationsPlugin.plugin;

public class RankDatabase {

    private final Map<String, String> queries = new HashMap<>();
    private final String ruuid;

    public RankDatabase(UUID ruuid) {
        this.ruuid = ruuid.toString();
        try {
            final String[] rawQueries = new String(
                    Objects.requireNonNull(plugin.getResource("database/rank.sql")).readAllBytes(),
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

    public void validateRank() {
        String sql = queries.get("makes sure ruuid exists in db");
        if (sql == null) throw new IllegalArgumentException("Query not found!");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, ruuid);
            preparedStatement.setString(2, ruuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public int fetchRank() {

        String sql = queries.get("fetches the data");
        if (sql == null) throw new IllegalArgumentException("Query not found!");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, ruuid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    // Retrieve the data from the result set
                    int level = rs.getInt("Level");
                    return level;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
        return 0;
    }

    public void removeRank() {
        String sql = queries.get("remove a rank");
        if (sql == null) throw new IllegalArgumentException("Query not found!");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, ruuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public void levelUp() {
        String sql = queries.get("level up");
        if (sql == null) throw new IllegalArgumentException("Query not found!");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, ruuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public void setObjective(String column, int value) {
        if (!"obj_a".equals(column) && !"obj_b".equals(column) && !"obj_c".equals(column)) {
            throw new IllegalArgumentException("Invalid column name: " + column);
        }
        String sql = String.format(queries.get("update objective"),column);
        if (sql == null) throw new IllegalArgumentException("Query not found!");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, value); // Set the value for the column
            preparedStatement.setString(2, ruuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }


}
