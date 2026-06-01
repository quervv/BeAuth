package dev.beffaxone.beauth.bungee;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {

    private final BeAuthBungee plugin;
    private Configuration configuration;

    public ConfigManager(BeAuthBungee plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save default config.yml: " + e.getMessage());
            }
        }

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load config.yml: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return configuration.getBoolean("enabled", true);
    }

    public int getMojangTimeout() {
        return configuration.getInt("mojang-timeout", 5000);
    }

    public int getCacheDurationMinutes() {
        return configuration.getInt("cache-duration-minutes", 30);
    }

    public boolean isKickOnApiError() {
        return configuration.getBoolean("kick-on-api-error", true);
    }

    public String getApiErrorMessage() {
        return configuration.getString("messages.api-error", "&cErrore nella verifica dello stato premium. Riprova più tardi.");
    }
}
