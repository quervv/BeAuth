package dev.beffaxone.beauth.listeners;

import dev.beffaxone.beauth.BeAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class PlayerCommandListener implements Listener {

    private final BeAuth plugin;

    public PlayerCommandListener(BeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAuthManager().isLoggedIn(player.getUniqueId())) {
            String message = event.getMessage();
            String[] parts = message.split(" ");
            if (parts.length == 0) return;
            String cmd = parts[0].substring(1).toLowerCase();
            if (cmd.contains(":")) {
                cmd = cmd.substring(cmd.indexOf(":") + 1);
            }
            List<String> allowed = plugin.getConfigManager().getAllowedCommands();
            if (allowed == null || allowed.isEmpty()) {
                allowed = List.of("login", "register", "l", "reg", "accedi", "registra");
            }
            if (!allowed.contains(cmd)) {
                event.setCancelled(true);
                plugin.getMessagesManager().sendMessage(player, "security.command-blocked");
            }
        }
    }
}
