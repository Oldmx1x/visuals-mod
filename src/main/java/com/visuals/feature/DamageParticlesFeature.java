package com.visuals.feature;

import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Watches nearby entities for health drops and spawns custom particles
 * (hearts, sparks, or floating damage numbers) at the damage location.
 */
public class DamageParticlesFeature extends AbstractFeature {

    public static class Particle {
        public Vec3d pos;
        public Vec3d vel;
        public final float damage;
        public int age;
        public final int maxAge = 28;
        public final float r, g, b;
        public final String type;

        Particle(Vec3d pos, float damage, float r, float g, float b, String type) {
            this.pos    = pos;
            this.damage = damage;
            this.r = r; this.g = g; this.b = b;
            this.type = type;
            this.vel = new Vec3d(
                    (Math.random() - .5) * .06,
                    Math.random() * .12 + .04,
                    (Math.random() - .5) * .06);
        }
    }

    /** Public so the HitColor mixin can also spawn particles */
    public static final List<Particle> PARTICLES = Collections.synchronizedList(new ArrayList<>());

    private final Map<Integer, Float> healthCache = new HashMap<>();

    @Override
    public void register() {

        // Tick: track health changes
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!VisualsClient.config.damageParticlesEnabled) return;
            if (client.player == null || client.world == null) return;

            var cfg = VisualsClient.config;

            client.world.getEntities().forEach(entity -> {
                if (!(entity instanceof LivingEntity living)) return;
                if (entity == client.player) return;

                int id = entity.getId();
                float hp = living.getHealth();

                if (healthCache.containsKey(id)) {
                    float prev   = healthCache.get(id);
                    float dmg    = prev - hp;
                    if (dmg > 0.1f) {
                        Vec3d p = entity.getPos().add(0, entity.getHeight() * .75, 0);
                        PARTICLES.add(new Particle(p, dmg,
                                cfg.damageParticlesR, cfg.damageParticlesG, cfg.damageParticlesB,
                                cfg.damageParticlesType));
                    }
                }
                healthCache.put(id, hp);
            });

            // Advance particles
            PARTICLES.removeIf(p -> {
                p.age++;
                p.pos = p.pos.add(p.vel);
                p.vel = p.vel.multiply(.96).add(0, -.004, 0);
                return p.age >= p.maxAge;
            });
        });

        // World render: draw 3D particles
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!VisualsClient.config.damageParticlesEnabled || PARTICLES.isEmpty()) return;

            var cfg = VisualsClient.config;
            Vec3d cam = context.camera().getPos();
            var ms  = context.matrixStack();

            for (Particle p : PARTICLES) {
                float progress = (float) p.age / p.maxAge;
                float alpha    = (1f - progress) * 0.9f;
                double dx = p.pos.x - cam.x;
                double dy = p.pos.y - cam.y;
                double dz = p.pos.z - cam.z;

                switch (p.type) {
                    case "HEARTS" -> {
                        double s = 0.06 * (1 + .4 * (1 - progress));
                        RenderUtils.drawFilledCircle(ms, dx - s * .7, dy, dz,
                                s, 8, p.r, p.g, p.b, alpha);
                        RenderUtils.drawFilledCircle(ms, dx + s * .7, dy, dz,
                                s, 8, p.r, p.g, p.b, alpha);
                        RenderUtils.drawFilledCircle(ms, dx, dy - s * .5, dz,
                                s * 1.1, 8, p.r, p.g, p.b, alpha);
                    }
                    case "SPARKS" -> {
                        float ro = (float)(0.03 + 0.08 * progress);
                        RenderUtils.drawCircleOutline(ms, dx, dy, dz,
                                ro, 6, p.r, p.g, p.b, alpha, 1.5f);
                        RenderUtils.drawCircleOutline(ms, dx, dy + .05, dz,
                                ro * .6f, 4, p.r, p.g, p.b, alpha * .6f, 1.0f);
                    }
                    default -> // NUMBERS: tiny dot (number drawn on HUD below)
                        RenderUtils.drawFilledCircle(ms, dx, dy, dz,
                                0.035, 6, p.r, p.g, p.b, alpha);
                }
            }
        });

        // HUD render: draw floating damage numbers when type = NUMBERS
        HudRenderCallback.EVENT.register((ctx, td) -> {
            if (!VisualsClient.config.damageParticlesEnabled) return;
            if (!"NUMBERS".equals(VisualsClient.config.damageParticlesType)) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;

            for (Particle p : PARTICLES) {
                float progress = (float) p.age / p.maxAge;
                // Project world position onto screen
                var camera = mc.gameRenderer.getCamera();
                Vec3d rel = p.pos.subtract(camera.getPos());
                int alpha = (int)((1 - progress) * 255);
                if (alpha < 20) continue;
                String txt = String.format("%.1f", p.damage);
                int col = (alpha << 24) | RenderUtils.argb(1, p.r, p.g, p.b) & 0xFFFFFF;
                ctx.drawText(mc.textRenderer, txt,
                        mc.getWindow().getScaledWidth() / 2 + (int)(rel.x * 5),
                        mc.getWindow().getScaledHeight() / 2 - (int)(rel.y * 8),
                        col, true);
            }
        });
    }

    @Override public String getName()    { return "DamageParticles"; }
    @Override public boolean isEnabled() { return VisualsClient.config.damageParticlesEnabled; }
}
