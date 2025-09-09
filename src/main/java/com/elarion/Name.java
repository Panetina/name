package com.elarion;

import com.elarion.command.CNameCommand;
import com.elarion.util.NameStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class Name implements ModInitializer {
    public static final String MOD_ID = "name";

    @Override
    public void onInitialize() {
        // Initialize storage
        NameStorage.load();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CNameCommand.register(dispatcher);
        });

        // Set server reference and save data when server stops
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            NameStorage.setServer(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            NameStorage.save();
        });
    }
}