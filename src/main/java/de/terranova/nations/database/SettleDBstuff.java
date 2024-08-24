package de.terranova.nations.database;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.Settlement;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.worldguard.math.Vectore2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class SettleDBstuff {

    public static void getInitialSettlementData() throws SQLException {
        String sql = "SELECT * FROM `settlements_table`";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            HashMap<UUID, Settlement> settlements = new HashMap<>();
            while (rs.next()) {
                UUID SUUID = UUID.fromString(rs.getString("SUUID"));
                String name = rs.getString("name");
                String location = rs.getString("location");
                int level = rs.getInt("level");
                int obj_a = rs.getInt("obj_a");
                int obj_b = rs.getInt("obj_b");
                int obj_c = rs.getInt("obj_c");
                int obj_d = rs.getInt("obj_d");
                Objective objective = new Objective(0, obj_a, obj_b, obj_c, obj_d, null, null, null, null);
                settlements.put(SUUID, new Settlement(SUUID, getMembersAccess(SUUID.toString()), new Vectore2(location), name, level, objective));
            }
            NationsPlugin.settlementManager.setSettlements(settlements);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public static HashMap<UUID, AccessLevelEnum> getMembersAccess(String SUUID) throws SQLException {
        String sql = "SELECT * FROM `access_table` WHERE SUUID = ?";
        HashMap<UUID, AccessLevelEnum> access = new HashMap<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, SUUID);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                access.put(UUID.fromString(rs.getString("PUUID")), AccessLevelEnum.valueOf(rs.getString("access")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
        return access;
    }

    public static void addSettlement(UUID SUUID, String name, Vectore2 location, UUID owner) throws SQLException {
        String settlementSql = "INSERT INTO `settlements_table` (`SUUID`, `Name`, `Location`) VALUES (?, ?, ?)";
        String accessSql = "INSERT INTO `access_table` (`SUUID`, `PUUID`, `ACCESS`) VALUES (?, ?, 'MAJOR')";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement settlementStatement = con.prepareStatement(settlementSql);
             PreparedStatement accessStatement = con.prepareStatement(accessSql)) {

            settlementStatement.setString(1, SUUID.toString());
            settlementStatement.setString(2, name);
            settlementStatement.setString(3, location.asString());
            settlementStatement.executeUpdate();

            accessStatement.setString(1, SUUID.toString());
            accessStatement.setString(2, owner.toString());
            accessStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public static void changeMemberAccess(UUID SUUID, UUID PUUID, AccessLevelEnum access) {
        if (access.equals(AccessLevelEnum.REMOVE)) {
            String sql = "DELETE FROM access_table WHERE SUUID = ? AND PUUID = ?";
            try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
                 PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, SUUID.toString());
                statement.setString(2, PUUID.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
            }
        } else {
            String sql = "INSERT INTO access_table (SUUID, PUUID, access) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE access = VALUES(access)";
            try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
                 PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, SUUID.toString());
                statement.setString(2, PUUID.toString());
                statement.setString(3, access.name());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
            }
        }
    }

    public static void rename(UUID SUUID, String name) {
        String sql = "UPDATE settlements_table SET name = ? WHERE SUUID = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, SUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public static void setLevel(UUID SUUID, int level) {
        String updateLevelSql = "UPDATE settlements_table SET level = ? WHERE SUUID = ?";
        String resetObjectivesSql = "UPDATE settlements_table SET obj_a = 0, obj_b = 0, obj_c = 0, obj_d = 0 WHERE SUUID = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement updateLevelStatement = con.prepareStatement(updateLevelSql);
             PreparedStatement resetObjectivesStatement = con.prepareStatement(resetObjectivesSql)) {

            updateLevelStatement.setInt(1, level);
            updateLevelStatement.setString(2, SUUID.toString());
            updateLevelStatement.executeUpdate();

            resetObjectivesStatement.setString(1, SUUID.toString());
            resetObjectivesStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public static void syncObjectives(UUID SUUID, int obj_a, int obj_b, int obj_c, int obj_d) {
        String sql = "UPDATE settlements_table SET obj_a = ?, obj_b = ?, obj_c = ?, obj_d = ? WHERE SUUID = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, obj_a);
            statement.setInt(2, obj_b);
            statement.setInt(3, obj_c);
            statement.setInt(4, obj_d);
            statement.setString(5, SUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }
}
