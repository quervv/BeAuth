package dev.beffaxone.beauth;

import dev.beffaxone.beauth.auth.AuthManager;
import dev.beffaxone.beauth.commands.*;
import dev.beffaxone.beauth.config.AntiVPNConfig;
import dev.beffaxone.beauth.config.ConfigManager;
import dev.beffaxone.beauth.config.MessagesManager;
import dev.beffaxone.beauth.database.DatabaseManager;
import dev.beffaxone.beauth.database.MySQLDatabase;
import dev.beffaxone.beauth.database.SQLiteDatabase;
import dev.beffaxone.beauth.hooks.BStatsMetrics;
import dev.beffaxone.beauth.hooks.LuckPermsHook;
import dev.beffaxone.beauth.hooks.PlaceholderAPIHook;
import dev.beffaxone.beauth.listeners.*;
import dev.beffaxone.beauth.premium.PremiumManager;
import dev.beffaxone.beauth.utils.AntiVPNManager;
import dev.beffaxone.beauth.utils.DiscordWebhook;
import dev.beffaxone.beauth.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BeAuth extends JavaPlugin {

    private static BeAuth instance;
    private ConfigManager configManager;
    private AntiVPNConfig antiVPNConfig;
    private MessagesManager messagesManager;
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private PremiumManager premiumManager;
    private AntiVPNManager antiVPNManager;

    @Override
    public void onEnable() {
        instance = this;
        printBanner();
        if (!loadConfiguration()) {
            getLogger().severe("Error loading config. Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!initDatabase()) {
            getLogger().severe("Error loading database. Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        authManager = new AuthManager(this);
        premiumManager = new PremiumManager(this);
        antiVPNManager = new AntiVPNManager(this);
        registerListeners();
        registerCommands();
        loadIntegrations();
        startUpdateChecker();
        getLogger().info("BeAuth enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (authManager != null) {
            authManager.shutdown();
        }
        if (premiumManager != null && premiumManager.getMojangAPI() != null) {
            premiumManager.getMojangAPI().shutdown();
        }
        if (antiVPNManager != null) {
            antiVPNManager.shutdown();
        }
        DiscordWebhook.shutdown();
        UpdateChecker.shutdown();
        if (configManager != null && configManager.isLuckPermsEnabled() &&
                Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            LuckPermsHook.shutdown();
        }
        Bukkit.getScheduler().cancelTasks(this);
        instance = null;
        getLogger().info("BeAuth disabled!");
    }

    private boolean loadConfiguration() {
        try {
            saveDefaultConfig();
            saveResourceIfNotExists("messages.yml");
            saveResourceIfNotExists("antivpn.yml");
            configManager = new ConfigManager(this);
            antiVPNConfig = new AntiVPNConfig(this);
            messagesManager = new MessagesManager(this);
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading configuration", e);
            return false;
        }
    }

    private boolean initDatabase() {
        try {
            String dbType = configManager.getDatabaseType();
            if ("mysql".equalsIgnoreCase(dbType)) {
                databaseManager = new MySQLDatabase(this);
            } else {
                databaseManager = new SQLiteDatabase(this);
            }
            return databaseManager.initialize();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error initializing database", e);
            return false;
        }
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerSecurityListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new AntiVPNListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new PlayerDamageListener(this), this);
        pm.registerEvents(new PlayerCommandListener(this), this);
    }

    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(this));
        getCommand("premium").setExecutor(new PremiumCommand(this));
        getCommand("beauth").setExecutor(new BeAuthAdminCommand(this));
        getCommand("beauth").setTabCompleter(new BeAuthAdminCommand(this));
    }

    private void loadIntegrations() {
        if (configManager.isPlaceholderAPIEnabled() &&
                Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }
        if (configManager.isLuckPermsEnabled() &&
                Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            LuckPermsHook.initialize(this);
        }
        if (configManager.isBStatsEnabled()) {
            new BStatsMetrics(this, configManager.getBStatsPluginId());
        }
    }

    private void startUpdateChecker() {
        if (configManager.isUpdateCheckerEnabled()) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, new UpdateChecker(this), 100L);
        }
    }

    public void saveResourceIfNotExists(String resourcePath) {
        var file = new java.io.File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    private void printBanner() {
        getLogger().info("§b╔══════════════════════════════════╗");
        getLogger().info("§b║     §fBe§bAuth §fv" + getDescription().getVersion() + "               §b║");
        getLogger().info("§b║   §7Authentication Plugin        §b║");
        getLogger().info("§b║   §7Author: §fbeffaxone           §b║");
        getLogger().info("§b╚══════════════════════════════════╝");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        if (antiVPNConfig != null) {
            antiVPNConfig.reload();
        }
        if (antiVPNManager != null) {
            antiVPNManager.clearCache();
        }
        messagesManager.reload();
    }

    public static BeAuth getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public PremiumManager getPremiumManager() {
        return premiumManager;
    }

    public AntiVPNConfig getAntiVPNConfig() {
        return antiVPNConfig;
    }

    public AntiVPNManager getAntiVPNManager() {
        return antiVPNManager;
    }
}
