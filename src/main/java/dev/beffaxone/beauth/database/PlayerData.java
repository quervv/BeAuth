package dev.beffaxone.beauth.database;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final String username;
    private String passwordHash;
    private boolean premium;
    private String lastIp;
    private long lastLogin;

    public PlayerData(UUID uuid, String username, String passwordHash, boolean premium, String lastIp, long lastLogin) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.premium = premium;
        this.lastIp = lastIp;
        this.lastLogin = lastLogin;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
}
