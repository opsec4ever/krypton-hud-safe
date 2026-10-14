package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.ArrayList;
import java.util.List;

/** Enabled modules that have a keybind. */
public class HotkeysElement extends KElement {
    public static final HudElementInfo<HotkeysElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-hotkeys", "Enabled modules with keybinds.", HotkeysElement::new);

    public HotkeysElement() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        List<String> lines = new ArrayList<>();
        if (preview()) {
            lines.add("MODULE  [R]");
        } else {
            for (Module m : Modules.get().getActive()) {
                if (m.keybind.isSet()) lines.add(m.title.toUpperCase() + "  [" + m.keybind + "]");
            }
        }
        if (lines.isEmpty()) lines.add("NONE");
        textPanel(renderer, "HOTKEYS", lines);
    }
}
