package dev.beffaxone.beauth.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class AuthPlayer {

    private final UUID uuid;
    private final String username;
    private boolean registered;
    private boolean loggedIn;
    private UUID sessionToken;
    private final long joinTime;
    private int loginAttempts;
    private BukkitTask timeoutTask;
    private BukkitTask actionbarTask;

    public AuthPlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.registered = false;
        this.loggedIn = false;
        this.sessionToken = UUID.randomUUID();
        this.joinTime = System.currentTimeMillis();
        this.loginAttempts = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public UUID getSessionToken() {
        return sessionToken;
    }

    public void generateNewSessionToken() {
        this.sessionToken = UUID.randomUUID();
    }

    public long getJoinTime() {
        return joinTime;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }

    public BukkitTask getTimeoutTask() {
        return timeoutTask;
    }

    public void setTimeoutTask(BukkitTask timeoutTask) {
        if (this.timeoutTask != null) {
            this.timeoutTask.cancel();
        }
        this.timeoutTask = timeoutTask;
    }

    public BukkitTask getActionbarTask() {
        return actionbarTask;
    }

    public void setActionbarTask(BukkitTask actionbarTask) {
        if (this.actionbarTask != null) {
            this.actionbarTask.cancel();
        }
        this.actionbarTask = actionbarTask;
    }

    public void cleanup() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
        if (actionbarTask != null) {
            actionbarTask.cancel();
            actionbarTask = null;
        }
    }
}
