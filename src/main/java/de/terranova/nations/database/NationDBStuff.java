package de.terranova.nations.database;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationRelationType;
import de.terranova.nations.nations.SettlementNationRelation;
import de.terranova.nations.nations.SettlementRank;

import java.sql.*;
import java.util.*;

public class NationDBStuff {
    // Load all nations from the database
    public static List<Nation> getAllNations() {
        List<Nation> nations = new ArrayList<>();
        String sql = "SELECT * FROM nations_table";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID nationId = UUID.fromString(rs.getString("NUUID"));
                String name = rs.getString("name");
                UUID leaderId = UUID.fromString(rs.getString("leader"));

                Nation nation = new Nation(name, leaderId);
                nation.setId(nationId);

                loadNationSettlements(nation);
                loadNationMembers(nation);
                loadNationRelations(nation);

                nations.add(nation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nations;
    }

    // Save a nation to the database
    public static void saveNation(Nation nation) {
        String sqlInsertNation = "INSERT INTO nations_table (NUUID, name, leader) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), leader = VALUES(leader)";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlInsertNation)) {

            ps.setString(1, nation.getId().toString());
            ps.setString(2, nation.getName());
            ps.setString(3, nation.getLeader().toString());
            ps.executeUpdate();

            // Save other nation data
            saveNationMembers(nation);
            saveNationRelations(nation);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a nation from the database
    public static void deleteNation(UUID nationId) {
        String sqlDeleteNation = "DELETE FROM nations_table WHERE NUUID = ?";
        String sqlDeleteRelations = "DELETE FROM nation_relations WHERE NUUID1 = ? OR NUUID2 = ?";
        String sqlDeleteMembers = "DELETE FROM nation_members WHERE NUUID = ?";
        String sqlDeleteSettlementRelations = "DELETE FROM settlement_nation_relations WHERE NUUID = ?";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement psDeleteNation = con.prepareStatement(sqlDeleteNation);
             PreparedStatement psDeleteRelations = con.prepareStatement(sqlDeleteRelations);
             PreparedStatement psDeleteMembers = con.prepareStatement(sqlDeleteMembers);
             PreparedStatement psDeleteSettlementRelations = con.prepareStatement(sqlDeleteSettlementRelations)) {

            psDeleteNation.setString(1, nationId.toString());
            psDeleteNation.executeUpdate();

            psDeleteRelations.setString(1, nationId.toString());
            psDeleteRelations.setString(2, nationId.toString());
            psDeleteRelations.executeUpdate();

            psDeleteMembers.setString(1, nationId.toString());
            psDeleteMembers.executeUpdate();

            psDeleteSettlementRelations.setString(1, nationId.toString());
            psDeleteSettlementRelations.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load settlements belonging to a nation
    private static void loadNationSettlements(Nation nation) {
        String sql = "SELECT SUUID, rank FROM settlement_nation_relations WHERE NUUID = ?";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nation.getId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID settlementId = UUID.fromString(rs.getString("SUUID"));
                SettlementRank rank = SettlementRank.valueOf(rs.getString("rank"));

                nation.addSettlement(settlementId);

                // Optionally, you can store the rank information in a map if needed
                // nation.setSettlementRank(settlementId, rank);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add a settlement to a nation
    public static void addSettlementToNation(SettlementNationRelation relation) {
        String sql = "INSERT INTO settlement_nation_relations (SUUID, NUUID, rank) VALUES (?, ?, ?)";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, relation.getSettlementId().toString());
            ps.setString(2, relation.getNationId().toString());
            ps.setString(3, relation.getRank().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a settlement from a nation
    public static void removeSettlementFromNation(UUID settlementId) {
        String sql = "DELETE FROM settlement_nation_relations WHERE SUUID = ?";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, settlementId.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check if a settlement is in a nation
    public static boolean isSettlementInNation(UUID settlementId) {
        String sql = "SELECT NUUID FROM settlement_nation_relations WHERE SUUID = ?";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, settlementId.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Existing methods for members and relations...

    // Load members of a nation
    private static void loadNationMembers(Nation nation) {
        String sql = "SELECT PUUID FROM nation_members WHERE NUUID = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nation.getId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("PUUID"));
                nation.addMember(playerId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Save members of a nation
    private static void saveNationMembers(Nation nation) {
        String sqlDelete = "DELETE FROM nation_members WHERE NUUID = ?";
        String sqlInsert = "INSERT INTO nation_members (NUUID, PUUID) VALUES (?, ?)";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement psDelete = con.prepareStatement(sqlDelete);
             PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {

            psDelete.setString(1, nation.getId().toString());
            psDelete.executeUpdate();

            for (UUID playerId : nation.getMembers()) {
                psInsert.setString(1, nation.getId().toString());
                psInsert.setString(2, playerId.toString());
                psInsert.addBatch();
            }
            psInsert.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load relations of a nation
    private static void loadNationRelations(Nation nation) {
        String sql = "SELECT NUUID2, relation FROM nation_relations WHERE NUUID1 = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nation.getId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID otherNationId = UUID.fromString(rs.getString("NUUID2"));
                NationRelationType relation = NationRelationType.valueOf(rs.getString("relation"));
                nation.setRelation(otherNationId, relation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Save relations of a nation
    private static void saveNationRelations(Nation nation) {
        String sqlDelete = "DELETE FROM nation_relations WHERE NUUID1 = ?";
        String sqlInsert = "INSERT INTO nation_relations (NUUID1, NUUID2, relation) VALUES (?, ?, ?)";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement psDelete = con.prepareStatement(sqlDelete);
             PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {

            psDelete.setString(1, nation.getId().toString());
            psDelete.executeUpdate();

            for (Map.Entry<UUID, NationRelationType> entry : nation.getRelations().entrySet()) {
                psInsert.setString(1, nation.getId().toString());
                psInsert.setString(2, entry.getKey().toString());
                psInsert.setString(3, entry.getValue().name());
                psInsert.addBatch();
            }
            psInsert.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
