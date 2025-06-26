package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.modules.bank.Transaction;

import java.sql.*;
import java.util.*;

public class BankDAO {

    private static final Map<String, String> queries = new HashMap<>();

    static {
        queries.put("getLatestTransactions", "SELECT `user`, `credit` AS amount, `timestamp` AS date, `total` FROM `bank` WHERE `RUUID` = ? ORDER BY `timestamp` ASC LIMIT 50;");
        queries.put("getBankCredit", "SELECT `user`, `credit` AS amount, `timestamp` AS date, `total` FROM `bank` WHERE `RUUID` = ? ORDER BY `timestamp` DESC LIMIT 1;");
        queries.put("insert", "INSERT INTO `bank` (`RUUID`, `user`, `credit`, `timestamp`, `total`) VALUES (?, ?, ?, ?, ?);");
        queries.put("delete", "DELETE FROM `bank` WHERE `RUUID` = ?;");
        queries.put("checkEntries", "DELETE FROM `bank` WHERE `RUUID` = ? AND `timestamp` NOT IN (SELECT `timestamp` FROM (SELECT `timestamp` FROM `bank` WHERE `RUUID` = ? ORDER BY `timestamp` DESC LIMIT 50) AS recent_timestamps) LIMIT 1;");
    }

    public List<Transaction> getLatestTransactions(UUID ruuid) {
        String sql = queries.get("getLatestTransactions");
        List<Transaction> transactions = new ArrayList<>();
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, ruuid.toString());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(rs.getString("user"), rs.getInt("amount"), rs.getTimestamp("date").toInstant(), rs.getInt("total")));
            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to fetch transactions for " + ruuid.toString());
        }
        return transactions;
    }

    public Optional<Transaction> getBankCredit(UUID ruuid) {
        String sql = queries.get("getBankCredit");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, ruuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(new Transaction(rs.getString("user"), rs.getInt("amount"), rs.getTimestamp("date").toInstant(), rs.getInt("total")));
            }
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to fetch bank credit for " + ruuid.toString());
        }
        return Optional.empty();
    }

    public void insertTransaction(UUID ruuid, Transaction transaction) {
        String sql = queries.get("insert");
        String sql2 = queries.get("checkEntries");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps2 = con.prepareStatement(sql2);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ps.setString(2, transaction.user);
            ps.setInt(3, transaction.amount);
            ps.setTimestamp(4, Timestamp.from(transaction.instant));
            ps.setInt(5, transaction.total);
            ps.executeUpdate();
            ps2.setString(1, ruuid.toString());
            ps2.setString(2, ruuid.toString());
            ps2.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to insert transaction for " + ruuid.toString());
        }
    }

    public void deleteAllEntries(UUID ruuid) {
        String sql = queries.get("delete");
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            NationsPlugin.plugin.getLogger().severe("Failed to delete all entries for " + ruuid.toString());
        }
    }
}