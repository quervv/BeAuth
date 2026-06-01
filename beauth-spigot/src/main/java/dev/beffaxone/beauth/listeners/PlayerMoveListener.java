package dev.beffaxone.beauth.listeners;

import dev.beffaxone.beauth.BeAuth;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final BeAuth plugin;

    public PlayerMoveListener(BeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAuthManager().isLoggedIn(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                Location newLoc = from.clone();
                newLoc.setYaw(to.getYaw());
                newLoc.setPitch(to.getPitch());
                event.setTo(newLoc);
                if (player.getNoDamageTicks() % 40 == 0) {
                    plugin.getMessagesManager().sendMessage(player, "security.move-blocked");
                }
            }
        }
    }
}
