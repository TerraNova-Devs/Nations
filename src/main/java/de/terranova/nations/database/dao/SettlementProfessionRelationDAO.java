package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.professions.ProfessionStatus;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SettlementProfessionRelationDAO {

    private static final String SELECT_STATUS = """
       SELECT Status FROM settlement_profession_relation
       WHERE RUUID=? AND ProfessionID=?
    """;

    private static final String UPDATE_STATUS = """
       UPDATE settlement_profession_relation
       SET Status=? WHERE RUUID=? AND ProfessionID=?
    """;

    private static final String SELECT_ALL_STATUSES = """
       SELECT ProfessionID, Status FROM settlement_profession_relation
       WHERE RUUID=?
    """;

    private static final String UPSERT_RELATION = """
       INSERT INTO settlement_profession_relation (RUUID, ProfessionID, Status)
       VALUES (?, ?, ?)
       ON DUPLICATE KEY UPDATE Status=VALUES(Status)
    """;

    private static final String SELECT_ACTIVE_STATUS = """
       SELECT ProfessionID FROM settlement_profession_relation
       WHERE RUUID=? AND Status='ACTIVE'
    """;

    /**
     * Liefert den Status einer konkreten Profession in einer Stadt.
     * Falls nichts in DB steht, interpretieren wir das als LOCKED.
     */
    public static ProfessionStatus getStatus(String ruuid, int professionID) {
        ProfessionStatus status = ProfessionStatus.LOCKED; // default
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_STATUS)) {
            ps.setString(1, ruuid);
            ps.setInt(2, professionID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                status = ProfessionStatus.valueOf(rs.getString("Status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * Setzt den Status in der DB.
     * Falls kein Eintrag existiert, wird er angelegt.
     */
    public static void setStatus(String ruuid, int professionID, ProfessionStatus newStatus) {
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(UPSERT_RELATION)) {
            ps.setString(1, ruuid);
            ps.setInt(2, professionID);
            ps.setString(3, newStatus.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lädt alle ProfessionID->Status für eine bestimmte Stadt.
     */
    public static Map<Integer, ProfessionStatus> getAllStatuses(String ruuid) {
        Map<Integer, ProfessionStatus> map = new HashMap<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_STATUSES)) {
            ps.setString(1, ruuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int pid = rs.getInt("ProfessionID");
                String st = rs.getString("Status");
                ProfessionStatus stat = ProfessionStatus.valueOf(st);
                map.put(pid, stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Liefert die ID der aktiven Profession einer Stadt.
     */
    public static Integer getActiveProfessionID(String ruuid) {
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ACTIVE_STATUS)) {
            ps.setString(1, ruuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ProfessionID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
