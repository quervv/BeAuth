package dev.beffaxone.beauth.premium;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.beffaxone.beauth.BeAuth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MojangAPI {

    private final BeAuth plugin;
    private final HttpClient httpClient;

    public MojangAPI(BeAuth plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(plugin.getConfigManager().getPremiumApiTimeout()))
                .build();
    }

    public static class PremiumResult {
        private final boolean premium;
        private final UUID uuid;

        public PremiumResult(boolean premium, UUID uuid) {
            this.premium = premium;
            this.uuid = uuid;
        }

        public boolean isPremium() {
            return premium;
        }

        public UUID getUuid() {
            return uuid;
        }
    }

    public CompletableFuture<PremiumResult> checkPremiumStatus(String username) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                            String idStr = json.get("id").getAsString();
                            UUID mojangUuid = parseUUIDWithoutDashes(idStr);
                            return new PremiumResult(true, mojangUuid);
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Mojang JSON error: " + username, e);
                        }
                    }
                    return new PremiumResult(false, null);
                }).exceptionally(ex -> new PremiumResult(false, null));
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
}
