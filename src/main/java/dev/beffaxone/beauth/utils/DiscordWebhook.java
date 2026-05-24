package dev.beffaxone.beauth.utils;

import dev.beffaxone.beauth.BeAuth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class DiscordWebhook {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private DiscordWebhook() {}

    public static void sendEmbed(BeAuth plugin, String description, int color) {
        String url = plugin.getConfigManager().getDiscordWebhookUrl();
        if (url == null || url.isEmpty() || url.startsWith("https://discord.com/api/webhooks/YOUR_WEBHOOK_URL")) {
            return;
        }
        String cleanDescription = stripColorCodes(description);
        String json = "{"
                + "\"embeds\": [{"
                + "\"description\": \"" + escapeJson(cleanDescription) + "\","
                + "\"color\": " + color
                + "}]"
                + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        plugin.getLogger().warning("Discord webhook alert error: HTTP " + response.statusCode());
                    }
                }).exceptionally(ex -> {
                    plugin.getLogger().warning("Discord webhook request error: " + ex.getMessage());
                    return null;
                });
    }

    private static String stripColorCodes(String input) {
        if (input == null) return "";
        String cleaned = input.replaceAll("[&§][0-9a-fk-orA-FK-OR]", "");
        cleaned = cleaned.replaceAll("[&§]#([A-Fa-f0-9]{6})", "");
        return cleaned;
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
