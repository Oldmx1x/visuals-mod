package com.visuals.feature;

import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * TargetESP  — coloured wire-frame box + health bar rendered in 3-D around the
 *              entity currently under the crosshair.
 * TargetHUD  — small on-screen panel showing name, HP and armour.
 */
public class TargetESPFeature extends AbstractFeature {

    @Override
    public void register() {

        // ── 3-D ESP box ───────────────────────────────────────────────────
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!VisualsClient.config.targetESPEnabled
             || !VisualsClient.config.targetESPBox) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.targetedEntity == null) return;
            Entity target = mc.targetedEntity;
            if (!(target instanceof LivingEntity living)) return;

            var cfg = VisualsClient.config;
            Vec3d cam = context.camera().getPos();
            var ms  = context.matrixStack();

            float td  = context.tickDelta();
            Vec3d lp  = target.getLerpedPos(td);
            Box   box = target.getBoundingBox().offset(lp.subtract(target.getPos()));

            double ox = -cam.x, oy = -cam.y, oz = -cam.z;

            ms.push();
            ms.translate(ox, oy, oz);

            // Main bounding box
            RenderUtils.drawBox(ms,
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    cfg.targetESPR, cfg.targetESPG, cfg.targetESPB, 1f, 1.6f);

            // Thin fill-box (subtle)
            RenderUtils.drawBox(ms,
                    box.minX + .01, box.minY + .01, box.minZ + .01,
                    box.maxX - .01, box.maxY - .01, box.maxZ - .01,
                    cfg.targetESPR, cfg.targetESPG, cfg.targetESPB, .18f, .5f);

            // 3-D health bar above the entity
            float hpPct = MathHelper.clamp(living.getHealth() / living.getMaxHealth(), 0, 1);
            double cx = (box.minX + box.maxX) / 2.0;
            double topY = box.maxY + 0.25;
            double cz   = (box.minZ + box.maxZ) / 2.0;
            double bw   = (box.maxX - box.minX) * .75;
            double bh   = .055;

            // Background strip
            RenderUtils.drawBox(ms,
                    cx - bw/2, topY, cz - bh/2,
                    cx + bw/2, topY + bh*2, cz + bh/2,
                    .15f, .15f, .15f, .75f, .5f);
            // Fill strip (green → red based on HP)
            float gr = 1 - hpPct, gg = hpPct, gb = 0f;
            RenderUtils.drawBox(ms,
                    cx - bw/2, topY, cz - bh/2,
                    cx - bw/2 + bw * hpPct, topY + bh*2, cz + bh/2,
                    gr, gg, gb, .90f, .5f);

            ms.pop();
        });

        // ── HUD panel ─────────────────────────────────────────────────────
        HudRenderCallback.EVENT.register((ctx, td) -> {
            if (!VisualsClient.config.targetHUDEnabled) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.targetedEntity == null) return;
            if (!(mc.targetedEntity instanceof LivingEntity living)) return;

            int sw = mc.getWindow().getScaledWidth();
            int pw = 130, ph = 56;
            int px = sw - pw - 6, py = 6;

            // Panel background
            ctx.fill(px - 3, py - 3, px + pw + 3, py + ph + 3, 0xAA000000);
            ctx.fill(px - 3, py - 3, px + pw + 3, py - 1,       0xFF000000);

            // Name
            String name = living.getName().getString();
            ctx.drawText(mc.textRenderer, "» " + name, px, py,     0xFFFFFF, true);

            // HP text
            float hp = living.getHealth(), maxHp = living.getMaxHealth();
            String hpStr = String.format("HP: %.1f / %.1f", hp, maxHp);
            ctx.drawText(mc.textRenderer, hpStr, px, py + 13, 0xFF5555, true);

            // HP bar
            int bw = pw, bh = 5, by = py + 26;
            ctx.fill(px, by, px + bw, by + bh, 0xFF222222);
            float pct = hp / maxHp;
            int hpCol = MathHelper.hsvToRgb(pct * .33f, 1f, 1f) | 0xFF000000;
            ctx.fill(px, by, px + (int)(bw * pct), by + bh, hpCol);

            // Armour
            int armour = living.getArmor();
            String armStr = "Armor: " + armour + " / 20";
            ctx.drawText(mc.textRenderer, armStr, px, py + 38, 0x5555FF, true);
        });
    }

    @Override public String getName()    { return "TargetESP"; }
    @Override public boolean isEnabled() {
        return VisualsClient.config.targetESPEnabled
            || VisualsClient.config.targetHUDEnabled;
    }
}
