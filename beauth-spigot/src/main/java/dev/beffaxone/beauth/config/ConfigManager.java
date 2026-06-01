package dev.beffaxone.beauth.config;

import dev.beffaxone.beauth.BeAuth;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final BeAuth plugin;
    private FileConfiguration config;

    public ConfigManager(BeAuth plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.config = plugin.getConfig();
    }

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getSQLiteFile() {
        return config.getString("database.sqlite.file", "beauth.db");
    }

    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "beauth");
    }

    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "");
    }

    public int getMySQLMaximumPoolSize() {
        return config.getInt("database.mysql.pool.maximum-pool-size", 10);
    }

    public int getMySQLMinimumIdle() {
        return config.getInt("database.mysql.pool.minimum-idle", 2);
    }

    public long getMySQLConnectionTimeout() {
        return config.getLong("database.mysql.pool.connection-timeout", 30000);
    }

    public long getMySQLIdleTimeout() {
        return config.getLong("database.mysql.pool.idle-timeout", 600000);
    }

    public long getMySQLMaxLifetime() {
        return config.getLong("database.mysql.pool.max-lifetime", 1800000);
    }

    public int getLoginTimeout() {
        return config.getInt("auth.login-timeout", 60);
    }

    public int getMaxLoginAttempts() {
        return config.getInt("auth.max-login-attempts", 5);
    }

    public int getBruteForceBanDuration() {
        return config.getInt("auth.brute-force-ban-duration", 300);
    }

    public int getMinPasswordLength() {
        return config.getInt("auth.min-password-length", 6);
    }

    public int getMaxPasswordLength() {
        return config.getInt("auth.max-password-length", 64);
    }

    public boolean isAutoLoginIPEnabled() {
        return config.getBoolean("auth.auto-login-ip", true);
    }

    public int getSessionDurationHours() {
        return config.getInt("auth.session-duration", 24);
    }

    public List<String> getForbiddenPasswords() {
        return config.getStringList("auth.forbidden-passwords");
    }

    public int getBCryptRounds() {
        return config.getInt("auth.bcrypt-rounds", 12);
    }

    public boolean isPremiumSystemEnabled() {
        return config.getBoolean("premium.enabled", true);
    }

    public boolean isPremiumAutoDetect() {
        return config.getBoolean("premium.auto-detect", true);
    }

    public boolean isPremiumAutoLoginEnabled() {
        return config.getBoolean("premium.auto-login", true);
    }

    public boolean isLockPremiumNames() {
        return config.getBoolean("premium.lock-premium-names", true);
    }

    public int getPremiumMojangTimeout() {
        return config.getInt("premium.mojang-timeout", 5000);
    }

    public boolean isKickOnSpoof() {
        return config.getBoolean("premium.kick-on-spoof", true);
    }

    public int getPremiumApiTimeout() {
        return config.getInt("premium.api-timeout", 5000);
    }

    public boolean isPremiumUuidVerificationEnabled() {
        return config.getBoolean("premium.uuid-verification", true);
    }

    public boolean showJoinTitle() {
        return config.getBoolean("ui.show-join-title", true);
    }

    public boolean showActionbar() {
        return config.getBoolean("ui.show-actionbar", true);
    }

    public int getActionbarUpdateInterval() {
        return config.getInt("ui.actionbar-update-interval", 40);
    }

    public boolean isSoundEnabled(String type) {
        return config.getBoolean("ui.sounds." + type + ".enabled", true);
    }

    public String getSoundName(String type) {
        return config.getString("ui.sounds." + type + ".sound", "ENTITY_PLAYER_LEVELUP");
    }

    public float getSoundVolume(String type) {
        return (float) config.getDouble("ui.sounds." + type + ".volume", 1.0);
    }

    public float getSoundPitch(String type) {
        return (float) config.getDouble("ui.sounds." + type + ".pitch", 1.0);
    }

    public boolean isParticlesEnabled() {
        return config.getBoolean("ui.particles.enabled", false);
    }

    public String getParticleType() {
        return config.getString("ui.particles.type", "TOTEM_OF_UNDYING");
    }

    public int getParticleCount() {
        return config.getInt("ui.particles.count", 30);
    }

    public boolean isGuiEnabled() {
        return config.getBoolean("gui.enabled", true);
    }

    public boolean isAntiSpoofEnabled() {
        return config.getBoolean("security.anti-spoof", true);
    }

    public boolean isBungeeGuardEnabled() {
        return config.getBoolean("security.bungeeguard.enabled", false);
    }

    public String getBungeeGuardToken() {
        return config.getString("security.bungeeguard.token", "");
    }

    public List<String> getAllowedProxyIps() {
        return config.getStringList("security.allowed-proxy-ips");
    }

    public boolean isRateLimitEnabled() {
        return config.getBoolean("security.rate-limit", true);
    }

    public int getRateLimitMaxAttempts() {
        return config.getInt("security.rate-limit-detail.max-attempts", 5);
    }

    public int getRateLimitWindowSeconds() {
        return config.getInt("security.rate-limit-detail.window-seconds", 60);
    }

    public boolean isBlockedCommandsEnabled() {
        return config.getBoolean("security.blocked-commands.enabled", true);
    }

    public List<String> getAllowedCommands() {
        return config.getStringList("security.blocked-commands.allowed-commands");
    }

    public boolean isPacketProtectionEnabled() {
        return config.getBoolean("security.packet-protection", true);
    }

    public boolean isSessionTokensEnabled() {
        return config.getBoolean("security.session-tokens", true);
    }

    public boolean isPlaceholderAPIEnabled() {
        return config.getBoolean("integrations.placeholderapi.enabled", true);
    }

    public boolean isLuckPermsEnabled() {
        return config.getBoolean("integrations.luckperms.enabled", true);
    }

    public String getLuckPermsUnauthGroup() {
        return config.getString("integrations.luckperms.unauth-group", "unauth");
    }

    public boolean isBStatsEnabled() {
        return config.getBoolean("integrations.bstats.enabled", true);
    }

    public int getBStatsPluginId() {
        return config.getInt("integrations.bstats.plugin-id", 12345);
    }

    public boolean isDiscordEnabled() {
        return config.getBoolean("integrations.discord.enabled", false);
    }

    public String getDiscordWebhookUrl() {
        return config.getString("integrations.discord.webhook-url", "");
    }

    public boolean isDiscordAlertEnabled(String type) {
        return config.getBoolean("integrations.discord.alert-on." + type, true);
    }

    public int getDiscordEmbedColor() {
        return config.getInt("integrations.discord.embed-color", 5814783);
    }

    public boolean isUpdateCheckerEnabled() {
        return config.getBoolean("integrations.update-checker.enabled", true);
    }

    public String getUpdateCheckerGithubRepo() {
        return config.getString("integrations.update-checker.github-repo", "beffaxone/BeAuth");
    }

    public boolean logFailedLogins() {
        return config.getBoolean("logging.log-failed-logins", true);
    }

    public boolean logSuccessfulLogins() {
        return config.getBoolean("logging.log-successful-logins", false);
    }

    public boolean logRegistrations() {
        return config.getBoolean("logging.log-registrations", true);
    }
}
