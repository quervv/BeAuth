package dev.beffaxone.beauth.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PreLoginListener {

    private final BeAuthVelocity plugin;

    public PreLoginListener(BeAuthVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public EventTask onPreLogin(PreLoginEvent event) {
        if (!plugin.getConfigManager().isEnabled()) {
            return null;
        }

        String username = event.getUsername();
        return EventTask.resumeWhenComplete(
                plugin.getMojangAPI().checkPremiumStatus(username).thenAccept(result -> {
                    if (result.isError()) {
                        if (plugin.getConfigManager().isKickOnApiError()) {
                            Component reason = LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getConfigManager().getApiErrorMessage());
                            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
                        }
                    } else if (result.isPremium()) {
                        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                    } else {
                        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
                    }
                }).exceptionally(ex -> {
                    plugin.getLogger().error("Error in Velocity PreLogin verification", ex);
                    if (plugin.getConfigManager().isKickOnApiError()) {
                        Component reason = LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getConfigManager().getApiErrorMessage());
                        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
                    }
                    return null;
                })
        );
    }
}
