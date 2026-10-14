package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;

import java.util.List;

public class WatermarkElement extends KElement {
    public static final HudElementInfo<WatermarkElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-watermark", "Client name watermark.", WatermarkElement::new);

    private final Setting<String> text = settings.getDefaultGroup().add(new StringSetting.Builder()
        .name("text").description("Watermark text.").defaultValue("KRYPTON+").build());

    public WatermarkElement() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        textPanel(renderer, null, List.of(text.get().toUpperCase()));
    }
}
