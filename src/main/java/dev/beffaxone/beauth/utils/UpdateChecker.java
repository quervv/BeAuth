package dev.beffaxone.beauth.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.beffaxone.beauth.BeAuth;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UpdateChecker implements Runnable {

    private final BeAuth plugin;
    private final HttpClient httpClient;

    public UpdateChecker(BeAuth plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public void run() {
        checkAndUpdate(Bukkit.getConsoleSender());
    }

    public void checkAndUpdate(CommandSender receiver) {
        String repo = plugin.getConfigManager().getUpdateCheckerGithubRepo();
        if (repo == null || repo.isEmpty()) return;
        String url = "https://api.github.com/repos/" + repo + "/releases/latest";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "BeAuth-Plugin-UpdateChecker")
                .GET()
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                            String latestVersion = json.get("tag_name").getAsString().replace("v", "");
                            String currentVersion = plugin.getDescription().getVersion();
                            String htmlUrl = json.get("html_url").getAsString();
                            if (isNewerVersion(latestVersion, currentVersion)) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    receiver.sendMessage(plugin.getMessagesManager().getMessage("update.available",
                                            "{version}", latestVersion,
                                            "{url}", htmlUrl
                                    ));
                                });
                            } else {
                                if (receiver == Bukkit.getConsoleSender()) {
                                    plugin.getLogger().info("BeAuth is up to date.");
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error parsing update data.");
                        }
                    }
                }).exceptionally(ex -> null);
    }

    private boolean isNewerVersion(String latest, String current) {
        String[] latestSplit = latest.split("\\.");
        String[] currentSplit = current.split("\\.");
        int length = Math.max(latestSplit.length, currentSplit.length);
        for (int i = 0; i < length; i++) {
            int latestPart = i < latestSplit.length ? Integer.parseInt(latestSplit[i].replaceAll("[^0-9]", "")) : 0;
            int currentPart = i < currentSplit.length ? Integer.parseInt(currentSplit[i].replaceAll("[^0-9]", "")) : 0;
            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }
        return false;
    }
}
