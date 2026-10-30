package com.visuals.mixin;

import com.visuals.VisualsClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ViewModel   — applies custom translation & scale to the first-person hand.
 * OldAnimations — removes the sprint-arm-centre offset that was added in 1.8+.
 */
@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

    /**
     * Called at the very start of renderFirstPersonItem so we can pre-transform
     * the MatrixStack before Minecraft does its own hand positioning.
     */
    @Inject(
        method = "renderFirstPersonItem",
        at = @At("HEAD")
    )
    private void visuals$beforeRenderFirstPersonItem(
            AbstractClientPlayerEntity player,
            float tickDelta, float pitch, Hand hand,
            float swingProgress, ItemStack item, float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light, CallbackInfo ci) {

        var cfg = VisualsClient.config;

        // ── ViewModel offset ──────────────────────────────────────────────
        if (cfg.viewModelEnabled) {
            matrices.translate(cfg.viewModelX, cfg.viewModelY, cfg.viewModelZ);
            matrices.scale(cfg.viewModelScaleX, cfg.viewModelScaleY, cfg.viewModelScaleZ);
        }

        // ── OldAnimations: cancel the 1.8 sprint-centre arm tilt ─────────
        // In 1.7 the arm did NOT swing toward the centre of the screen while
        // sprinting. We counter this by applying an inverse translate when
        // the player is sprinting and OldAnimations is enabled.
        if (cfg.oldAnimationsEnabled && player.isSprinting()) {
            // The vanilla sprint offset is roughly (-0.01, 0.0, 0.0) for the
            // main hand before further transforms; we nudge it back.
            matrices.translate(0.01f, 0.0f, 0.0f);
        }
    }
}
