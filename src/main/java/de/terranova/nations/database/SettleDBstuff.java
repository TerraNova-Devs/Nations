package de.terranova.nations.database;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import de.terranova.nations.settlements.RegionTypes.Transaction;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.worldguard.math.Vectore2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

import static de.terranova.nations.NationsPlugin.plugin;

public class SettleDBstuff {

    private UUID SUUID;

    public SettleDBstuff(UUID SUUID) {
        this.SUUID = SUUID;
        try {
            if (!verifySettlement()) {
                this.SUUID = null;
                NationsPlugin.logger.warning("[DEBUG] Nations/SettleDBstuff failed to verify Settlement: " + SUUID.toString());
            } else {
                if (NationsPlugin.debug)
                    NationsPlugin.logger.warning("[DEBUG] Nations/SettleDBstuff verified Settlement: " + SUUID.toString());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB Failed to verify Settlement", e);
        }
    }

    public static void getInitialSettlementData() {
        String sql = "SELECT * FROM `settlements_table`";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            HashMap<UUID, SettleRegionType> settlements = new HashMap<>();
            while (rs.next()) {
                UUID SUUID = UUID.fromString(rs.getString("SUUID"));
                String name = rs.getString("name");
                String location = rs.getString("location");
                int level = rs.getInt("level");
                int bank = rs.getInt("bank");
                int obj_b = rs.getInt("obj_a");
                int obj_c = rs.getInt("obj_b");
                int obj_d = rs.getInt("obj_c");
                Objective objective = new Objective(0, bank, obj_b, obj_c, obj_d, null, null, null);
                if (NationsPlugin.debug)
                    NationsPlugin.logger.info("[DEBUG] Getting settlement: " + name + " | UUID: " + SUUID);

                SettleDBstuff settleDB = new SettleDBstuff(SUUID);
                settlements.put(SUUID, new SettleRegionType(SUUID, settleDB.getMembersAccess(), new Vectore2(location), name, level, objective, settleDB.getTransactionHistory()));
            }
            NationsPlugin.settleManager.setSettlements(settlements);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public static void addSettlement(UUID SUUID, String name, Vectore2 location, UUID owner) {
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

    private boolean verifySettlement() throws SQLException {
        boolean result;
        String sql = "SELECT 1 FROM `settlements_table` WHERE SUUID = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, SUUID.toString());
            ResultSet rs = statement.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
        return result;
    }

    private HashMap<UUID, AccessLevel> getMembersAccess() throws SQLException {
        String sql = "SELECT * FROM `access_table` WHERE SUUID = ?";
        HashMap<UUID, AccessLevel> access = new HashMap<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, SUUID.toString());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                access.put(UUID.fromString(rs.getString("PUUID")), AccessLevel.valueOf(rs.getString("access")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
        return access;
    }

    private List<Transaction> getTransactionHistory() throws SQLException {
        String sql = "SELECT * FROM `transaction_table` WHERE SUUID = ? ORDER BY timestamp";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, SUUID.toString());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(rs.getString("username"), rs.getInt("amount"), rs.getTimestamp("timestamp")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
        return transactions;
    }

    public void changeMemberAccess(UUID PUUID, AccessLevel access) {
        if (access == null) {
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

    public void rename(String name) {
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

    public void cash(int value,int amount, String username, Timestamp timestamp) {
        String sql_bank = "UPDATE settlements_table SET bank = ? WHERE SUUID = ?";

        // Read the content of the file
        String sql_history = null;
        try {
            sql_history = new String(Objects.requireNonNull(plugin.getResource("database/transaction.sql")).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement historyStatement = con.prepareStatement(sql_history);
             PreparedStatement statement = con.prepareStatement(sql_bank)) {
            statement.setInt(1, value);
            statement.setString(2, SUUID.toString());
            statement.executeUpdate();

            historyStatement.setString(1, SUUID.toString());
            historyStatement.setString(2, username);
            historyStatement.setInt(3, amount);
            historyStatement.setTimestamp(4, timestamp);
            historyStatement.setString(5, SUUID.toString());
            historyStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public void setLevel(int level) {
        String updateLevelSql = "UPDATE settlements_table SET level = ? WHERE SUUID = ?";
        String resetObjectivesSql = "UPDATE settlements_table SET obj_a = 0, obj_b = 0, obj_c = 0 WHERE SUUID = ?";
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

    public void syncObjectives(int obj_a, int obj_b, int obj_c) {
        String sql = "UPDATE settlements_table SET obj_a = ?, obj_b = ?, obj_c = ? WHERE SUUID = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, obj_a);
            statement.setInt(2, obj_b);
            statement.setInt(3, obj_c);
            statement.setString(4, SUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }

    public void dropSettlement() {
        String sql_settlements_table = "DELETE FROM settlements_table WHERE SUUID = ?";
        String sql_access_table = "DELETE FROM access_table WHERE SUUID = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement settlements_table_statement = con.prepareStatement(sql_settlements_table);
             PreparedStatement access_table_statement = con.prepareStatement(sql_access_table)) {

            settlements_table_statement.setString(1, SUUID.toString());
            settlements_table_statement.executeUpdate();

            access_table_statement.setString(1, SUUID.toString());
            access_table_statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials in the config file", e);
        }
    }
}
