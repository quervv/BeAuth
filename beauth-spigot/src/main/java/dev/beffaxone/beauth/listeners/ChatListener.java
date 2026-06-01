package dev.beffaxone.beauth.listeners;

import dev.beffaxone.beauth.BeAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final BeAuth plugin;

    public ChatListener(BeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAuthManager().isLoggedIn(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getMessagesManager().sendMessage(player, "security.chat-blocked");
        }
    }
}
