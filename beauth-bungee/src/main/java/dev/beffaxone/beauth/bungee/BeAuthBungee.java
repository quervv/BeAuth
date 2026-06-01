package dev.beffaxone.beauth.bungee;

import net.md_5.bungee.api.plugin.Plugin;

public class BeAuthBungee extends Plugin {

    private ConfigManager configManager;
    private MojangAPI mojangAPI;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.mojangAPI = new MojangAPI(this);

        getProxy().getPluginManager().registerListener(this, new PreLoginListener(this));

        getLogger().info("BeAuth BungeeCord companion plugin enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (mojangAPI != null) {
            mojangAPI.shutdown();
        }
        getLogger().info("BeAuth BungeeCord companion plugin disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MojangAPI getMojangAPI() {
        return mojangAPI;
    }
}
