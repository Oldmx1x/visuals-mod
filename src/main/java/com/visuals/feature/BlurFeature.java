package com.visuals.feature;

import com.visuals.VisualsClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

/**
 * Overlays a semi-transparent dark panel behind any open screen (inventory,
 * pause menu, etc.) to simulate a background blur / dim effect.
 *
 * NOTE: True shader-based blur requires a custom post-processing pipeline.
 * This implementation uses a dark overlay which closely mimics the look of
 * most visual mods that advertise "blur".
 */
public class BlurFeature extends AbstractFeature {

    @Override
    public void register() {
        HudRenderCallback.EVENT.register((ctx, td) -> {
            if (!VisualsClient.config.blurEnabled) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.currentScreen == null) return;

            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();

            int alpha = (int)(VisualsClient.config.blurDarkness * 180);
            ctx.fill(0, 0, sw, sh, (alpha << 24));
        });
    }

    @Override public String getName()    { return "Blur"; }
    @Override public boolean isEnabled() { return VisualsClient.config.blurEnabled; }
}
