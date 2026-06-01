package dev.beffaxone.beauth.database;

import dev.beffaxone.beauth.BeAuth;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class SQLiteDatabase implements DatabaseManager {

    private final BeAuth plugin;
    private final File dbFile;
    private Connection connection;
    private ExecutorService dbExecutor;

    public SQLiteDatabase(BeAuth plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getSQLiteFile());
        this.dbExecutor = Executors.newSingleThreadExecutor();
    }

    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite Driver not found", e);
            }
        }
        return connection;
    }

    @Override
    public boolean initialize() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS beauth_players (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "premium_uuid VARCHAR(36), " +
                             "username VARCHAR(16) NOT NULL UNIQUE, " +
                             "password_hash VARCHAR(255) NOT NULL, " +
                             "premium BOOLEAN NOT NULL DEFAULT 0, " +
                             "last_ip VARCHAR(45), " +
                             "last_login BIGINT" +
                             ");")) {
                ps.executeUpdate();
            }
            migrateDatabase(conn);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite create table error", e);
            return false;
        }
    }

    private void migrateDatabase(Connection conn) {
        try {
            boolean hasPremiumUuid = false;
            try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info(beauth_players);");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("premium_uuid".equalsIgnoreCase(rs.getString("name"))) {
                        hasPremiumUuid = true;
                        break;
                    }
                }
            }
            if (!hasPremiumUuid) {
                try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE beauth_players ADD COLUMN premium_uuid VARCHAR(36);")) {
                    ps.executeUpdate();
                    plugin.getLogger().info("Database migration successful: added premium_uuid column to SQLite.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite migration error", e);
        }
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM beauth_players WHERE uuid = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String premiumUuidStr = rs.getString("premium_uuid");
                        UUID premiumUuid = premiumUuidStr != null && !premiumUuidStr.isEmpty() ? UUID.fromString(premiumUuidStr) : null;
                        return new PlayerData(
                                UUID.fromString(rs.getString("uuid")),
                                premiumUuid,
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getBoolean("premium"),
                                rs.getString("last_ip"),
                                rs.getLong("last_login")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite load error " + uuid, e);
            }
            return null;
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerDataByName(String username) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM beauth_players WHERE LOWER(username) = LOWER(?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String premiumUuidStr = rs.getString("premium_uuid");
                        UUID premiumUuid = premiumUuidStr != null && !premiumUuidStr.isEmpty() ? UUID.fromString(premiumUuidStr) : null;
                        return new PlayerData(
                                UUID.fromString(rs.getString("uuid")),
                                premiumUuid,
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getBoolean("premium"),
                                rs.getString("last_ip"),
                                rs.getLong("last_login")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite load error " + username, e);
            }
            return null;
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT OR REPLACE INTO beauth_players (uuid, premium_uuid, username, password_hash, premium, last_ip, last_login) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, data.getUuid().toString());
                ps.setString(2, data.getPremiumUuid() != null ? data.getPremiumUuid().toString() : null);
                ps.setString(3, data.getUsername());
                ps.setString(4, data.getPasswordHash());
                ps.setBoolean(5, data.isPremium());
                ps.setString(6, data.getLastIp());
                ps.setLong(7, data.getLastLogin());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite save error " + data.getUuid(), e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Void> deletePlayerData(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM beauth_players WHERE uuid = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite delete error " + uuid, e);
            }
        }, dbExecutor);
    }

    @Override
    public void close() {
        dbExecutor.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite close error", e);
        }
    }
}
