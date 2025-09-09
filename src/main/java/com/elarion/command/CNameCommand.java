package com.elarion.command;

import com.elarion.util.NameStorage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class CNameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("cname")
                .requires(source -> source.hasPermissionLevel(4)) // Admin only (level 4 = ops)
                .then(argument("player", EntityArgumentType.player())
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(CNameCommand::setName))));

        dispatcher.register(literal("cname")
                .requires(source -> source.hasPermissionLevel(4))
                .then(argument("player", EntityArgumentType.player())
                        .then(literal("reset")
                                .executes(CNameCommand::resetName))));
    }

    private static int setName(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            String customName = StringArgumentType.getString(context, "name");

            // Store the custom name
            NameStorage.setCustomName(player.getUuid(), customName);

            // Refresh player data for all clients
            refreshPlayerData(player);

            context.getSource().sendFeedback(() ->
                            Text.literal("Set custom name for " + player.getGameProfile().getName() + " to " + customName),
                    true);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int resetName(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

            // Remove custom name
            NameStorage.removeCustomName(player.getUuid());

            // Refresh player data for all clients
            refreshPlayerData(player);

            context.getSource().sendFeedback(() ->
                            Text.literal("Reset custom name for " + player.getGameProfile().getName()),
                    true);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static void refreshPlayerData(ServerPlayerEntity player) {
        // Refresh command suggestions - this is the most important part
        player.getServer().getPlayerManager().sendCommandTree(player);

        // For 1.21.1, the mixins will handle the display changes automatically
        // The tab list updates naturally when the player's display name changes
    }
}