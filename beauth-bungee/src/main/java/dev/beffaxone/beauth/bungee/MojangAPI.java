package dev.beffaxone.beauth.bungee;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class MojangAPI {

    private final BeAuthBungee plugin;
    private final HttpClient httpClient;
    private final Map<String, CacheEntry<PremiumResult>> cache;

    public MojangAPI(BeAuthBungee plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(plugin.getConfigManager().getMojangTimeout()))
                .build();
    }

    private static class CacheEntry<T> {
        private final T value;
        private final long expiresAt;

        public CacheEntry(T value, long durationMs) {
            this.value = value;
            this.expiresAt = System.currentTimeMillis() + durationMs;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }

        public T getValue() {
            return value;
        }
    }

    public static class PremiumResult {
        private final boolean premium;
        private final UUID uuid;
        private final boolean error;

        public PremiumResult(boolean premium, UUID uuid) {
            this(premium, uuid, false);
        }

        public PremiumResult(boolean premium, UUID uuid, boolean error) {
            this.premium = premium;
            this.uuid = uuid;
            this.error = error;
        }

        public boolean isPremium() {
            return premium;
        }

        public UUID getUuid() {
            return uuid;
        }

        public boolean isError() {
            return error;
        }
    }

    public CompletableFuture<PremiumResult> checkPremiumStatus(String username) {
        String cacheKey = username.toLowerCase();
        CacheEntry<PremiumResult> cached = cache.get(cacheKey);
        if (cached != null) {
            if (!cached.isExpired()) {
                return CompletableFuture.completedFuture(cached.getValue());
            }
            cache.remove(cacheKey);
        }

        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(plugin.getConfigManager().getMojangTimeout()))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    PremiumResult result;
                    if (response.statusCode() == 200) {
                        try {
                            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                            String idStr = json.get("id").getAsString();
                            UUID mojangUuid = parseUUIDWithoutDashes(idStr);
                            result = new PremiumResult(true, mojangUuid, false);
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Mojang JSON error: " + username, e);
                            result = new PremiumResult(false, null, true);
                        }
                    } else if (response.statusCode() == 204 || response.statusCode() == 404) {
                        result = new PremiumResult(false, null, false);
                    } else {
                        plugin.getLogger().warning("Mojang API returned status code " + response.statusCode() + " for " + username);
                        result = new PremiumResult(false, null, true);
                    }

                    if (!result.isError()) {
                        long cacheDurationMs = plugin.getConfigManager().getCacheDurationMinutes() * 60 * 1000L;
                        cache.put(cacheKey, new CacheEntry<>(result, cacheDurationMs));
                    }
                    return result;
                }).exceptionally(ex -> {
                    plugin.getLogger().log(Level.WARNING, "Mojang API request failed for " + username + ": " + ex.getMessage());
                    return new PremiumResult(false, null, true);
                });
    }

    private UUID parseUUIDWithoutDashes(String uuidWithoutDashes) {
        if (uuidWithoutDashes.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID length");
        }
        return UUID.fromString(uuidWithoutDashes.substring(0, 8) + "-" +
                uuidWithoutDashes.substring(8, 12) + "-" +
                uuidWithoutDashes.substring(12, 16) + "-" +
                uuidWithoutDashes.substring(16, 20) + "-" +
                uuidWithoutDashes.substring(20, 32));
    }

    public void shutdown() {
        cache.clear();
    }
}
