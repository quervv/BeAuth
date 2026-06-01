package dev.beffaxone.beauth.listeners;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.utils.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final BeAuth plugin;

    public PlayerJoinListener(BeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getAuthManager().handleJoin(player);
        if (player.hasPermission("beauth.admin") && plugin.getConfigManager().isUpdateCheckerEnabled()) {
            new UpdateChecker(plugin).checkAndUpdate(player);
        }
    }
}
