package com.elarion.util;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameStorage {
    private static final Path CONFIG_PATH = Path.of("config", "name_mod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<UUID, String> customNames = new HashMap<>();
    private static MinecraftServer server;

    public static void setServer(MinecraftServer server) {
        NameStorage.server = server;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                Reader reader = Files.newBufferedReader(CONFIG_PATH);
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();

                if (json.has("customNames")) {
                    JsonObject namesObject = json.getAsJsonObject("customNames");
                    customNames.clear();

                    for (Map.Entry<String, JsonElement> entry : namesObject.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(entry.getKey());
                            String name = entry.getValue().getAsString();
                            customNames.put(uuid, name);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid UUID in name config: " + entry.getKey());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load name mod config: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            JsonObject json = new JsonObject();
            JsonObject namesObject = new JsonObject();

            for (Map.Entry<UUID, String> entry : customNames.entrySet()) {
                namesObject.addProperty(entry.getKey().toString(), entry.getValue());
            }

            json.add("customNames", namesObject);

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            System.err.println("Failed to save name mod config: " + e.getMessage());
        }
    }

    public static void setCustomName(UUID playerUuid, String name) {
        customNames.put(playerUuid, name);
        save();
        refreshPlayer(playerUuid);
    }

    public static void removeCustomName(UUID playerUuid) {
        customNames.remove(playerUuid);
        save();
        refreshPlayer(playerUuid);
    }

    public static String getCustomName(UUID playerUuid) {
        return customNames.get(playerUuid);
    }

    public static boolean hasCustomName(UUID playerUuid) {
        return customNames.containsKey(playerUuid);
    }

    private static void refreshPlayer(UUID playerUuid) {
        if (server != null) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
            if (player != null) {
                // Refresh command suggestions - this ensures command autocomplete works
                server.getPlayerManager().sendCommandTree(player);
            }
        }
    }
}