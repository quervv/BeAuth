package dev.beffaxone.beauth.auth;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.database.PlayerData;
import dev.beffaxone.beauth.hooks.LuckPermsHook;
import dev.beffaxone.beauth.utils.ColorUtils;
import dev.beffaxone.beauth.utils.DiscordWebhook;
import dev.beffaxone.beauth.premium.MojangAPI;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AuthManager {

    private final BeAuth plugin;
    private final Map<UUID, AuthPlayer> onlinePlayers;
    private final PasswordManager passwordManager;
    private final SessionManager sessionManager;

    public AuthManager(BeAuth plugin) {
        this.plugin = plugin;
        this.onlinePlayers = new ConcurrentHashMap<>();
        this.passwordManager = new PasswordManager(plugin);
        this.sessionManager = new SessionManager(plugin);
    }

    public Map<UUID, AuthPlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public AuthPlayer getAuthPlayer(UUID uuid) {
        return onlinePlayers.get(uuid);
    }

    public boolean isLoggedIn(UUID uuid) {
        AuthPlayer ap = onlinePlayers.get(uuid);
        return ap != null && ap.isLoggedIn();
    }

    public void handleJoin(Player player) {
        AuthPlayer ap = new AuthPlayer(player.getUniqueId(), player.getName());
        onlinePlayers.put(player.getUniqueId(), ap);
        String ip = player.getAddress().getAddress().getHostAddress();
        if (plugin.getConfigManager().isLuckPermsEnabled()) {
            LuckPermsHook.setUnauthenticated(player);
        }
        
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId()).thenCompose(data -> {
            if (data == null) {
                return plugin.getDatabaseManager().getPlayerDataByName(player.getName()).thenCompose(dataByName -> {
                    if (dataByName != null) {
                        return CompletableFuture.completedFuture(dataByName);
                    }
                    if (plugin.getConfigManager().isPremiumSystemEnabled() && plugin.getConfigManager().isPremiumAutoDetect()) {
                        return plugin.getPremiumManager().getMojangAPI().checkPremiumStatus(player.getName()).thenApply(premiumResult -> {
                            if (premiumResult.isPremium() && player.getUniqueId().equals(premiumResult.getUuid())) {
                                PlayerData newData = new PlayerData(player.getUniqueId(), player.getUniqueId(), player.getName(), "", true, ip, System.currentTimeMillis());
                                plugin.getDatabaseManager().savePlayerData(newData);
                                return newData;
                            }
                            return null;
                        });
                    }
                    return CompletableFuture.completedFuture(null);
                });
            } else {
                return CompletableFuture.completedFuture(data);
            }
        }).thenAccept(data -> {
            if (data != null && data.isPremium() && data.getPremiumUuid() != null 
                    && data.getPremiumUuid().equals(player.getUniqueId()) && !data.getUuid().equals(player.getUniqueId())) {
                UUID oldUuid = data.getUuid();
                PlayerData migratedData = new PlayerData(
                        player.getUniqueId(),
                        player.getUniqueId(),
                        data.getUsername(),
                        data.getPasswordHash(),
                        true,
                        ip,
                        System.currentTimeMillis()
                );
                plugin.getDatabaseManager().deletePlayerData(oldUuid).thenRun(() -> {
                    plugin.getDatabaseManager().savePlayerData(migratedData);
                });
                data = migratedData;
            }
            
            final PlayerData finalData = data;
            Bukkit.getScheduler().runTask(plugin, () -> processLoginState(player, ap, finalData, ip));
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error loading player data " + player.getName(), ex);
            Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(ColorUtils.colorize("&cSession loading error.")));
            return null;
        });
    }

    private void processLoginState(Player player, AuthPlayer ap, PlayerData data, String ip) {
        if (data != null) {
            ap.setRegistered(true);
            if (data.isPremium() && plugin.getConfigManager().isPremiumSystemEnabled()) {
                UUID expectedPremiumUuid = data.getPremiumUuid();
                if (expectedPremiumUuid == null) {
                    try {
                        MojangAPI.PremiumResult premiumResult = plugin.getPremiumManager().getMojangAPI().checkPremiumStatus(player.getName()).join();
                        if (premiumResult.isPremium()) {
                            expectedPremiumUuid = premiumResult.getUuid();
                            data.setPremiumUuid(expectedPremiumUuid);
                            plugin.getDatabaseManager().savePlayerData(data);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to resolve premium UUID for " + player.getName() + " on join", e);
                    }
                }
                if (expectedPremiumUuid == null || !player.getUniqueId().equals(expectedPremiumUuid)) {
                    player.kickPlayer(ColorUtils.colorize(plugin.getMessagesManager().getRawMessage("premium.uuid-spoof")));
                    plugin.getLogger().warning("[BeAuth Security] Blocked premium login bypass for " + player.getName() + " (UUID mismatch or not premium).");
                    return;
                }
                if (plugin.getConfigManager().isPremiumAutoLoginEnabled()) {
                    authenticate(player, ap, data, "premium-auto-login");
                    return;
                }
            }
            if (sessionManager.hasValidSession(player.getUniqueId(), ip)) {
                authenticate(player, ap, data, "auto-login");
                return;
            }
            startAuthTasks(player, ap, false);
        } else {
            ap.setRegistered(false);
            startAuthTasks(player, ap, true);
        }
    }

    private void startAuthTasks(Player player, AuthPlayer ap, boolean register) {
        if (register) {
            plugin.getMessagesManager().sendMessage(player, "register.prompt");
            if (plugin.getConfigManager().showJoinTitle()) {
                player.sendTitle(
                        plugin.getMessagesManager().getRawMessage("register.title"),
                        plugin.getMessagesManager().getRawMessage("register.subtitle"),
                        10, 70, 20
                );
            }
        } else {
            plugin.getMessagesManager().sendMessage(player, "login.prompt");
            if (plugin.getConfigManager().showJoinTitle()) {
                player.sendTitle(
                        plugin.getMessagesManager().getRawMessage("login.title"),
                        plugin.getMessagesManager().getRawMessage("login.subtitle"),
                        10, 70, 20
                );
            }
        }
        if (plugin.getConfigManager().showActionbar()) {
            var task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || ap.isLoggedIn()) {
                        cancel();
                        return;
                    }
                    String path = register ? "register.prompt" : "login.actionbar";
                    String msg = plugin.getMessagesManager().getRawMessage(path);
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg));
                }
            }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getActionbarUpdateInterval());
            ap.setActionbarTask(task);
        }
        var timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && !ap.isLoggedIn()) {
                    player.kickPlayer(plugin.getMessagesManager().getRawMessage("login.timeout-kick"));
                }
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getLoginTimeout() * 20L);
        ap.setTimeoutTask(timeoutTask);
    }

    public void login(Player player, String password) {
        AuthPlayer ap = onlinePlayers.get(player.getUniqueId());
        if (ap == null) return;
        if (!ap.isRegistered()) {
            plugin.getMessagesManager().sendMessage(player, "register.prompt");
            return;
        }
        if (ap.isLoggedIn()) {
            plugin.getMessagesManager().sendMessage(player, "login.already-logged-in");
            return;
        }
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId()).thenAccept(data -> {
            if (data == null) {
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getMessagesManager().sendMessage(player, "login.not-registered"));
                return;
            }
            boolean matches = passwordManager.verifyPassword(password, data.getPasswordHash());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (matches) {
                    authenticate(player, ap, data, "success");
                } else {
                    ap.incrementLoginAttempts();
                    int max = plugin.getConfigManager().getMaxLoginAttempts();
                    if (ap.getLoginAttempts() >= max) {
                        player.kickPlayer(plugin.getMessagesManager().getRawMessage("login.brute-force-ban", "{minutes}", String.valueOf(plugin.getConfigManager().getBruteForceBanDuration() / 60)));
                        logFailedLoginDiscord(player, ap.getLoginAttempts());
                    } else {
                        plugin.getMessagesManager().sendMessage(player, "login.wrong-password", "{attempts}", String.valueOf(ap.getLoginAttempts()), "{max-attempts}", String.valueOf(max));
                        playAuthSound(player, "login-fail");
                        logFailedLoginDiscord(player, ap.getLoginAttempts());
                    }
                }
            });
        });
    }

    public void register(Player player, String password, String confirmPassword) {
        AuthPlayer ap = onlinePlayers.get(player.getUniqueId());
        if (ap == null) return;
        if (ap.isRegistered()) {
            plugin.getMessagesManager().sendMessage(player, "register.already-registered");
            return;
        }
        if (!password.equals(confirmPassword)) {
            plugin.getMessagesManager().sendMessage(player, "register.password-mismatch");
            playAuthSound(player, "login-fail");
            return;
        }
        int minLen = plugin.getConfigManager().getMinPasswordLength();
        int maxLen = plugin.getConfigManager().getMaxPasswordLength();
        if (password.length() < minLen) {
            plugin.getMessagesManager().sendMessage(player, "register.password-too-short", "{min}", String.valueOf(minLen));
            playAuthSound(player, "login-fail");
            return;
        }
        if (password.length() > maxLen) {
            plugin.getMessagesManager().sendMessage(player, "register.password-too-long", "{max}", String.valueOf(maxLen));
            playAuthSound(player, "login-fail");
            return;
        }
        if (plugin.getConfigManager().getForbiddenPasswords().contains(password.toLowerCase())) {
            plugin.getMessagesManager().sendMessage(player, "register.password-forbidden");
            playAuthSound(player, "login-fail");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String hash = passwordManager.hashPassword(password);
            String ip = player.getAddress().getAddress().getHostAddress();
            PlayerData data = new PlayerData(player.getUniqueId(), player.getName(), hash, false, ip, System.currentTimeMillis());
            plugin.getDatabaseManager().savePlayerData(data).thenRun(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ap.setRegistered(true);
                    authenticate(player, ap, data, "register-success");
                });
            });
        });
    }

    public void changePassword(Player player, String oldPassword, String newPassword) {
        AuthPlayer ap = onlinePlayers.get(player.getUniqueId());
        if (ap == null || !ap.isLoggedIn()) {
            plugin.getMessagesManager().sendMessage(player, "change-password.not-logged-in");
            return;
        }
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId()).thenAccept(data -> {
            if (data == null) return;
            boolean matches = passwordManager.verifyPassword(oldPassword, data.getPasswordHash());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!matches) {
                    plugin.getMessagesManager().sendMessage(player, "change-password.wrong-old-password");
                    playAuthSound(player, "login-fail");
                    return;
                }
                int minLen = plugin.getConfigManager().getMinPasswordLength();
                if (newPassword.length() < minLen) {
                    plugin.getMessagesManager().sendMessage(player, "register.password-too-short", "{min}", String.valueOf(minLen));
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    String hash = passwordManager.hashPassword(newPassword);
                    data.setPasswordHash(hash);
                    plugin.getDatabaseManager().savePlayerData(data).thenRun(() -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getMessagesManager().sendMessage(player, "change-password.success");
                            playAuthSound(player, "register-success");
                        });
                    });
                });
            });
        });
    }

    private void authenticate(Player player, AuthPlayer ap, PlayerData data, String messageType) {
        ap.setLoggedIn(true);
        ap.cleanup();
        String ip = player.getAddress().getAddress().getHostAddress();
        sessionManager.createSession(player.getUniqueId(), ip);
        data.setLastIp(ip);
        data.setLastLogin(System.currentTimeMillis());
        plugin.getDatabaseManager().savePlayerData(data);
        if (plugin.getConfigManager().isLuckPermsEnabled()) {
            LuckPermsHook.setAuthenticated(player);
        }
        if ("premium-auto-login".equals(messageType)) {
            plugin.getMessagesManager().sendMessage(player, "login.premium-auto-login", "{player}", player.getName());
            playAuthSound(player, "login-success");
        } else if ("auto-login".equals(messageType)) {
            plugin.getMessagesManager().sendMessage(player, "login.auto-login", "{player}", player.getName());
            playAuthSound(player, "login-success");
        } else if ("register-success".equals(messageType)) {
            plugin.getMessagesManager().sendMessage(player, "register.success", "{player}", player.getName(), "{server}", Bukkit.getServer().getName());
            playAuthSound(player, "register-success");
            logRegistrationDiscord(player);
        } else {
            plugin.getMessagesManager().sendMessage(player, "login.success", "{player}", player.getName());
            playAuthSound(player, "login-success");
            logLoginDiscord(player);
        }
        spawnSuccessParticles(player);
    }

    public void handleQuit(Player player) {
        AuthPlayer ap = onlinePlayers.remove(player.getUniqueId());
        if (ap != null) {
            ap.cleanup();
        }
        sessionManager.removeSession(player.getUniqueId());
    }

    public void shutdown() {
        for (AuthPlayer ap : onlinePlayers.values()) {
            ap.cleanup();
        }
        onlinePlayers.clear();
        sessionManager.clear();
    }

    private void playAuthSound(Player player, String configPath) {
        if (!plugin.getConfigManager().isSoundEnabled(configPath)) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundName(configPath).toUpperCase());
            float vol = plugin.getConfigManager().getSoundVolume(configPath);
            float pitch = plugin.getConfigManager().getSoundPitch(configPath);
            player.playSound(player.getLocation(), sound, vol, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + configPath);
        }
    }

    private void spawnSuccessParticles(Player player) {
        if (!plugin.getConfigManager().isParticlesEnabled()) {
            return;
        }
        try {
            Particle particle = Particle.valueOf(plugin.getConfigManager().getParticleType().toUpperCase());
            int count = plugin.getConfigManager().getParticleCount();
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), count, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type.");
        }
    }

    private void logLoginDiscord(Player player) {
        if (plugin.getConfigManager().isDiscordEnabled() && plugin.getConfigManager().isDiscordAlertEnabled("login")) {
            String ip = player.getAddress().getAddress().getHostAddress();
            String msg = plugin.getMessagesManager().getRawMessage("discord.login-message", "{player}", player.getName(), "{ip}", ip);
            DiscordWebhook.sendEmbed(plugin, msg, plugin.getConfigManager().getDiscordEmbedColor());
        }
    }

    private void logRegistrationDiscord(Player player) {
        if (plugin.getConfigManager().isDiscordEnabled() && plugin.getConfigManager().isDiscordAlertEnabled("register")) {
            String ip = player.getAddress().getAddress().getHostAddress();
            String msg = plugin.getMessagesManager().getRawMessage("discord.register-message", "{player}", player.getName(), "{ip}", ip);
            DiscordWebhook.sendEmbed(plugin, msg, plugin.getConfigManager().getDiscordEmbedColor());
        }
    }

    private void logFailedLoginDiscord(Player player, int attempts) {
        if (plugin.getConfigManager().isDiscordEnabled() && plugin.getConfigManager().isDiscordAlertEnabled("failed-login")) {
            String msg = plugin.getMessagesManager().getRawMessage("discord.failed-login-message", "{player}", player.getName(), "{attempts}", String.valueOf(attempts));
            DiscordWebhook.sendEmbed(plugin, msg, 16711680);
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
