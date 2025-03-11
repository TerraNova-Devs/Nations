package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.professions.Building;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingDAO {
    private static final String SELECT_BY_PROFESSION = "SELECT * FROM buildings WHERE ProfessionID=?";
    private static final String SELECT_ONE = "SELECT * FROM buildings WHERE BuildingID=?";

    public static List<Building> getBuildingsByProfession(int professionId) {
        List<Building> result = new ArrayList<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_PROFESSION)) {
            ps.setInt(1, professionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Building b = new Building(
                        rs.getInt("BuildingID"),
                        rs.getInt("ProfessionID"),
                        rs.getString("Name"),
                        rs.getString("Description")
                );
                result.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Building getBuilding(int buildingId) {
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ONE)) {
            ps.setInt(1, buildingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Building(
                        rs.getInt("BuildingID"),
                        rs.getInt("ProfessionID"),
                        rs.getString("Name"),
                        rs.getString("Description")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
