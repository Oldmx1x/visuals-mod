package com.visuals.mixin;

import com.visuals.VisualsClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When CustomHUD is enabled we cancel the vanilla health/armour/food renders
 * so they don't overlap our custom bars.
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {

    // Suppress vanilla health hearts
    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void visuals$cancelHealthBar(
            net.minecraft.client.gui.DrawContext context,
            net.minecraft.entity.player.PlayerEntity player,
            int x, int y, int lines,
            float ticks, float maxHealth,
            int lastMaxHealth, int health,
            int absorption, CallbackInfo ci) {

        if (VisualsClient.config.customHUDEnabled
         && VisualsClient.config.showHealthBar) {
            ci.cancel();
        }
    }

    // Suppress vanilla armour bar
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void visuals$cancelArmor(
            net.minecraft.client.gui.DrawContext context,
            net.minecraft.entity.player.PlayerEntity player,
            int y, int lines, int width, int x,
            CallbackInfo ci) {

        if (VisualsClient.config.customHUDEnabled
         && VisualsClient.config.showArmorBar) {
            ci.cancel();
        }
    }

    // Suppress vanilla food bar
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void visuals$cancelFood(
            net.minecraft.client.gui.DrawContext context,
            net.minecraft.entity.player.PlayerEntity player,
            int y, int x, CallbackInfo ci) {

        if (VisualsClient.config.customHUDEnabled
         && VisualsClient.config.showHungerBar) {
            ci.cancel();
        }
    }
}
