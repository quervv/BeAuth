package dev.beffaxone.beauth.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "beauth-velocity",
        name = "BeAuth-Velocity",
        version = "1.0.0",
        description = "Companion Velocity plugin for BeAuth",
        authors = {"beffaxone"}
)
public class BeAuthVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigManager configManager;
    private MojangAPI mojangAPI;

    @Inject
    public BeAuthVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.configManager = new ConfigManager(this, dataDirectory);
        this.configManager.loadConfig();

        this.mojangAPI = new MojangAPI(this);

        server.getEventManager().register(this, new PreLoginListener(this));

        logger.info("BeAuth Velocity companion plugin enabled successfully.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (mojangAPI != null) {
            mojangAPI.shutdown();
        }
        logger.info("BeAuth Velocity companion plugin disabled.");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MojangAPI getMojangAPI() {
        return mojangAPI;
    }
}
