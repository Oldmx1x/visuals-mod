package com.visuals.feature;

import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Renders a customisable glowing torus (halo) above the local player's head.
 * Supports rainbow animation.
 */
public class HaloFeature extends AbstractFeature {

    @Override
    public void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!VisualsClient.config.haloEnabled) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            var cfg = VisualsClient.config;
            Vec3d cam = context.camera().getPos();
            var ms  = context.matrixStack();

            Vec3d pp = mc.player.getLerpedPos(context.tickDelta());
            double headY = pp.y + mc.player.getEyeHeight(mc.player.getPose()) + cfg.haloOffsetY;

            double dx = pp.x - cam.x;
            double dy = headY - cam.y;
            double dz = pp.z - cam.z;

            float r, g, b;
            if (cfg.haloRainbow) {
                float[] rb = RenderUtils.rainbow(0f);
                r = rb[0]; g = rb[1]; b = rb[2];
            } else {
                r = cfg.haloR; g = cfg.haloG; b = cfg.haloB;
            }

            // Glow layer
            RenderUtils.drawTorus(ms, dx, dy, dz,
                    cfg.haloRadius * 1.18, cfg.haloThickness * 2.2,
                    32, 8, r, g, b, cfg.haloAlpha * 0.22f);

            // Main halo
            RenderUtils.drawTorus(ms, dx, dy, dz,
                    cfg.haloRadius, cfg.haloThickness,
                    32, 8, r, g, b, cfg.haloAlpha);
        });
    }

    @Override public String getName()    { return "Halo"; }
    @Override public boolean isEnabled() { return VisualsClient.config.haloEnabled; }
}
