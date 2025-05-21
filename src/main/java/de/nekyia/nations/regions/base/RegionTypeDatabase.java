package de.nekyia.nations.regions.base;

import de.nekyia.nations.NationsPlugin;
import de.nekyia.nations.worldguard.math.Vectore2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static de.nekyia.nations.NationsPlugin.plugin;

public class RegionTypeDatabase {

    private final static Map<String, String> queries = new HashMap<>();
    private final String ruuid;

    static {
        try {
            final String[] rawQueries = new String(
                    Objects.requireNonNull(plugin.getResource("database/regions.sql")).readAllBytes(),
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
        if (NationsPlugin.debug) {
            NationsPlugin.nationsDebugger.logInfo("[RegionTypeDatabase] -> loading region-related SQL queries");
            queries.forEach((name, query) -> {
                NationsPlugin.nationsDebugger.logInfo(name);
                NationsPlugin.nationsDebugger.logInfo(query);
            });
            NationsPlugin.nationsDebugger.logInfo("[RegionTypeDatabase] <- finished loading region-related SQL queries");
        }
    }

    public RegionTypeDatabase(UUID ruuid) {
        this.ruuid = ruuid.toString();
    }


    public void insertGridRegion(GridRegionType gridRegion) {
        queries.forEach((y,x) -> {
        });
        String sql = queries.get("grid region insert");
        if (sql == null) throw new IllegalArgumentException("Query not found!");


        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid);
            ps.setString(2, gridRegion.getName());
            ps.setString(3, gridRegion.type);
            ps.setString(4, gridRegion.location.asString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void removeGridRegion() {
        String sql = queries.get("grid region remove");
        if (sql == null) throw new IllegalArgumentException("Query not found!");


        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateGridRegionName(String name) {
        String sql = queries.get("grid update name");
        if (sql == null) throw new IllegalArgumentException("Query not found!");


        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<UUID, RegionType> fetchRegions(String type) {

        String sql = queries.get("grid region by type");
        if (sql == null) throw new IllegalArgumentException("Query not found!");

        HashMap<UUID, RegionType> gridRegions = new HashMap<>();
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("RUUID"));
                gridRegions.put(id, RegionType.retrieveRegionType(rs.getString("type"),rs.getString("name"),id,new Vectore2(rs.getString("location"))).get());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gridRegions;
    }



}
