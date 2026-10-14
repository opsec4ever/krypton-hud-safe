package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Single-line pill: player | FPS | time. */
public class InfoElement extends KElement {
    public static final HudElementInfo<InfoElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-info", "Player, FPS and time in one pill.", InfoElement::new);

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Setting<Boolean> player = bool("player", true);
    private final Setting<Boolean> fps = bool("fps", true);
    private final Setting<Boolean> time = bool("time", true);

    public InfoElement() { super(INFO); }

    private Setting<Boolean> bool(String name, boolean def) {
        return settings.getDefaultGroup().add(new BoolSetting.Builder()
            .name(name).description("Show " + name + ".").defaultValue(def).build());
    }

    @Override
    public void render(HudRenderer renderer) {
        List<String> parts = new ArrayList<>();
        if (player.get()) parts.add(preview() ? "PLAYER" : mc.player.getName().getString().toUpperCase());
        if (fps.get()) parts.add(mc.getCurrentFps() + " FPS");
        if (time.get()) parts.add(LocalTime.now().format(TIME));
        if (parts.isEmpty()) parts.add("INFO");
        textPanel(renderer, null, List.of(String.join("  |  ", parts)));
    }
}
