package de.terranova.nations.database;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.settlement;
import de.terranova.nations.worldguard.Vectore2;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class SettleDBstuff {

        public static void getInitialSettlementData() throws SQLException {
                Connection con = NationsPlugin.hikari.dataSource.getConnection();
                Statement statement = con.createStatement();
                statement.execute("SELECT * FROM `settlements_table`");
                ResultSet rs = statement.getResultSet();
                HashMap<UUID, settlement> settlements = new HashMap<>();
                while (rs.next()) {
                        UUID SUUID = UUID.fromString(rs.getString("SUUID"));
                        String name = rs.getString("name");
                        String location = rs.getString("location");
                        int level = rs.getInt("level");
                        int obj_a = rs.getInt("obj_a");
                        int obj_b = rs.getInt("obj_b");
                        int obj_c = rs.getInt("obj_c");
                        int obj_d = rs.getInt("obj_d");
                        settlements.put(SUUID,new settlement(SUUID, getMembersAcess(SUUID.toString()), new Vectore2(location), name,level));
                }
                NationsPlugin.settlementManager.setSettlements(settlements);
        }

        public static HashMap<UUID, AccessLevelEnum> getMembersAcess(String SUUID) throws SQLException {
                HashMap<UUID, AccessLevelEnum> access = new HashMap<>();
                Connection con = NationsPlugin.hikari.dataSource.getConnection();
                Statement statement = con.createStatement();
                statement.execute(String.format("SELECT * FROM `access_table` WHERE SUUID LIKE 'test'", SUUID));
                ResultSet rs = statement.getResultSet();
                while (rs.next()) {
                        access.put(UUID.fromString(rs.getString("PUUID")),AccessLevelEnum.valueOf(rs.getString("AccessLevel")));
                }
            return access;
        }

        public static void addSettlement(UUID SUUID, String name, Vectore2 location, UUID owner) throws SQLException {
                Connection con = NationsPlugin.hikari.dataSource.getConnection();
                Statement statement = con.createStatement();
                statement.execute("INSERT INTO `settlements_table`" +
                        "(`SUUID`, `Name`, `Location`)" +
                        String.format("VALUES ('%s','%s','%s')", SUUID.toString(), name, location.asString()));
                statement.execute("INSERT INTO `access_table`" +
                        "(`SUUID`, `PUUID`, `ACCESS`) " +
                        String.format("VALUES ('%s','%s','50')", SUUID.toString(), owner.toString()));
        }
}
