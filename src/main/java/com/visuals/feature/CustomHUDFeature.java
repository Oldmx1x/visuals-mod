package com.visuals.feature;

import com.visuals.VisualsClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

/**
 * Draws custom health and armour bars near the hotbar.
 * The vanilla bars are hidden via MixinInGameHud when this is enabled.
 */
public class CustomHUDFeature extends AbstractFeature {

    @Override
    public void register() {
        HudRenderCallback.EVENT.register((ctx, td) -> {
            if (!VisualsClient.config.customHUDEnabled) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            if (mc.options.hudHidden || mc.currentScreen != null) return;

            var cfg = VisualsClient.config;
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            int cx = sw / 2;

            // Position bars above hotbar (hotbar is at sh - 22)
            int baseY = sh - 48;

            // ── Health ────────────────────────────────────────────────────
            if (cfg.showHealthBar) {
                float hp    = mc.player.getHealth();
                float maxHp = mc.player.getMaxHealth();
                float pct   = MathHelper.clamp(hp / maxHp, 0, 1);

                int bw = 91, bh = 5;
                int bx = cx - bw - 1;
                int by = baseY;

                ctx.fill(bx, by,      bx + bw, by + bh, 0xBB111111);
                int col = MathHelper.hsvToRgb(pct * .33f, 1f, 1f) | 0xFF000000;
                ctx.fill(bx, by,      bx + (int)(bw * pct), by + bh, col);

                // Label
                String lbl = String.format("❤ %.0f", hp);
                ctx.drawText(mc.textRenderer, lbl, bx, by - 10, 0xFFCC3333, true);
            }

            // ── Armour ────────────────────────────────────────────────────
            if (cfg.showArmorBar) {
                int arm   = mc.player.getArmor();
                float pct = arm / 20f;

                int bw = 91, bh = 5;
                int bx = cx + 1;
                int by = baseY;

                ctx.fill(bx, by, bx + bw, by + bh, 0xBB111111);
                ctx.fill(bx, by, bx + (int)(bw * pct), by + bh, 0xFF4455FF);

                String lbl = String.format("🛡 %d", arm);
                ctx.drawText(mc.textRenderer, lbl, bx, by - 10, 0xFF4455FF, true);
            }

            // ── Hunger ────────────────────────────────────────────────────
            if (cfg.showHungerBar) {
                int food  = mc.player.getHungerManager().getFoodLevel();
                float pct = food / 20f;

                int bw = 91, bh = 3;
                int bx = cx + 1;
                int by = baseY + 8;

                ctx.fill(bx, by, bx + bw, by + bh, 0xBB111111);
                ctx.fill(bx, by, bx + (int)(bw * pct), by + bh, 0xFFCC8833);
            }
        });
    }

    @Override public String getName()    { return "CustomHUD"; }
    @Override public boolean isEnabled() { return VisualsClient.config.customHUDEnabled; }
}
