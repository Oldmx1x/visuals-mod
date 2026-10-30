package com.visuals.feature;

import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

/**
 * Draws a stylised watermark in the top-right corner showing the mod name,
 * optionally current FPS and network ping.
 */
public class WatermarkFeature extends AbstractFeature {

    @Override
    public void register() {
        HudRenderCallback.EVENT.register((ctx, td) -> {
            if (!VisualsClient.config.watermarkEnabled) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            var cfg = VisualsClient.config;

            StringBuilder sb = new StringBuilder(cfg.watermarkText);

            if (cfg.watermarkShowFPS) {
                sb.append("  ").append(mc.getCurrentFps()).append(" fps");
            }
            if (cfg.watermarkShowPing && mc.getNetworkHandler() != null && mc.player != null) {
                var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (entry != null) {
                    sb.append("  ").append(entry.getLatency()).append(" ms");
                }
            }

            String text = sb.toString();
            int tw = mc.textRenderer.getWidth(text);
            int tx = mc.getWindow().getScaledWidth() - tw - 3;
            int ty = 3;

            // Background pill
            ctx.fill(tx - 3, ty - 2, tx + tw + 3, ty + mc.textRenderer.fontHeight + 2, 0x99000000);
            // Accent left bar
            int accent = RenderUtils.argb(1f, cfg.watermarkR, cfg.watermarkG, cfg.watermarkB);
            ctx.fill(tx - 3, ty - 2, tx - 1, ty + mc.textRenderer.fontHeight + 2, accent);

            ctx.drawText(mc.textRenderer, text, tx, ty, accent, true);
        });
    }

    @Override public String getName()    { return "Watermark"; }
    @Override public boolean isEnabled() { return VisualsClient.config.watermarkEnabled; }
}
