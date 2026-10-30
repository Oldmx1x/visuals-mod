package com.visuals.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HitColor — after an entity's normal render pass, if it is currently hurt
 * (hurtTime > 0) and HitColor is enabled, we draw a translucent coloured
 * bounding-box overlay instead of the vanilla red flash.
 *
 * The vanilla red flash is handled by the entity shader via OverlayTexture.
 * We do NOT disable it here; instead we just layer our own colour on top.
 * If you want to replace the red flash entirely, you can set hurtTime=0 before
 * the parent render and restore it afterwards — but that can break death
 * animations, so the overlay approach is safer.
 */
@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Inject(
        method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("TAIL")
    )
    private <T extends LivingEntity> void visuals$onRenderTail(
            T entity, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            int light, CallbackInfo ci) {

        if (!VisualsClient.config.hitColorEnabled) return;
        if (entity.hurtTime <= 0 && entity.deathTime <= 0) return;

        var cfg = VisualsClient.config;

        // Build a camera-relative bounding box centred on the entity
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null) return;
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        Box bb = entity.getBoundingBox();
        double dx = entity.getLerpedPos(tickDelta).x - camPos.x;
        double dy = entity.getLerpedPos(tickDelta).y - camPos.y;
        double dz = entity.getLerpedPos(tickDelta).z - camPos.z;
        double hw = (bb.maxX - bb.minX) / 2.0;
        double hh  = bb.maxY - bb.minY;

        // We use a standalone MatrixStack at identity (world render context)
        // because this inject runs inside entity render where the stack is local.
        // Drawing directly here with Tessellator avoids needing the world context.
        MatrixStack ms = new MatrixStack();
        ms.translate(dx, dy, dz);

        float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() * 0.015);
        float alpha = cfg.hitColorAlpha * pulse;

        RenderUtils.drawBox(ms,
                -hw, 0,  -hw,
                 hw, hh,  hw,
                cfg.hitColorR, cfg.hitColorG, cfg.hitColorB, alpha, 1.8f);
    }
}
