package com.elarion.mixin;

import com.elarion.util.NameStorage;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerCommandSource.class)
public abstract class CommandSuggestionsMixin {

    @Inject(method = "getPlayerNames", at = @At("RETURN"), cancellable = true)
    private void onGetPlayerNames(CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        CompletableFuture<Suggestions> originalFuture = cir.getReturnValue();

        cir.setReturnValue(originalFuture.thenApply(originalSuggestions -> {
            ServerCommandSource source = (ServerCommandSource) (Object) this;
            SuggestionsBuilder builder = new SuggestionsBuilder(
                    "", // or actual input if you captured it
                    originalSuggestions.getRange().getStart()
            );


            // Process each suggestion and replace with custom names if available
            originalSuggestions.getList().forEach(suggestion -> {
                String playerName = suggestion.getText();

                // Find the player and check for custom name
                ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
                if (player != null && NameStorage.hasCustomName(player.getUuid())) {
                    String customName = NameStorage.getCustomName(player.getUuid());
                    builder.suggest(customName, suggestion.getTooltip());
                } else {
                    builder.suggest(playerName, suggestion.getTooltip());
                }
            });

            return builder.build();
        }));
    }
}