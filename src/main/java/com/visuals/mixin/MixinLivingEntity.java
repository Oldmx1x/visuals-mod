package com.visuals.mixin;

import com.visuals.VisualsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SwingAnimation — overrides getHandSwingDuration() so the local player's
 *                  swing is faster or slower according to the config multiplier.
 * OldAnimations  — forces the 1.7 swing duration of 6 ticks (ignoring haste).
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "getHandSwingDuration()I", at = @At("RETURN"), cancellable = true)
    private void visuals$onGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
        // Only affect the local player
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        //noinspection EqualsBetweenInconvertibleTypes
        if ((Object) this != mc.player) return;

        var cfg = VisualsClient.config;

        // OldAnimations takes priority: 1.7 always used 6 ticks
        if (cfg.oldAnimationsEnabled) {
            cir.setReturnValue(6);
            return;
        }

        // Custom swing speed multiplier
        if (cfg.swingAnimationEnabled) {
            int base     = cir.getReturnValue();
            int modified = Math.max(1, Math.round(base / cfg.swingSpeed));
            cir.setReturnValue(modified);
        }
    }
}
