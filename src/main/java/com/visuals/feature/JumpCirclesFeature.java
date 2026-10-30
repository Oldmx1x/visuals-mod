package com.visuals.feature;

import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Spawns expanding concentric ring effects on the ground each time the
 * local player jumps.
 */
public class JumpCirclesFeature extends AbstractFeature {

    private static class Circle {
        final Vec3d pos;
        int age;
        final int maxAge;

        Circle(Vec3d pos, int maxAge) {
            this.pos = pos;
            this.maxAge = maxAge;
        }
    }

    private final List<Circle> circles = new ArrayList<>();
    private boolean wasOnGround = true;

    @Override
    public void register() {

        // Detect jump & age circles each tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!VisualsClient.config.jumpCirclesEnabled) return;
            if (client.player == null) return;

            boolean onGround = client.player.isOnGround();
            if (wasOnGround && !onGround && client.player.getVelocity().y > 0.0) {
                circles.add(new Circle(
                        client.player.getPos(),
                        VisualsClient.config.jumpCirclesDuration));
            }
            wasOnGround = onGround;

            Iterator<Circle> it = circles.iterator();
            while (it.hasNext()) { if (++it.next().age >= it.next().maxAge) it.remove(); }
            // Fix: re-iterate cleanly
        });

        // Separate clean iteration for ageing
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            circles.removeIf(c -> c.age >= c.maxAge);
        });

        // Render
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!VisualsClient.config.jumpCirclesEnabled || circles.isEmpty()) return;

            var cfg = VisualsClient.config;
            Vec3d cam = context.camera().getPos();
            var ms  = context.matrixStack();

            for (Circle c : circles) {
                float progress = (float) c.age / c.maxAge;
                float radius   = progress * cfg.jumpCirclesMaxRadius;
                float alpha    = cfg.jumpCirclesAlpha * (1f - progress);

                double dx = c.pos.x - cam.x;
                double dy = c.pos.y - cam.y + 0.03;
                double dz = c.pos.z - cam.z;

                // Outer ring
                RenderUtils.drawCircleOutline(ms, dx, dy, dz, radius, 36,
                        cfg.jumpCirclesR, cfg.jumpCirclesG, cfg.jumpCirclesB, alpha, 2.0f);
                // Inner trailing ring
                RenderUtils.drawCircleOutline(ms, dx, dy, dz, radius * 0.65f, 36,
                        cfg.jumpCirclesR, cfg.jumpCirclesG, cfg.jumpCirclesB, alpha * 0.45f, 1.2f);
            }
        });
    }

    @Override public String getName()    { return "JumpCircles"; }
    @Override public boolean isEnabled() { return VisualsClient.config.jumpCirclesEnabled; }
}
