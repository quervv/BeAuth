package dev.beffaxone.beauth.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.config.AntiVPNConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AntiVPNManager {
    private final BeAuth plugin;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final HttpClient httpClient;

    public AntiVPNManager(BeAuth plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build();
    }

    public void clearCache() {
        cache.clear();
    }

    public void shutdown() {
        cache.clear();
        try {
            httpClient.close();
        } catch (Exception ignored) {}
    }

    public boolean checkVPN(String ipAddress) {
        AntiVPNConfig config = plugin.getAntiVPNConfig();
        if (!config.isEnabled()) {
            return false;
        }
        if (isPrivateOrLocal(ipAddress)) {
            return false;
        }
        for (String whitelistedIp : config.getWhitelistIps()) {
            if (ipAddress.equalsIgnoreCase(whitelistedIp.trim())) {
                return false;
            }
        }
        int cacheDurationHours = config.getCacheDurationHours();
        if (cacheDurationHours > 0) {
            CacheEntry entry = cache.get(ipAddress);
            if (entry != null) {
                long ageMs = System.currentTimeMillis() - entry.timestamp;
                long maxAgeMs = cacheDurationHours * 3600000L;
                if (ageMs < maxAgeMs) {
                    return entry.isVpn;
                }
                cache.remove(ipAddress);
            }
        }
        boolean isVpn = queryAPI(ipAddress);
        if (cacheDurationHours > 0) {
            cache.put(ipAddress, new CacheEntry(isVpn, System.currentTimeMillis()));
        }
        return isVpn;
    }

    public boolean isVpnCached(String ipAddress) {
        CacheEntry entry = cache.get(ipAddress);
        return entry != null && entry.isVpn;
    }

    public boolean isWhitelistedPlayer(String name) {
        if (name == null) {
            return false;
        }
        List<String> whitelistedPlayers = plugin.getAntiVPNConfig().getWhitelistPlayers();
        for (String player : whitelistedPlayers) {
            if (player.trim().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean queryAPI(String ipAddress) {
        AntiVPNConfig config = plugin.getAntiVPNConfig();
        String apiService = config.getApiService();
        String apiKey = config.getApiKey();
        int apiTimeout = config.getApiTimeout();
        return queryProxyCheck(ipAddress, apiKey, apiTimeout);
    }

    private boolean queryProxyCheck(String ipAddress, String apiKey, int timeoutMs) {
        try {
            String url = "https://ip.proxycheck.io/v2/" + ipAddress + "?vpn=1&asn=1";
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                url = url + "&key=" + apiKey.trim();
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", "BeAuth-AntiVPN")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("status")) {
                    String status = json.get("status").getAsString();
                    if ("ok".equalsIgnoreCase(status) || "warning".equalsIgnoreCase(status)) {
                        if (json.has(ipAddress)) {
                            JsonObject ipJson = json.getAsJsonObject(ipAddress);
                            if (ipJson.has("proxy")) {
                                String proxyVal = ipJson.get("proxy").getAsString();
                                boolean isVpn = "yes".equalsIgnoreCase(proxyVal);
                                if (isVpn) {
                                    String provider = ipJson.has("provider") ? ipJson.get("provider").getAsString() : "unknown";
                                    plugin.getLogger().info("[AntiVPN] Flagged IP " + ipAddress + " (Provider: " + provider + ")");
                                }
                                return isVpn;
                            }
                        }
                    } else {
                        String message = json.has("message") ? json.get("message").getAsString() : "None";
                        plugin.getLogger().warning("[AntiVPN] ProxyCheck API returned status: " + status + ". Message: " + message);
                    }
                }
            } else {
                plugin.getLogger().warning("[AntiVPN] Failed to connect to ProxyCheck API. HTTP status code: " + response.statusCode());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AntiVPN] Error checking IP " + ipAddress + " via ProxyCheck", e);
        }
        return false;
    }

    private boolean isPrivateOrLocal(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return true;
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            try {
                String[] parts = ip.split("\\.");
                if (parts.length >= 2) {
                    int secondPart = Integer.parseInt(parts[1]);
                    return secondPart >= 16 && secondPart <= 31;
                }
            } catch (NumberFormatException ignored) {}
        }
        return false;
    }

    private static class CacheEntry {
        final boolean isVpn;
        final long timestamp;

        CacheEntry(boolean isVpn, long timestamp) {
            this.isVpn = isVpn;
            this.timestamp = timestamp;
        }
    }
}