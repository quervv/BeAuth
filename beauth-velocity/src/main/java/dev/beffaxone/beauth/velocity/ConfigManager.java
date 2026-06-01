package dev.beffaxone.beauth.velocity;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigManager {

    private final BeAuthVelocity plugin;
    private final Path dataDirectory;
    private Map<String, Object> configMap;

    public ConfigManager(BeAuthVelocity plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
    }

    @SuppressWarnings("unchecked")
    public void loadConfig() {
        File dataDir = dataDirectory.toFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File file = new File(dataDir, "config.yml");
        if (!file.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().error("Could not save default config.yml: " + e.getMessage());
            }
        }

        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(file.toPath())) {
            configMap = yaml.load(in);
        } catch (IOException e) {
            plugin.getLogger().error("Could not load config.yml: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return getBoolean("enabled", true);
    }

    public int getMojangTimeout() {
        return getInt("mojang-timeout", 5000);
    }

    public int getCacheDurationMinutes() {
        return getInt("cache-duration-minutes", 30);
    }

    public boolean isKickOnApiError() {
        return getBoolean("kick-on-api-error", true);
    }

    @SuppressWarnings("unchecked")
    public String getApiErrorMessage() {
        if (configMap != null) {
            Object messagesObj = configMap.get("messages");
            if (messagesObj instanceof Map) {
                Map<String, Object> messages = (Map<String, Object>) messagesObj;
                Object apiError = messages.get("api-error");
                if (apiError != null) {
                    return apiError.toString();
                }
            }
        }
        return "&cErrore nella verifica dello stato premium. Riprova più tardi.";
    }

    private boolean getBoolean(String key, boolean def) {
        if (configMap != null && configMap.containsKey(key)) {
            Object obj = configMap.get(key);
            if (obj instanceof Boolean) {
                return (Boolean) obj;
            }
        }
        return def;
    }

    private int getInt(String key, int def) {
        if (configMap != null && configMap.containsKey(key)) {
            Object obj = configMap.get(key);
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
        }
        return def;
    }
}
