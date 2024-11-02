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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HikariCP {

    //static Dotenv secret;

    private final NationsPlugin plugin;
    private final String user;
    private final String password;
    public HikariDataSource dataSource;

    public HikariCP(NationsPlugin plugin) throws SQLException {


        this.plugin = plugin;
        //secret = Dotenv.configure().directory().filename(".env").load();

        //user = secret.get("USERNAME");
        user = "minecraft";
        //System.out.println(user);
        //password = secret.get("PASSWORD");
        password = "minecraft";
        //System.out.println(password);

        HikariConfig config = getHikariConfig();
        dataSource = new HikariDataSource(config);
        prepareTables();
    }

    private @NotNull HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost/nations");
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(20);
        config.setMaxLifetime(1800000);
        config.setKeepaliveTime(0);
        config.setConnectionTimeout(5000);
        config.setLeakDetectionThreshold(100000);
        config.setPoolName("NationsHikariPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        return config;
    }

    private void prepareTables() {
        try (Connection connection = dataSource.getConnection()) {

            // Read the SQL script from the resource file
            String sqlScript;
            try {
                sqlScript = new String(Objects.requireNonNull(plugin.getResource("database/mysql_schema.sql")).readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read SQL schema file.", e);
            }

            // Remove comments and split into individual statements
            List<String> sqlStatements = parseSqlStatements(sqlScript);

            try (Statement statement = connection.createStatement()) {
                for (String sql : sqlStatements) {
                    sql = sql.trim();
                    if (sql.isEmpty()) {
                        continue; // Skip empty statements
                    }
                    statement.execute(sql);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ and that your connecting user account has privileges to create tables.", e);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. Please check the supplied database credentials.", e);
        }
    }

    private List<String> parseSqlStatements(String sqlScript) {
        List<String> statements = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        boolean inComment = false;
        boolean inLineComment = false;
        boolean inString = false;
        char stringChar = ' ';

        for (int i = 0; i < sqlScript.length(); i++) {
            char c = sqlScript.charAt(i);

            // Handle multi-line comments /* */
            if (inComment) {
                if (c == '*' && i + 1 < sqlScript.length() && sqlScript.charAt(i + 1) == '/') {
                    inComment = false;
                    i++; // Skip '/'
                }
                continue;
            }

            // Handle single-line comments --
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }

            // Start of multi-line comment
            if (c == '/' && i + 1 < sqlScript.length() && sqlScript.charAt(i + 1) == '*') {
                inComment = true;
                i++; // Skip '*'
                continue;
            }

            // Start of single-line comment
            if (c == '-' && i + 1 < sqlScript.length() && sqlScript.charAt(i + 1) == '-') {
                inLineComment = true;
                i++; // Skip second '-'
                continue;
            }

            // Start or end of string literal
            if (c == '\'' || c == '"') {
                if (inString && c == stringChar) {
                    inString = false;
                } else if (!inString) {
                    inString = true;
                    stringChar = c;
                }
            }

            // Check for statement delimiter ';' if not inside a string
            if (c == ';' && !inString) {
                statements.add(sb.toString());
                sb.setLength(0); // Reset StringBuilder
                continue;
            }

            // Append character to current statement
            sb.append(c);
        }

        // Add any remaining statement
        if (sb.length() > 0) {
            statements.add(sb.toString());
        }

        return statements;
    }

    public void closeConnection() throws SQLException {
        dataSource.close();
    }

}
