package dev.kryptonlook;

import dev.kryptonlook.hud.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Krypton-style HUD elements for Meteor. Adds no modules; place the elements in Meteor's HUD editor.
 * (GUI restyling is parked in _gui-later/ and not part of this build.)
 */
public class KryptonLookAddon extends MeteorAddon {
    public static final HudGroup HUD_GROUP = new HudGroup("Krypton Look");

    @Override
    public void onInitialize() {
        installFont();
        Hud hud = Hud.get();
        hud.register(WatermarkElement.INFO);
        hud.register(InfoElement.INFO);
        hud.register(CoordsElement.INFO);
        hud.register(StatsElement.INFO);
        hud.register(ModuleListElement.INFO);
        hud.register(PotionsElement.INFO);
        hud.register(HotkeysElement.INFO);
        hud.register(RadarElement.INFO);
        hud.register(CompassElement.INFO);
        hud.register(NotificationElement.INFO);
    }

    /** Copies the bundled font into meteor-client/fonts if missing. Restart once, then pick it in Config > Font. */
    private void installFont() {
        try {
            File dir = new File(MeteorClient.FOLDER, "fonts");
            File out = new File(dir, "MinecraftTen.ttf");
            if (out.exists()) return;
            try (InputStream in = KryptonLookAddon.class.getResourceAsStream("/assets/krypton-look/fonts/MinecraftTen.ttf")) {
                if (in == null) return;
                dir.mkdirs();
                Files.copy(in, out.toPath());
            }
        } catch (Exception ignored) {
            // font is optional; never block startup
        }
    }

    @Override
    public String getPackage() {
        return "dev.kryptonlook";
    }
}
