package dev.beffaxone.beauth.config;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.utils.ColorUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class AntiVPNConfig {

    private final BeAuth plugin;
    private YamlConfiguration config;
    private File file;

    public AntiVPNConfig(BeAuth plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.file = new File(plugin.getDataFolder(), "antivpn.yml");
        if (!file.exists()) {
            plugin.saveResource("antivpn.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public String getKickMessage(String ip) {
        String msg = config.getString("kick-message", "&cANTIVPN >> non puoi entrare con questo ip %ip%");
        msg = msg.replace("%ip%", ip);
        return ColorUtils.colorize(msg);
    }

    public String getApiService() {
        return config.getString("api.service", "proxycheck");
    }

    public String getApiKey() {
        return config.getString("api.key", "");
    }

    public int getApiTimeout() {
        return config.getInt("api.timeout", 3000);
    }

    public int getCacheDurationHours() {
        return config.getInt("cache.duration-hours", 24);
    }

    public List<String> getWhitelistPlayers() {
        return config.getStringList("whitelist.players");
    }

    public List<String> getWhitelistIps() {
        return config.getStringList("whitelist.ips");
    }

    public boolean isPermissionBypassEnabled() {
        return config.getBoolean("permission-bypass", true);
    }
}
