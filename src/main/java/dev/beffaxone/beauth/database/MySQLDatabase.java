package dev.beffaxone.beauth.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.beffaxone.beauth.BeAuth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MySQLDatabase implements DatabaseManager {

    private final BeAuth plugin;
    private HikariDataSource dataSource;

    public MySQLDatabase(BeAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean initialize() {
        try {
            var cm = plugin.getConfigManager();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + cm.getMySQLHost() + ":" + cm.getMySQLPort() + "/" + cm.getMySQLDatabase() + "?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername(cm.getMySQLUsername());
            config.setPassword(cm.getMySQLPassword());
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(cm.getMySQLMaximumPoolSize());
            config.setMinimumIdle(cm.getMySQLMinimumIdle());
            config.setConnectionTimeout(cm.getMySQLConnectionTimeout());
            config.setIdleTimeout(cm.getMySQLIdleTimeout());
            config.setMaxLifetime(cm.getMySQLMaxLifetime());
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            this.dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "CREATE TABLE IF NOT EXISTS beauth_players (" +
                                 "uuid VARCHAR(36) NOT NULL, " +
                                 "username VARCHAR(16) NOT NULL, " +
                                 "password_hash VARCHAR(255) NOT NULL, " +
                                 "premium TINYINT(1) NOT NULL DEFAULT 0, " +
                                 "last_ip VARCHAR(45), " +
                                 "last_login BIGINT, " +
                                 "PRIMARY KEY (uuid), " +
                                 "UNIQUE KEY idx_username (username)" +
                                 ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")) {
                ps.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL init error", e);
            return false;
        }
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM beauth_players WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new PlayerData(
                                UUID.fromString(rs.getString("uuid")),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getBoolean("premium"),
                                rs.getString("last_ip"),
                                rs.getLong("last_login")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL load error " + uuid, e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerDataByName(String username) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM beauth_players WHERE LOWER(username) = LOWER(?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new PlayerData(
                                UUID.fromString(rs.getString("uuid")),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getBoolean("premium"),
                                rs.getString("last_ip"),
                                rs.getLong("last_login")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL load error " + username, e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO beauth_players (uuid, username, password_hash, premium, last_ip, last_login) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "username = VALUES(username), " +
                    "password_hash = VALUES(password_hash), " +
                    "premium = VALUES(premium), " +
                    "last_ip = VALUES(last_ip), " +
                    "last_login = VALUES(last_login)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, data.getUuid().toString());
                ps.setString(2, data.getUsername());
                ps.setString(3, data.getPasswordHash());
                ps.setBoolean(4, data.isPremium());
                ps.setString(5, data.getLastIp());
                ps.setLong(6, data.getLastLogin());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL save error " + data.getUuid(), e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deletePlayerData(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM beauth_players WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL delete error " + uuid, e);
            }
        });
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
