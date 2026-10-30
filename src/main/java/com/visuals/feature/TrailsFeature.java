package com.visuals.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import com.visuals.VisualsClient;
import com.visuals.util.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Stores the last N positions of the local player and renders them as a
 * fading coloured (or rainbow) trail.
 */
public class TrailsFeature extends AbstractFeature {

    private record Point(Vec3d pos, float r, float g, float b) {}

    private final Deque<Point> points = new ArrayDeque<>();
    private float rainbowOff = 0f;

    @Override
    public void register() {

        // Collect positions every tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!VisualsClient.config.trailsEnabled) return;
            if (client.player == null) return;

            var cfg = VisualsClient.config;
            rainbowOff = (rainbowOff + 0.012f) % 1f;

            float pr, pg, pb;
            if (cfg.trailsRainbow) {
                float[] rb = RenderUtils.rainbow(rainbowOff);
                pr = rb[0]; pg = rb[1]; pb = rb[2];
            } else {
                pr = cfg.trailsR; pg = cfg.trailsG; pb = cfg.trailsB;
            }

            Vec3d pos = client.player.getPos().add(0, 0.08, 0);
            points.addFirst(new Point(pos, pr, pg, pb));

            while (points.size() > cfg.trailsLength) {
                points.removeLast();
            }
        });

        // Render trail as lines
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!VisualsClient.config.trailsEnabled || points.size() < 2) return;

            var cfg = VisualsClient.config;
            Vec3d cam = context.camera().getPos();
            var ms  = context.matrixStack();

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.lineWidth(2.0f);
            RenderUtils.setupRender();

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf  = tess.getBuffer();
            buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            Matrix4f mat = ms.peek().getPositionMatrix();
            Point[] arr  = points.toArray(new Point[0]);
            int total    = arr.length;

            for (int i = 0; i < total - 1; i++) {
                float fade  = 1f - (float) i / total;
                float alpha = cfg.trailsAlpha * fade;
                Point a = arr[i], b = arr[i + 1];

                // ИСПРАВЛЕНО: убраны пустые скобки у x, y, z
                buf.vertex(mat,
                        (float)(a.pos().x - cam.x),
                        (float)(a.pos().y - cam.y),
                        (float)(a.pos().z - cam.z))
                   .color(a.r(), a.g(), a.b(), alpha).next();

                buf.vertex(mat,
                        (float)(b.pos().x - cam.x),
                        (float)(b.pos().y - cam.y),
                        (float)(b.pos().z - cam.z))
                   .color(b.r(), b.g(), b.b(), alpha * 0.6f).next();
            }

            tess.draw();
            RenderUtils.cleanupRender();
        });
    }

    @Override public String getName()    { return "Trails"; }
    @Override public boolean isEnabled() { return VisualsClient.config.trailsEnabled; }
}
