package com.visuals.feature;

import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Renders a glowing Chinese straw-hat cone above the local player's head.
 */
public class ChinaHatFeature extends AbstractFeature {

    @Override
    public void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!VisualsClient.config.chinaHatEnabled) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            var cfg = VisualsClient.config;
            Vec3d cam = context.camera().getPos();
            var ms  = context.matrixStack();

            // Position: just above the player's head
            Vec3d pp = mc.player.getLerpedPos(context.tickDelta());
            double headY = pp.y + mc.player.getEyeHeight(mc.player.getPose()) + 0.15;

            double dx = pp.x - cam.x;
            double dy = headY - cam.y;
            double dz = pp.z - cam.z;

            // Outer glow pass (larger, transparent)
            RenderUtils.drawCone(ms, dx, dy, dz,
                    cfg.chinaHatSize * 1.12, cfg.chinaHatHeight * 1.06, 28,
                    cfg.chinaHatR, cfg.chinaHatG, cfg.chinaHatB,
                    cfg.chinaHatAlpha * 0.35f);

            // Main hat
            RenderUtils.drawCone(ms, dx, dy, dz,
                    cfg.chinaHatSize, cfg.chinaHatHeight, 28,
                    cfg.chinaHatR, cfg.chinaHatG, cfg.chinaHatB,
                    cfg.chinaHatAlpha);
        });
    }

    @Override public String getName()   { return "ChinaHat"; }
    @Override public boolean isEnabled(){ return VisualsClient.config.chinaHatEnabled; }
}
