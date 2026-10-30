package com.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

/**
 * All mod settings serialised to visuals.json in the config directory.
 * Edit the JSON file while the game is closed to change settings.
 */
public class VisualsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("visuals.json");

    // ── ChinaHat ──────────────────────────────────────────────────────────
    public boolean chinaHatEnabled = true;
    public float   chinaHatR       = 1.0f;
    public float   chinaHatG       = 0.8f;
    public float   chinaHatB       = 0.0f;
    public float   chinaHatAlpha   = 0.90f;
    public float   chinaHatSize    = 0.45f;   // base radius
    public float   chinaHatHeight  = 0.38f;   // cone height

    // ── Halo ──────────────────────────────────────────────────────────────
    public boolean haloEnabled    = true;
    public boolean haloRainbow    = false;
    public float   haloR          = 0.0f;
    public float   haloG          = 0.8f;
    public float   haloB          = 1.0f;
    public float   haloAlpha      = 0.85f;
    public float   haloRadius     = 0.40f;
    public float   haloThickness  = 0.055f;
    public float   haloOffsetY    = 0.28f;   // above eye level

    // ── JumpCircles ───────────────────────────────────────────────────────
    public boolean jumpCirclesEnabled   = true;
    public float   jumpCirclesR         = 0.20f;
    public float   jumpCirclesG         = 0.80f;
    public float   jumpCirclesB         = 1.00f;
    public float   jumpCirclesAlpha     = 0.70f;
    public float   jumpCirclesMaxRadius = 2.0f;
    public int     jumpCirclesDuration  = 22;   // ticks

    // ── Trails ────────────────────────────────────────────────────────────
    public boolean trailsEnabled = true;
    public boolean trailsRainbow = true;
    public float   trailsR       = 0.0f;
    public float   trailsG       = 0.5f;
    public float   trailsB       = 1.0f;
    public float   trailsAlpha   = 0.55f;
    public int     trailsLength  = 35;

    // ── HitColor ──────────────────────────────────────────────────────────
    public boolean hitColorEnabled = true;
    public float   hitColorR       = 0.6f;
    public float   hitColorG       = 0.0f;
    public float   hitColorB       = 1.0f;   // purple default
    public float   hitColorAlpha   = 0.45f;

    // ── DamageParticles ───────────────────────────────────────────────────
    public boolean damageParticlesEnabled = true;
    /** HEARTS | SPARKS | NUMBERS */
    public String  damageParticlesType    = "HEARTS";
    public float   damageParticlesR       = 1.0f;
    public float   damageParticlesG       = 0.2f;
    public float   damageParticlesB       = 0.5f;

    // ── TargetESP / TargetHUD ─────────────────────────────────────────────
    public boolean targetESPEnabled  = true;
    public boolean targetESPBox      = true;
    public boolean targetHUDEnabled  = true;
    public float   targetESPR        = 1.0f;
    public float   targetESPG        = 0.15f;
    public float   targetESPB        = 0.15f;

    // ── ViewModel ─────────────────────────────────────────────────────────
    public boolean viewModelEnabled = false;
    public float   viewModelX       = 0.0f;
    public float   viewModelY       = 0.0f;
    public float   viewModelZ       = 0.0f;
    public float   viewModelScaleX  = 1.0f;
    public float   viewModelScaleY  = 1.0f;
    public float   viewModelScaleZ  = 1.0f;

    // ── SwingAnimation ────────────────────────────────────────────────────
    public boolean swingAnimationEnabled = false;
    public float   swingSpeed           = 1.5f;   // multiplier (>1 = faster)

    // ── OldAnimations (1.7 style) ─────────────────────────────────────────
    public boolean oldAnimationsEnabled = false;

    // ── CustomHUD ─────────────────────────────────────────────────────────
    public boolean customHUDEnabled = false;
    public boolean showHealthBar    = true;
    public boolean showArmorBar     = true;
    public boolean showHungerBar    = true;

    // ── Blur ──────────────────────────────────────────────────────────────
    public boolean blurEnabled  = true;
    public float   blurDarkness = 0.65f;   // 0..1 — overlay opacity

    // ── Watermark ─────────────────────────────────────────────────────────
    public boolean watermarkEnabled  = true;
    public String  watermarkText     = "Visuals";
    public boolean watermarkShowFPS  = true;
    public boolean watermarkShowPing = true;
    public float   watermarkR        = 0.0f;
    public float   watermarkG        = 0.85f;
    public float   watermarkB        = 1.0f;

    // ── ArrayList ─────────────────────────────────────────────────────────
    public boolean arrayListEnabled  = true;
    public boolean arrayListGradient = true;
    public float   arrayListR1       = 0.0f;
    public float   arrayListG1       = 0.85f;
    public float   arrayListB1       = 1.0f;
    public float   arrayListR2       = 0.9f;
    public float   arrayListG2       = 0.0f;
    public float   arrayListB2       = 0.6f;

    // ─────────────────────────────────────────────────────────────────────

    public static VisualsConfig load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) {
                VisualsConfig cfg = GSON.fromJson(r, VisualsConfig.class);
                if (cfg != null) return cfg;
            } catch (Exception ignored) {}
        }
        VisualsConfig cfg = new VisualsConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
