package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettlementObjectiveProgressDAO {

    private static final String SELECT_ALL = """
               SELECT ObjectiveID, Progress
               FROM settlement_objective_progress
               WHERE RUUID=?
            """;

    private static final String UPSERT = """
               INSERT INTO settlement_objective_progress (RUUID, ObjectiveID, Progress)
               VALUES (?, ?, ?)
               ON DUPLICATE KEY UPDATE Progress=VALUES(Progress)
            """;

    private static final String INSERT_OR_UPDATE = """
               INSERT INTO settlement_objective_progress (RUUID, ObjectiveID, Progress)
               VALUES (?, ?, ?)
               ON DUPLICATE KEY UPDATE Progress=VALUES(Progress);
            """;

    private static final String SELECT_PROGRESS = """
               SELECT Progress FROM settlement_objective_progress
               WHERE RUUID=? AND ObjectiveID=?;
            """;

    /**
     * Lädt den gesamten Fortschritt aller Objectives für eine bestimmte Stadt.
     * Gibt ein Map<ObjectiveID, Progress> zurück.
     */
    public static Map<String, Long> getAllProgress(String ruuid) {
        Map<String, Long> map = new HashMap<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_ALL)) {
            ps.setString(1, ruuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String objId = rs.getString("ObjectiveID");
                long prog = rs.getLong("Progress");
                map.put(objId, prog);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Setzt den Fortschritt eines konkreten Objectives auf einen neuen Wert
     * (anstatt nur +1 wie in addProgress).
     */
    public static void setProgress(String ruuid, String objectiveId, long newProgress) {
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPSERT)) {
            ps.setString(1, ruuid);
            ps.setString(2, objectiveId);
            ps.setLong(3, newProgress);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static long getProgress(String ruuid, String objectiveId) {
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_PROGRESS)) {
            ps.setString(1, ruuid);
            ps.setString(2, objectiveId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("Progress");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void addProgress(String ruuid, String objectiveId, long delta) {
        long current = getProgress(ruuid, objectiveId);
        long updated = current + delta;

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT_OR_UPDATE)) {
            ps.setString(1, ruuid);
            ps.setString(2, objectiveId);
            ps.setLong(3, updated);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
