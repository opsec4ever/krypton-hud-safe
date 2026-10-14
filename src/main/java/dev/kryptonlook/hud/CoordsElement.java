package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CoordsElement extends KElement {
    public static final HudElementInfo<CoordsElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-coords", "Player coordinates.", CoordsElement::new);

    public CoordsElement() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        String s = preview() ? "0 | 64 | 0"
            : String.format("%.0f | %.0f | %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        textPanel(renderer, null, List.of(s));
    }
}
