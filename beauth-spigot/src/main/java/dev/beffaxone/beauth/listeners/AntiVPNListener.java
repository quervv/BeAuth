package dev.beffaxone.beauth.listeners;

import dev.beffaxone.beauth.BeAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class AntiVPNListener implements Listener {

    private final BeAuth plugin;

    public AntiVPNListener(BeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!plugin.getAntiVPNConfig().isEnabled()) {
            return;
        }

        String ip = event.getAddress().getHostAddress();
        String name = event.getName();

        if (plugin.getAntiVPNManager().isWhitelistedPlayer(name)) {
            return;
        }

        if (!plugin.getAntiVPNConfig().isPermissionBypassEnabled()) {
            boolean isVpn = plugin.getAntiVPNManager().checkVPN(ip);
            if (isVpn) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, plugin.getAntiVPNConfig().getKickMessage(ip));
            }
        } else {
            plugin.getAntiVPNManager().checkVPN(ip);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(PlayerLoginEvent event) {
        if (!plugin.getAntiVPNConfig().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String ip = event.getAddress().getHostAddress();
        String name = player.getName();

        if (plugin.getAntiVPNManager().isWhitelistedPlayer(name)) {
            return;
        }

        if (plugin.getAntiVPNConfig().isPermissionBypassEnabled() && player.hasPermission("beauth.antivpn.bypass")) {
            return;
        }

        if (plugin.getAntiVPNManager().isVpnCached(ip)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, plugin.getAntiVPNConfig().getKickMessage(ip));
        }
    }
}
