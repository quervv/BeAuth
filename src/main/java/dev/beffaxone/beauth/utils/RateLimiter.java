package dev.beffaxone.beauth.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    private final int maxAttempts;
    private final int windowSeconds;
    private final Map<String, List<Long>> ipAttempts;

    public RateLimiter(int maxAttempts, int windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
        this.ipAttempts = new ConcurrentHashMap<>();
    }

    public synchronized boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        long limitTime = now - ((long) windowSeconds * 1000);
        List<Long> attempts = ipAttempts.computeIfAbsent(ip, k -> new ArrayList<>());
        attempts.removeIf(timestamp -> timestamp < limitTime);
        if (attempts.size() >= maxAttempts) {
            return true;
        }
        attempts.add(now);
        return false;
    }

    public synchronized long getRemainingTimeSeconds(String ip) {
        List<Long> attempts = ipAttempts.get(ip);
        if (attempts == null || attempts.isEmpty()) {
            return 0;
        }
        long now = System.currentTimeMillis();
        long oldestInWindow = attempts.get(0);
        long diff = oldestInWindow + ((long) windowSeconds * 1000) - now;
        return Math.max(0, diff / 1000);
    }

    public synchronized void reset(String ip) {
        ipAttempts.remove(ip);
    }
}
