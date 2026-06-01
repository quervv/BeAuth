package dev.beffaxone.beauth.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.logging.Level;

public class PreLoginListener implements Listener {

    private final BeAuthBungee plugin;

    public PreLoginListener(BeAuthBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }

        event.registerIntent(plugin);
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                String username = event.getConnection().getName();
                plugin.getMojangAPI().checkPremiumStatus(username).thenAccept(result -> {
                    if (result.isError()) {
                        if (plugin.getConfigManager().isKickOnApiError()) {
                            event.setCancelReason(TextComponent.fromLegacyText(
                                    ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getApiErrorMessage())
                            ));
                            event.setCancelled(true);
                        }
                    } else if (result.isPremium()) {
                        event.getConnection().setOnlineMode(true);
                    } else {
                        event.getConnection().setOnlineMode(false);
                    }
                }).join();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in Bungee PreLogin verification", e);
                if (plugin.getConfigManager().isKickOnApiError()) {
                    event.setCancelReason(TextComponent.fromLegacyText(
                            ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getApiErrorMessage())
                    ));
                    event.setCancelled(true);
                }
            } finally {
                event.completeIntent(plugin);
            }
        });
    }
}
