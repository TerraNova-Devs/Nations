package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.professions.Profession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessionDAO {
    private static final String SELECT_ALL = "SELECT * FROM professions";

    public static List<Profession> getAllProfessions() {
        List<Profession> list = new ArrayList<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Profession p = new Profession(
                        rs.getInt("ProfessionID"),
                        rs.getString("Type"),
                        rs.getInt("Level"),
                        rs.getInt("Score"),
                        rs.getInt("Price")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
