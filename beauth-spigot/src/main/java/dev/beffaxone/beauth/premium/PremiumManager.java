package dev.beffaxone.beauth.premium;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.database.PlayerData;
import dev.beffaxone.beauth.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PremiumManager {

    private final BeAuth plugin;
    private final MojangAPI mojangAPI;

    public PremiumManager(BeAuth plugin) {
        this.plugin = plugin;
        this.mojangAPI = new MojangAPI(plugin);
    }

    public MojangAPI getMojangAPI() {
        return mojangAPI;
    }

    public void togglePremium(Player player) {
        if (!plugin.getConfigManager().isPremiumSystemEnabled()) {
            plugin.getMessagesManager().sendMessage(player, "general.no-permission");
            return;
        }
        plugin.getMessagesManager().sendMessage(player, "premium.checking");
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId()).thenAccept(data -> {
            if (data == null) {
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getMessagesManager().sendMessage(player, "login.not-registered"));
                return;
            }
            if (data.isPremium()) {
                data.setPremium(false);
                plugin.getDatabaseManager().savePlayerData(data).thenRun(() -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getAuthManager().getSessionManager().removeSession(player.getUniqueId());
                        plugin.getMessagesManager().sendMessage(player, "premium.disabled");
                        logPremiumToggleDiscord(player, "Cracked (Offline)");
                    });
                });
            } else {
                mojangAPI.checkPremiumStatus(player.getName()).thenAccept(result -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (result.isPremium()) {
                            data.setPremium(true);
                            data.setPremiumUuid(result.getUuid());
                            plugin.getDatabaseManager().savePlayerData(data).thenRun(() -> {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    plugin.getMessagesManager().sendMessage(player, "premium.enabled");
                                    logPremiumToggleDiscord(player, "Premium (Online)");
                                    player.kickPlayer(plugin.getMessagesManager().getRawMessage("premium.enabled"));
                                });
                            });
                        } else {
                            plugin.getMessagesManager().sendMessage(player, "premium.not-premium");
                        }
                    });
                }).exceptionally(ex -> {
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getMessagesManager().sendMessage(player, "premium.api-error"));
                    return null;
                });
            }
        });
    }

    public CompletableFuture<Boolean> setPremiumStatus(UUID uuid, String name, boolean premium) {
        if (premium) {
            return mojangAPI.checkPremiumStatus(name).thenCompose(result -> {
                UUID premiumUuid = result.isPremium() ? result.getUuid() : null;
                return plugin.getDatabaseManager().getPlayerData(uuid).thenCompose(data -> {
                    if (data == null) {
                        PlayerData newData = new PlayerData(uuid, premiumUuid, name, "", true, "", 0L);
                        return plugin.getDatabaseManager().savePlayerData(newData).thenApply(v -> true);
                    } else {
                        data.setPremium(true);
                        data.setPremiumUuid(premiumUuid);
                        return plugin.getDatabaseManager().savePlayerData(data).thenApply(v -> true);
                    }
                });
            });
        } else {
            return plugin.getDatabaseManager().getPlayerData(uuid).thenCompose(data -> {
                if (data == null) {
                    PlayerData newData = new PlayerData(uuid, null, name, "", false, "", 0L);
                    return plugin.getDatabaseManager().savePlayerData(newData).thenApply(v -> true);
                } else {
                    data.setPremium(false);
                    data.setPremiumUuid(null);
                    return plugin.getDatabaseManager().savePlayerData(data).thenApply(v -> true);
                }
            });
        }
    }

    private void logPremiumToggleDiscord(Player player, String status) {
        if (plugin.getConfigManager().isDiscordEnabled() && plugin.getConfigManager().isDiscordAlertEnabled("premium-toggle")) {
            String msg = plugin.getMessagesManager().getRawMessage("discord.premium-toggle-message", "{player}", player.getName(), "{status}", status);
            plugin.getDatabaseManager().getPlayerData(player.getUniqueId()).thenAccept(d -> {
                int color = status.contains("Premium") ? 65280 : 16711680;
                DiscordWebhook.sendEmbed(plugin, msg, color);
            });
        }
    }
}
