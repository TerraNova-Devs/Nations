package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.professions.ProfessionObjective;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessionObjectiveDAO {
    private static final String SELECT_BY_PROFESSION = "SELECT * FROM profession_objectives WHERE ProfessionID=?";

    public static List<ProfessionObjective> getObjectivesForProfession(int professionId) {
        List<ProfessionObjective> list = new ArrayList<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_PROFESSION)) {
            ps.setInt(1, professionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProfessionObjective obj = new ProfessionObjective(
                        rs.getInt("ObjectiveID"),
                        rs.getInt("ProfessionID"),
                        rs.getString("Action"),
                        rs.getString("Object"),
                        rs.getLong("Amount")
                );
                list.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
