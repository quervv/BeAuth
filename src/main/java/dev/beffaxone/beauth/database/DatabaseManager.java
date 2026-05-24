package dev.beffaxone.beauth.database;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DatabaseManager {

    boolean initialize();

    CompletableFuture<PlayerData> getPlayerData(UUID uuid);

    CompletableFuture<PlayerData> getPlayerDataByName(String username);

    CompletableFuture<Void> savePlayerData(PlayerData data);

    CompletableFuture<Void> deletePlayerData(UUID uuid);

    void close();
}
