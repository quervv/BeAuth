package dev.beffaxone.beauth.hooks;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.auth.AuthPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final BeAuth plugin;

    public PlaceholderAPIHook(BeAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "beauth";
    }

    @Override
    public @NotNull String getAuthor() {
        return "beffaxone";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        AuthPlayer ap = plugin.getAuthManager().getAuthPlayer(player.getUniqueId());
        switch (params.toLowerCase()) {
            case "logged" -> {
                return String.valueOf(ap != null && ap.isLoggedIn());
            }
            case "registered" -> {
                return String.valueOf(ap != null && ap.isRegistered());
            }
            case "premium" -> {
                if (ap == null) return "false";
                var future = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
                if (future.isDone()) {
                    try {
                        var data = future.get();
                        return String.valueOf(data != null && data.isPremium());
                    } catch (Exception ignored) {}
                }
                return "false";
            }
            case "status" -> {
                if (ap != null && ap.isLoggedIn()) {
                    return plugin.getMessagesManager().getRawMessage("gui.login-info.status-logged");
                } else {
                    return plugin.getMessagesManager().getRawMessage("gui.login-info.status-not-logged");
                }
            }
            default -> {
                return null;
            }
        }
    }
}
