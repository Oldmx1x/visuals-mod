package com.visuals.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import static java.lang.Math.*;

public final class RenderUtils {

    private RenderUtils() {}

    // ── OpenGL state helpers ───────────────────────────────────────────────

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void cleanupRender() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // ── 2D primitives (used for HUD, etc.) ───────────────────────────────

    /** ARGB int from 0..1 float components */
    public static int argb(float a, float r, float g, float b) {
        return ((int)(a * 255) << 24)
             | ((int)(r * 255) << 16)
             | ((int)(g * 255) <<  8)
             |  (int)(b * 255);
    }

    // ── 3D geometry (world-space, centred on origin before MatrixStack push) ─

    /**
     * Filled horizontal disc at (x,y,z) world offset from camera.
     */
    public static void drawFilledCircle(MatrixStack ms, double x, double y, double z,
                                        double radius, int segments,
                                        float r, float g, float b, float a) {
        ms.push();
        ms.translate(x, y, z);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        setupRender();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        Matrix4f m = ms.peek().getPositionMatrix();

        buf.vertex(m, 0, 0, 0).color(r, g, b, a).next();
        for (int i = 0; i <= segments; i++) {
            double ang = 2.0 * PI * i / segments;
            buf.vertex(m, (float)(radius * cos(ang)), 0, (float)(radius * sin(ang)))
               .color(r, g, b, a).next();
        }
        tess.draw();
        cleanupRender();
        ms.pop();
    }

    /**
     * Outlined horizontal circle.
     */
    public static void drawCircleOutline(MatrixStack ms, double x, double y, double z,
                                         double radius, int segments,
                                         float r, float g, float b, float a,
                                         float lineWidth) {
        ms.push();
        ms.translate(x, y, z);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);
        setupRender();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        Matrix4f m = ms.peek().getPositionMatrix();

