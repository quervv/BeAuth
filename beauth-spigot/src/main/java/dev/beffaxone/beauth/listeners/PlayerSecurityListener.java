package dev.beffaxone.beauth.listeners;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.database.PlayerData;
import dev.beffaxone.beauth.premium.MojangAPI;
import dev.beffaxone.beauth.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.profile.PlayerProfile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerSecurityListener implements Listener {

    private final BeAuth plugin;

    public PlayerSecurityListener(BeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String username = event.getName();
        UUID connectingUuid = event.getUniqueId();
        String ip = event.getAddress().getHostAddress();

        List<String> allowedProxyIps = plugin.getConfigManager().getAllowedProxyIps();
        if (allowedProxyIps != null && !allowedProxyIps.isEmpty()) {
            if (!allowedProxyIps.contains(ip)) {
                if (!ip.equals("127.0.0.1") && !ip.equals("0:0:0:0:0:0:0:1")) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                            ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("security.proxy-only")));
                    plugin.getLogger().log(Level.WARNING, "[BeAuth Security] Connection from " + username + " rejected: IP " + ip + " is not in allowed-proxy-ips.");
                    return;
                }
            }
        }

        boolean isPremiumName = false;
        UUID mojangUuid = null;

        if (plugin.getConfigManager().isPremiumSystemEnabled()) {
            try {
                MojangAPI.PremiumResult premiumResult = plugin.getPremiumManager().getMojangAPI().checkPremiumStatus(username).join();
                if (premiumResult.isError()) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                            ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("premium.api-error")));
                    plugin.getLogger().warning("[BeAuth Security] Rejected join for " + username + ": Mojang API returned an error/rate limit.");
                    return;
                }
                if (premiumResult.isPremium()) {
                    isPremiumName = true;
                    mojangUuid = premiumResult.getUuid();

                    if (!connectingUuid.equals(mojangUuid)) {
                        if (plugin.getConfigManager().isLockPremiumNames() || plugin.getConfigManager().isKickOnSpoof()) {
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                                    ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("premium.uuid-spoof")));
                            plugin.getLogger().warning("[BeAuth Security] Premium spoof blocked: " + username + 
                                    " (connecting UUID: " + connectingUuid + " != Mojang UUID: " + mojangUuid + ")");
                            return;
                        }
                    }
                } else {
                    if (plugin.getConfigManager().isAntiSpoofEnabled()) {
                        UUID expectedOfflineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
                        if (!connectingUuid.equals(expectedOfflineUuid)) {
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                                    ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("security.invalid-uuid")));
                            plugin.getLogger().warning("[BeAuth Security] UUID injection blocked: " + username + 
                                    " (connecting UUID: " + connectingUuid + " != expected offline UUID: " + expectedOfflineUuid + ")");
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error verifying premium status for " + username, e);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                        ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("premium.api-error")));
                return;
            }
        }

        try {
            PlayerData dbData = plugin.getDatabaseManager().getPlayerDataByName(username).join();
            if (dbData != null) {
                if (dbData.isPremium()) {
                    UUID savedPremiumUuid = dbData.getPremiumUuid();
                    if (savedPremiumUuid == null && isPremiumName) {
                        savedPremiumUuid = mojangUuid;
                        dbData.setPremiumUuid(mojangUuid);
                        plugin.getDatabaseManager().savePlayerData(dbData).join();
                    }

                    if (savedPremiumUuid != null && !connectingUuid.equals(savedPremiumUuid)) {
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                                ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("premium.uuid-spoof")));
                        plugin.getLogger().warning("[BeAuth Security] Premium lock blocked join: " + username + 
                                " (connecting UUID: " + connectingUuid + " != saved premium UUID: " + savedPremiumUuid + ")");
                        return;
                    }
                } else {
                    if (!dbData.getUuid().equals(connectingUuid)) {
                        if (isPremiumName && connectingUuid.equals(mojangUuid)) {
                            plugin.getLogger().info("[BeAuth Security] Registered cracked account '" + username + 
                                    "' collision resolved: premium owner is logging in.");
                        } else {
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                                    ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("security.name-collision")));
                            plugin.getLogger().warning("[BeAuth Security] Nickname collision blocked: " + username + 
                                    " (connecting UUID: " + connectingUuid + " != registered UUID: " + dbData.getUuid() + ")");
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error verifying database accounts for " + username, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (plugin.getConfigManager().isBungeeGuardEnabled()) {
            String token = plugin.getConfigManager().getBungeeGuardToken();
            if (token != null && !token.isEmpty()) {
                boolean valid = false;
                Player player = event.getPlayer();
                try {
                    String propValue = getProfileProperty(player.getPlayerProfile(), "bungeeguard-token");
                    if (token.equals(propValue)) {
                        valid = true;
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error checking BungeeGuard token in PlayerLoginEvent for " + player.getName(), e);
                }
                if (!valid) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, 
                            ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("security.bungeeguard-denied")));
                    plugin.getLogger().log(Level.WARNING, "[BeAuth Security] Connection from " + player.getName() + " rejected in PlayerLoginEvent: BungeeGuard token mismatch.");
                }
            }
        }
    }

    private String getProfileProperty(Object profile, String propertyName) {
        if (profile == null) return null;
        try {
            java.lang.reflect.Method getPropertiesMethod = null;
            try {
                getPropertiesMethod = profile.getClass().getMethod("getProperties");
            } catch (NoSuchMethodException e) {
                for (Class<?> iface : profile.getClass().getInterfaces()) {
                    try {
                        getPropertiesMethod = iface.getMethod("getProperties");
                        break;
                    } catch (NoSuchMethodException ignored) {}
                }
            }

            if (getPropertiesMethod != null) {
                java.util.Collection<?> properties = (java.util.Collection<?>) getPropertiesMethod.invoke(profile);
                if (properties != null) {
                    for (Object prop : properties) {
                        java.lang.reflect.Method getNameMethod = prop.getClass().getMethod("getName");
                        java.lang.reflect.Method getValueMethod = prop.getClass().getMethod("getValue");
                        String name = (String) getNameMethod.invoke(prop);
                        if (propertyName.equals(name)) {
                            return (String) getValueMethod.invoke(prop);
                        }
                    }
                }
            }

            Object gameProfile = null;
            try {
                java.lang.reflect.Method getGameProfileMethod = profile.getClass().getDeclaredMethod("getGameProfile");
                getGameProfileMethod.setAccessible(true);
                gameProfile = getGameProfileMethod.invoke(profile);
            } catch (Exception ignored) {
                for (java.lang.reflect.Field field : profile.getClass().getDeclaredFields()) {
                    if (field.getType().getName().endsWith("GameProfile")) {
                        field.setAccessible(true);
                        gameProfile = field.get(profile);
                        break;
                    }
                }
            }

            if (gameProfile != null) {
                java.lang.reflect.Method getPropertiesM = gameProfile.getClass().getMethod("getProperties");
                Object propertyMap = getPropertiesM.invoke(gameProfile);
                if (propertyMap != null) {
                    java.lang.reflect.Method valuesMethod = propertyMap.getClass().getMethod("values");
                    java.util.Collection<?> properties = (java.util.Collection<?>) valuesMethod.invoke(propertyMap);
                    if (properties != null) {
                        for (Object prop : properties) {
                            java.lang.reflect.Method getNameMethod = prop.getClass().getMethod("getName");
                            java.lang.reflect.Method getValueMethod = prop.getClass().getMethod("getValue");
                            String name = (String) getNameMethod.invoke(prop);
                            if (propertyName.equals(name)) {
                                return (String) getValueMethod.invoke(prop);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error extracting profile property via reflection: " + propertyName, e);
        }
        return null;
    }
}
