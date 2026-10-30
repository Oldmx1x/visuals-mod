package com.visuals.feature;

import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a gradient-coloured list of currently enabled modules on the
 * right side of the screen, with a thin colour bar on the edge.
 */
public class ArrayListFeature extends AbstractFeature {

    @Override
    public void register() {
        HudRenderCallback.EVENT.register((ctx, td) -> {
            if (!VisualsClient.config.arrayListEnabled) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            var cfg = VisualsClient.config;

            List<String> modules = buildList(cfg);
            if (modules.isEmpty()) return;

            int sw = mc.getWindow().getScaledWidth();
            int lineH = mc.textRenderer.fontHeight + 3;
            int barX  = sw - 1; // rightmost column

            for (int i = 0; i < modules.size(); i++) {
                String name = modules.get(i);
                int tw = mc.textRenderer.getWidth(name);
                int tx = sw - tw - 5;
                int ty = 4 + i * lineH;

                // Background
                ctx.fill(tx - 2, ty - 1, sw, ty + mc.textRenderer.fontHeight + 1, 0x88000000);

                // Gradient colour
                int col;
                if (cfg.arrayListGradient && modules.size() > 1) {
                    float t = (float) i / (modules.size() - 1);
                    float r = RenderUtils.lerp(cfg.arrayListR1, cfg.arrayListR2, t);
                    float g = RenderUtils.lerp(cfg.arrayListG1, cfg.arrayListG2, t);
                    float b = RenderUtils.lerp(cfg.arrayListB1, cfg.arrayListB2, t);
                    col = RenderUtils.argb(1f, r, g, b);
                } else {
                    col = RenderUtils.argb(1f, cfg.arrayListR1, cfg.arrayListG1, cfg.arrayListB1);
                }

                // Right-side colour strip
                ctx.fill(barX, ty - 1, barX + 2, ty + mc.textRenderer.fontHeight + 1, col);
                // Module name
                ctx.drawText(mc.textRenderer, name, tx, ty, col, true);
            }
        });
    }

    private List<String> buildList(com.visuals.config.VisualsConfig c) {
        List<String> l = new ArrayList<>();
        if (c.chinaHatEnabled)        l.add("ChinaHat");
        if (c.haloEnabled)            l.add("Halo");
        if (c.jumpCirclesEnabled)     l.add("JumpCircles");
        if (c.trailsEnabled)          l.add("Trails");
        if (c.hitColorEnabled)        l.add("HitColor");
        if (c.damageParticlesEnabled) l.add("DamageParticles");
        if (c.targetESPEnabled)       l.add("TargetESP");
        if (c.targetHUDEnabled)       l.add("TargetHUD");
        if (c.viewModelEnabled)       l.add("ViewModel");
        if (c.swingAnimationEnabled)  l.add("SwingAnimation");
        if (c.oldAnimationsEnabled)   l.add("OldAnimations");
        if (c.customHUDEnabled)       l.add("CustomHUD");
        if (c.blurEnabled)            l.add("Blur");
        if (c.watermarkEnabled)       l.add("Watermark");
        return l;
    }

    @Override public String getName()    { return "ArrayList"; }
    @Override public boolean isEnabled() { return VisualsClient.config.arrayListEnabled; }
}
