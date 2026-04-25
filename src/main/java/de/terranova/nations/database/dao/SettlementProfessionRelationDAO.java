package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.professions.ProfessionStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettlementProfessionRelationDAO {

  private static final String SELECT_STATUS =
      """
               SELECT Status FROM settlement_profession_relation
               WHERE RUUID=? AND ProfessionID=?
            """;

  private static final String UPDATE_STATUS =
      """
               UPDATE settlement_profession_relation
               SET Status=? WHERE RUUID=? AND ProfessionID=?
            """;

  private static final String SELECT_ALL_STATUSES =
      """
               SELECT ProfessionID, Status FROM settlement_profession_relation
               WHERE RUUID=?
            """;

  private static final String UPSERT_RELATION =
      """
               INSERT INTO settlement_profession_relation (RUUID, ProfessionID, Status)
               VALUES (?, ?, ?)
               ON DUPLICATE KEY UPDATE Status=VALUES(Status)
            """;

  private static final String SELECT_ACTIVE_STATUS =
      """
               SELECT ProfessionID FROM settlement_profession_relation
               WHERE RUUID=? AND Status='ACTIVE'
            """;

  private static final String DELETE_BY_SETTLEMENT =
      """
             DELETE FROM settlement_profession_relation
             WHERE RUUID=?
          """;

  private static final String DELETE_RELATIONS =
          "DELETE FROM settlement_profession_relation WHERE RUUID=?";

  private static final String DELETE_OBJECTIVES =
          "DELETE FROM settlement_objective_progress WHERE RUUID=?";

  private static final String DELETE_BUILDINGS =
          "DELETE FROM settlement_buildings WHERE RUUID=?";


  /**
   * Liefert den Status einer konkreten Profession in einer Stadt. Falls nichts in DB steht,
   * interpretieren wir das als LOCKED.
   */
  public static ProfessionStatus getStatus(String ruuid, String professionID) {
    ProfessionStatus status = ProfessionStatus.LOCKED; // default
    try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(SELECT_STATUS)) {
      ps.setString(1, ruuid);
      ps.setString(2, professionID);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        status = ProfessionStatus.valueOf(rs.getString("Status"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return status;
  }

  /** Setzt den Status in der DB. Falls kein Eintrag existiert, wird er angelegt. */
  public static void setStatus(String ruuid, String professionID, ProfessionStatus newStatus) {
    try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(UPSERT_RELATION)) {
      ps.setString(1, ruuid);
      ps.setString(2, professionID);
      ps.setString(3, newStatus.name());
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /** Lädt alle ProfessionID->Status für eine bestimmte Stadt. */
  public static Map<String, ProfessionStatus> getAllStatuses(String ruuid) {
    Map<String, ProfessionStatus> map = new HashMap<>();
    try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(SELECT_ALL_STATUSES)) {
      ps.setString(1, ruuid);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        String pid = rs.getString("ProfessionID");
        String st = rs.getString("Status");
        ProfessionStatus stat = ProfessionStatus.valueOf(st);
        map.put(pid, stat);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return map;
  }

  /** Liefert die ID der aktiven Profession einer Stadt. */
  public static String getActiveProfessionID(String ruuid) {
    try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(SELECT_ACTIVE_STATUS)) {
      ps.setString(1, ruuid);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("ProfessionID");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public static void removeSettlementProfessionData(String ruuid) {
    try (Connection con = NationsPlugin.hikari.dataSource.getConnection()) {
      con.setAutoCommit(false);

      try (
              PreparedStatement psObjectives = con.prepareStatement(DELETE_OBJECTIVES);
              PreparedStatement psBuildings = con.prepareStatement(DELETE_BUILDINGS);
              PreparedStatement psRelations = con.prepareStatement(DELETE_RELATIONS)
      ) {
        psObjectives.setString(1, ruuid);
        psObjectives.executeUpdate();

        psBuildings.setString(1, ruuid);
        psBuildings.executeUpdate();

        psRelations.setString(1, ruuid);
        psRelations.executeUpdate();

        con.commit();
      } catch (SQLException e) {
        con.rollback();
        throw e;
      }

    } catch (SQLException e) {
      NationsPlugin.plugin
              .getLogger()
              .severe("Failed to fully remove profession data for settlement: " + ruuid);
      e.printStackTrace();
    }
  }
}


