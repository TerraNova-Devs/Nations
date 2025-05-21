package de.nekyia.nations.regions.bank;

import de.nekyia.nations.NationsPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static de.nekyia.nations.NationsPlugin.plugin;

public class BankDatabase {

    private final Map<String, String> queries = new HashMap<>();
    private final String ruuid;

    public BankDatabase(UUID ruuid) {
        this.ruuid = ruuid.toString();
        try {
            final String[] rawQueries = new String(
                    Objects.requireNonNull(plugin.getResource("database/bank.sql")).readAllBytes(),
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
    }

    public List<Transaction> getLatestTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = queries.get("get latest transactions");
        if (sql == null) throw new IllegalArgumentException("Query not found!");

        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set the parameter for ruuid
            ps.setString(1, ruuid);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getString("user"),
                            rs.getInt("amount"),
                            rs.getTimestamp("date").toInstant(),
                            rs.getInt("total")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public Optional<Transaction> getLatestBankStatus() {
        String sql = queries.get("get bank credit");
        if (sql == null) throw new IllegalArgumentException("Query not found!");

        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set the parameter for ruuid
            ps.setString(1, ruuid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Transaction(
                            rs.getString("user"),
                            rs.getInt("amount"),
                            rs.getTimestamp("date").toInstant(),
                            rs.getInt("total"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void insertTransaction(Transaction transaction) {
        String sql = queries.get("insert value into bank");
        String sql2 = queries.get("check for more than 50 entries");
        if (sql == null) throw new IllegalArgumentException("Query not found!");

        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps2 = conn.prepareStatement(sql2);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruuid);
            ps.setString(2, transaction.user);
            ps.setInt(3, transaction.amount);
            ps.setObject(4, transaction.instant);
            ps.setInt(5, transaction.total);
            System.out.println("Prepared statement for insertion: " + ps);
            ps.executeUpdate();
            ps2.setString(1, ruuid);
            ps2.setString(2, ruuid);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllEntries() {
        String sql = queries.get("delete ruuid from bank");
        if (sql == null) throw new IllegalArgumentException("Query not found!");

        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Set the RUUID parameter
            ps.setString(1, ruuid);
            // Execute the deletion
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