        for (int i = 0; i <= segments; i++) {
            double ang = 2.0 * PI * i / segments;
            buf.vertex(m, (float)(radius * cos(ang)), 0, (float)(radius * sin(ang)))
               .color(r, g, b, a).next();
        }
        tess.draw();
        cleanupRender();
        ms.pop();
    }

    /**
     * Wire-frame axis-aligned bounding box.
     */
    public static void drawBox(MatrixStack ms,
                               double x1, double y1, double z1,
                               double x2, double y2, double z2,
                               float r, float g, float b, float a,
                               float lineWidth) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);
        setupRender();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f m = ms.peek().getPositionMatrix();

        float fx1 = (float)x1, fy1 = (float)y1, fz1 = (float)z1;
        float fx2 = (float)x2, fy2 = (float)y2, fz2 = (float)z2;

        // Bottom
        line(buf, m, fx1,fy1,fz1, fx2,fy1,fz1, r,g,b,a);
        line(buf, m, fx2,fy1,fz1, fx2,fy1,fz2, r,g,b,a);
        line(buf, m, fx2,fy1,fz2, fx1,fy1,fz2, r,g,b,a);
        line(buf, m, fx1,fy1,fz2, fx1,fy1,fz1, r,g,b,a);
        // Top
        line(buf, m, fx1,fy2,fz1, fx2,fy2,fz1, r,g,b,a);
        line(buf, m, fx2,fy2,fz1, fx2,fy2,fz2, r,g,b,a);
        line(buf, m, fx2,fy2,fz2, fx1,fy2,fz2, r,g,b,a);
        line(buf, m, fx1,fy2,fz2, fx1,fy2,fz1, r,g,b,a);
        // Verticals
        line(buf, m, fx1,fy1,fz1, fx1,fy2,fz1, r,g,b,a);
        line(buf, m, fx2,fy1,fz1, fx2,fy2,fz1, r,g,b,a);
        line(buf, m, fx2,fy1,fz2, fx2,fy2,fz2, r,g,b,a);
        line(buf, m, fx1,fy1,fz2, fx1,fy2,fz2, r,g,b,a);

        tess.draw();
        cleanupRender();
    }

    private static void line(BufferBuilder buf, Matrix4f m,
                              float x1,float y1,float z1,
                              float x2,float y2,float z2,
                              float r,float g,float b,float a) {
        buf.vertex(m,x1,y1,z1).color(r,g,b,a).next();
        buf.vertex(m,x2,y2,z2).color(r,g,b,a).next();
    }

    /**
     * Filled horizontal torus ring (used for Halo).
     * R = major radius (ring centre), r = tube radius.
     */
    public static void drawTorus(MatrixStack ms, double x, double y, double z,
                                 double R, double r, int segs, int tubeSegs,
                                 float cr, float cg, float cb, float ca) {
        ms.push();
        ms.translate(x, y, z);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        setupRender();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f m = ms.peek().getPositionMatrix();

        for (int i = 0; i < segs; i++) {
            double a1 = 2.0 * PI * i / segs;
            double a2 = 2.0 * PI * (i + 1) / segs;
            for (int j = 0; j < tubeSegs; j++) {
                double p1 = 2.0 * PI * j / tubeSegs;
                double p2 = 2.0 * PI * (j + 1) / tubeSegs;
                float[] v1 = tv(R, r, a1, p1);
                float[] v2 = tv(R, r, a1, p2);
                float[] v3 = tv(R, r, a2, p2);
                float[] v4 = tv(R, r, a2, p1);
                buf.vertex(m,v1[0],v1[1],v1[2]).color(cr,cg,cb,ca).next();
                buf.vertex(m,v2[0],v2[1],v2[2]).color(cr,cg,cb,ca).next();
                buf.vertex(m,v3[0],v3[1],v3[2]).color(cr,cg,cb,ca).next();
                buf.vertex(m,v4[0],v4[1],v4[2]).color(cr,cg,cb,ca).next();
            }
        }
        tess.draw();
        cleanupRender();
        ms.pop();
    }

    private static float[] tv(double R, double r, double theta, double phi) {
        return new float[]{
            (float)((R + r * cos(phi)) * cos(theta)),
            (float)(r * sin(phi)),
            (float)((R + r * cos(phi)) * sin(theta))
        };
    }

    /**
     * Solid cone with tip pointing up (used for ChinaHat).
     * baseRadius / height in local units. Centred at (x,y,z).
     */
    public static void drawCone(MatrixStack ms, double x, double y, double z,
                                double baseRadius, double height, int segs,
                                float r, float g, float b, float a) {
        ms.push();
        ms.translate(x, y, z);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        setupRender();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        Matrix4f m = ms.peek().getPositionMatrix();

        // Sides
        buf.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < segs; i++) {
            double a1 = 2.0 * PI * i / segs;
            double a2 = 2.0 * PI * (i + 1) / segs;
            float x1 = (float)(baseRadius * cos(a1));
            float z1 = (float)(baseRadius * sin(a1));
            float x2 = (float)(baseRadius * cos(a2));
            float z2 = (float)(baseRadius * sin(a2));
            buf.vertex(m, 0, (float)height, 0).color(r, g, b, a).next();
            buf.vertex(m, x1, 0, z1).color(r, g, b, a * 0.35f).next();
            buf.vertex(m, x2, 0, z2).color(r, g, b, a * 0.35f).next();
        }
        tess.draw();

        // Base cap
        buf.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m, 0, 0, 0).color(r, g, b, a * 0.25f).next();
        for (int i = 0; i <= segs; i++) {
            double ang = 2.0 * PI * i / segs;
            buf.vertex(m, (float)(baseRadius * cos(ang)), 0, (float)(baseRadius * sin(ang)))
               .color(r, g, b, a * 0.25f).next();
        }
        tess.draw();

        cleanupRender();
        ms.pop();
    }

    // ── Colour helpers ────────────────────────────────────────────────────

    /** Returns {r,g,b} in 0..1 from HSB, cycling over time with per-element offset */
    public static float[] rainbow(float offset) {
        float hue = ((System.currentTimeMillis() % 2000) / 2000f + offset) % 1f;
        java.awt.Color c = java.awt.Color.getHSBColor(hue, 1f, 1f);
        return new float[]{ c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f };
    }

    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
