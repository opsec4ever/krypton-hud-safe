package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CompassElement extends KElement {
    public static final HudElementInfo<CompassElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-compass", "Facing direction and heading.", CompassElement::new);

    // Minecraft yaw: 0 = south, 90 = west, 180 = north, 270 = east
    private static final String[] DIRS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public CompassElement() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        double yaw = preview() ? 180 : mc.player.getYaw();
        yaw = ((yaw % 360) + 360) % 360;
        String dir = DIRS[(int) Math.round(yaw / 45.0) % 8];
        textPanel(renderer, null, List.of(String.format("%-2s  %3.0f", dir, yaw)));
    }
}
