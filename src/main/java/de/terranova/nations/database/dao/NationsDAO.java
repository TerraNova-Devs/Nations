package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.*;
import de.terranova.nations.regions.RegionManager;

import java.sql.*;
import java.util.*;

public class NationsDAO {
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
                String bannerBase64 = rs.getString("banner_base64");

                Nation nation = new Nation(
                        nationId,
                        name,
                        null,
                        null,
                        null,
                        bannerBase64
                );

                loadNationSettlements(nation);
                loadNationRelations(nation);
                loadNationPlayerRanks(nation);

                nations.add(nation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nations;
    }

    // Save a nation to the database
    public static void saveNation(Nation nation) {
        String sqlInsertNation = "INSERT INTO nations_table (NUUID, name, banner_base64) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), banner_base64 = VALUES(banner_base64)";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlInsertNation)) {

            ps.setString(1, nation.getId().toString());
            ps.setString(2, nation.getName());
            ps.setString(3, nation.getBannerBase64());
            ps.executeUpdate();

            // Save other nation data
            // Settlements are saved when added/removed, so no need to save here
            addSettlementToNation(new SettlementNationRelation(nation.getCapital(), nation.getId(), SettlementRank.CAPITAL));
            saveNationRelations(nation);
            nation.getPlayerRanks().forEach((playerId, rank) ->
                    RegionManager.retrievePlayersSettlement(playerId).ifPresent(settleId ->
                            savePlayerRankToNation(rank, playerId, settleId.getId(), nation.getId())
                    )
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a nation from the database
    public static void deleteNation(UUID nationId) {
        String sqlDeleteNation = "DELETE FROM nations_table WHERE NUUID = ?";
        String sqlDeleteRelations = "DELETE FROM nation_relations WHERE NUUID1 = ? OR NUUID2 = ?";
        String sqlDeleteSettlementRelations = "DELETE FROM settlement_nation_relations WHERE NUUID = ?";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement psDeleteNation = con.prepareStatement(sqlDeleteNation);
             PreparedStatement psDeleteRelations = con.prepareStatement(sqlDeleteRelations);
             PreparedStatement psDeleteSettlementRelations = con.prepareStatement(sqlDeleteSettlementRelations)) {

            psDeleteNation.setString(1, nationId.toString());
            psDeleteNation.executeUpdate();

            psDeleteRelations.setString(1, nationId.toString());
            psDeleteRelations.setString(2, nationId.toString());
            psDeleteRelations.executeUpdate();

            psDeleteSettlementRelations.setString(1, nationId.toString());
            psDeleteSettlementRelations.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load settlements belonging to a nation
    private static Nation loadNationSettlements(Nation nation) {
        String sql = "SELECT `SUUID`, `rank` FROM settlement_nation_relations WHERE `NUUID` = ?";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nation.getId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID settlementId = UUID.fromString(rs.getString("SUUID"));
                SettlementRank rank = SettlementRank.valueOf(rs.getString("rank"));


                nation.addSettlement(settlementId, rank);
            }
            return nation;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nation;
    }

    // Add a settlement to a nation
    public static void addSettlementToNation(SettlementNationRelation relation) {
        String sql = "INSERT INTO settlement_nation_relations (`SUUID`, `NUUID`, `rank`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `rank` = VALUES(`rank`)";

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
        String sql = "DELETE FROM settlement_nation_relations WHERE `SUUID` = ?";

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
        String sql = "SELECT `NUUID` FROM settlement_nation_relations WHERE `SUUID` = ?";

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

    // Load relations of a nation
    private static Nation loadNationRelations(Nation nation) {
        String sql = "SELECT `NUUID2`, `relation` FROM nation_relations WHERE `NUUID1` = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nation.getId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID otherNationId = UUID.fromString(rs.getString("NUUID2"));
                NationRelationType relation = NationRelationType.valueOf(rs.getString("relation"));
                nation.setRelation(otherNationId, relation);
            }
            return nation;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nation;
    }

    // Save relations of a nation
    private static void saveNationRelations(Nation nation) {
        String sqlDelete = "DELETE FROM nation_relations WHERE `NUUID1` = ?";
        String sqlInsert = "INSERT INTO nation_relations (`NUUID1`, `NUUID2`, `relation`) VALUES (?, ?, ?)";

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

    // Load player ranks of a nation
    private static Nation loadNationPlayerRanks(Nation nation) {
        String sql = "SELECT `PUUID`, `rank` FROM nation_ranks WHERE `NUUID` = ?";
        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nation.getId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("PUUID"));
                NationPlayerRank rank = NationPlayerRank.valueOf(rs.getString("rank"));
                nation.setPlayerRank(playerId, rank);
            }
            return nation;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nation;
    }

    // Add a player rank to a nation
    public static void savePlayerRankToNation(NationPlayerRank rank, UUID playerId, UUID settleId, UUID nationId) {
        String sql = "INSERT INTO nation_ranks (`PUUID`, `SUUID`, `NUUID`, `rank`) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE `rank` = VALUES(`rank`)";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            ps.setString(2, settleId.toString());
            ps.setString(3, nationId.toString());
            ps.setString(4, rank.name());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a player rank from a nation
    public static void removePlayerRankFromNation(UUID playerId) {
        String sql = "DELETE FROM nation_ranks WHERE `PUUID` = ?";

        try (Connection con = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
