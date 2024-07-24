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
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class HikariCP {

  private final NationsPlugin plugin;
  private String user = "";
  private String password = "";
  public HikariDataSource dataSource;


  public HikariCP(NationsPlugin plugin) throws SQLException {
    this.plugin = plugin;
    this.password = System.getenv("PASSWORD");
    this.user = System.getenv("USERNAME");
    HikariConfig config = getHikariConfig();
    dataSource = new HikariDataSource(config);
    final Properties properties = getHikariConfigProperties();
    dataSource.setDataSourceProperties(properties);
    prepareTables();
  }

  private @NotNull Properties getHikariConfigProperties() {
    Properties properties = new Properties();
    properties.putAll(Map.of("cachePrepStmts", "true", "prepStmtCacheSize", "250", "prepStmtCacheSqlLimit", "2048", "useServerPrepStmts", "true", "useLocalSessionState", "true", "useLocalTransactionState", "true"));
    properties.putAll(Map.of("rewriteBatchedStatements", "true", "cacheResultSetMetadata", "true", "cacheServerConfiguration", "true", "elideSetAutoCommits", "true", "maintainTimeStats", "false"));
    return properties;
  }

  private @NotNull HikariConfig getHikariConfig() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://localhost/nations");
    config.setUsername(user);
    config.setPassword(password);
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(10);
    config.setMaxLifetime(1800000);
    config.setKeepaliveTime(0);
    config.setConnectionTimeout(5000);
    config.setPoolName("NationsHikariPool");
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    return config;
  }

  private void prepareTables() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      final String[] databaseSchema = new String(Objects.requireNonNull(plugin.getResource("database/%s_schema.sql")).readAllBytes(), StandardCharsets.UTF_8).split(";");
      try (Statement statement = connection.createStatement()) {
        for (String tableCreationStatement : databaseSchema) {
          statement.execute(tableCreationStatement);
        }
      } catch (SQLException e) {
        throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " + "and that your connecting user account has privileges to create tables.", e);
      }
    } catch (SQLException | IOException e) {
      throw new IllegalStateException("Failed to establish a connection to the MySQL database. " + "Please check the supplied database credentials in the config file", e);
    }
  }

  public void closeConnection() throws SQLException {
    dataSource.close();
  }

}
