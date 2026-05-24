package dev.beffaxone.beauth.auth;

import dev.beffaxone.beauth.BeAuth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final BeAuth plugin;
    private final Map<UUID, UserSession> sessions;

    public SessionManager(BeAuth plugin) {
        this.plugin = plugin;
        this.sessions = new ConcurrentHashMap<>();
    }

    private static class UserSession {
        private final String ipAddress;
        private final long creationTime;

        public UserSession(String ipAddress) {
            this.ipAddress = ipAddress;
            this.creationTime = System.currentTimeMillis();
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public long getCreationTime() {
            return creationTime;
        }
    }

    public void createSession(UUID uuid, String ip) {
        if (!plugin.getConfigManager().isAutoLoginIPEnabled()) {
            return;
        }
        sessions.put(uuid, new UserSession(ip));
    }

    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public boolean hasValidSession(UUID uuid, String currentIp) {
        if (!plugin.getConfigManager().isAutoLoginIPEnabled()) {
            return false;
        }
        UserSession session = sessions.get(uuid);
        if (session == null) {
            return false;
        }
        if (!session.getIpAddress().equalsIgnoreCase(currentIp)) {
            return false;
        }
        long durationMs = (long) plugin.getConfigManager().getSessionDurationHours() * 60 * 60 * 1000;
        long elapsed = System.currentTimeMillis() - session.getCreationTime();
        return elapsed < durationMs;
    }

    public void clear() {
        sessions.clear();
    }
}
