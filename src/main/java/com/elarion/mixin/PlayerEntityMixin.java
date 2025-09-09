package com.elarion.mixin;

import com.elarion.util.NameStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        UUID uuid = player.getUuid();

        if (NameStorage.hasCustomName(uuid)) {
            String customName = NameStorage.getCustomName(uuid);
            MutableText text = Text.literal(customName);

            // Apply team formatting if player is in a team
            Team team = player.getScoreboardTeam();
            if (team != null) {
                text.setStyle(Style.EMPTY.withColor(team.getColor()));
            }

            cir.setReturnValue(text);
        }
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void onGetName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        UUID uuid = player.getUuid();

        if (NameStorage.hasCustomName(uuid)) {
            String customName = NameStorage.getCustomName(uuid);
            MutableText text = Text.literal(customName);

            // Apply team formatting if player is in a team
            Team team = player.getScoreboardTeam();
            if (team != null) {
                text.setStyle(Style.EMPTY.withColor(team.getColor()));
            }

            cir.setReturnValue(text);
        }
    }
}