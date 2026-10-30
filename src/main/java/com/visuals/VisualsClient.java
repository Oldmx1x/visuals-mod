package com.visuals;

import com.visuals.config.VisualsConfig;
import com.visuals.feature.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class VisualsClient implements ClientModInitializer {

    public static final String MOD_ID = "visuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static VisualsConfig config;

    // Feature instances (public so mixins can read config)
    public static ChinaHatFeature    chinaHat;
    public static HaloFeature        halo;
    public static JumpCirclesFeature jumpCircles;
    public static TrailsFeature      trails;
    public static DamageParticlesFeature damageParticles;
    public static TargetESPFeature   targetESP;
    public static WatermarkFeature   watermark;
    public static ArrayListFeature   arrayList;
    public static CustomHUDFeature   customHUD;
    public static BlurFeature        blur;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Visuals] Initialising...");

        config = VisualsConfig.load();

        chinaHat        = new ChinaHatFeature();
        halo            = new HaloFeature();
        jumpCircles     = new JumpCirclesFeature();
        trails          = new TrailsFeature();
        damageParticles = new DamageParticlesFeature();
        targetESP       = new TargetESPFeature();
        watermark       = new WatermarkFeature();
        arrayList       = new ArrayListFeature();
        customHUD       = new CustomHUDFeature();
        blur            = new BlurFeature();

        chinaHat.register();
        halo.register();
        jumpCircles.register();
        trails.register();
        damageParticles.register();
        targetESP.register();
        watermark.register();
        arrayList.register();
        customHUD.register();
        blur.register();

        LOGGER.info("[Visuals] Ready!");
    }
}
