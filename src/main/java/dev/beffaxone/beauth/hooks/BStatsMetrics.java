package dev.beffaxone.beauth.hooks;

import dev.beffaxone.beauth.BeAuth;
import org.bstats.bukkit.Metrics;

public class BStatsMetrics {

    private final BeAuth plugin;
    private final int pluginId;

    public BStatsMetrics(BeAuth plugin, int pluginId) {
        this.plugin = plugin;
        this.pluginId = pluginId;
        initialize();
    }

    private void initialize() {
        try {
            new Metrics(plugin, pluginId);
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading bStats: " + e.getMessage());
        }
    }
}
