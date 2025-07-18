package de.terranova.nations.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.terranova.nations.NationsPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class HikariCP {

    private final NationsPlugin plugin;
    private final String user;
    private final String password;
    public HikariDataSource dataSource;

    public HikariCP(NationsPlugin plugin) throws SQLException {
        this.plugin = plugin;
        user = "minecraft";
        password = "minecraft";
        HikariConfig config = getHikariConfig();
        dataSource = new HikariDataSource(config);
        prepareTables();
    }

    private @NotNull HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost/nations");
        config.setUsername(user);
        config.setPassword(password);
        config.setPoolName("NationsHikariPool");

        // Connection Pool Size
        config.setMaximumPoolSize(6);
        config.setMinimumIdle(2);

        // Connection lifetime and idle cleanup
        config.setMaxLifetime(900_000);
        config.setIdleTimeout(300_000);
        config.setKeepaliveTime(0);

        // fail timeout / Leak detection
        config.setValidationTimeout(3000);
        config.setConnectionTimeout(5000);
        config.setLeakDetectionThreshold(100000);

        // Statement Caching
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // performance
        config.addDataSourceProperty("useServerPrepStmts", "false");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "false");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // JDBC driver properties
        config.addDataSourceProperty("useCompression", "true");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("noAccessToProcedureBodies", "true");
        return config;
    }

    private void prepareTables() {
        try (Connection connection = dataSource.getConnection()) {
            final String[] databaseSchema = new String(Objects.requireNonNull(plugin.getResource("database/mysql_schema.sql")).readAllBytes(), StandardCharsets.UTF_8).split("--");
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : databaseSchema) {
                    statement.execute(tableCreationStatement);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed SQL: " + e.getMessage() + ". Ensure syntax correctness.", e);
            }

        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database.", e);
        }

    }

    public void closeConnection() throws SQLException {
        dataSource.close();
    }

}
