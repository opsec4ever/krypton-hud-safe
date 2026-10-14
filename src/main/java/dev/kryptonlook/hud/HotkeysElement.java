package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.ArrayList;
import java.util.List;

/** Every module that has a keybind: name on the left (white if on, dim if off), key on the right. */
public class HotkeysElement extends KElement {
    public static final HudElementInfo<HotkeysElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-hotkeys", "Modules with keybinds.", HotkeysElement::new);

    private static final double PAD = 12, HEADER_H = 28, ROW_H = 22, ROW_GAP = 2;
    private static final Color KEY_COLOR = new Color(180, 175, 170, 255);

    public HotkeysElement() { super(INFO); }

    private record Entry(String name, String key, boolean active) {}

    @Override
    public void render(HudRenderer r) {
        List<Entry> entries = new ArrayList<>();
        if (preview()) {
            entries.add(new Entry("Auto Totem", "R", true));
            entries.add(new Entry("Fullbright", "B", false));
        } else {
            for (Module m : Modules.get().getAll()) {
                if (!m.keybind.isSet()) continue;
                String lower = m.title.toLowerCase();
                if (lower.contains("gui") || lower.contains("click")) continue;
                entries.add(new Entry(m.title, m.keybind.toString(), m.isActive()));
            }
        }

        double th = r.textHeight(false);
        double widestName = 0, widestKey = 0;
        for (Entry e : entries) {
            widestName = Math.max(widestName, r.textWidth(e.name(), false));
            widestKey = Math.max(widestKey, r.textWidth(e.key(), false));
        }
        double titleW = PAD + r.textWidth("Hotkeys", false) + PAD;
        double rowW = PAD + widestName + 24 + widestKey + PAD;
        double width = Math.max(titleW, rowW);
        double height = HEADER_H + (entries.isEmpty() ? 0 : 3 + entries.size() * (ROW_H + ROW_GAP));
        setSize(width, height);

        rrect(r, x, y, width, HEADER_H, 5, new Color(30, 28, 24, alpha()));
        r.text("Hotkeys", x + PAD, y + (HEADER_H - th) / 2, PRIMARY, false);

        double rowY = y + HEADER_H + 3;
        for (Entry e : entries) {
            rrect(r, x, rowY, width, ROW_H, 3, new Color(25, 23, 20, (int) (alpha() * 0.8)));
            double ty = rowY + (ROW_H - th) / 2;
            r.text(e.name(), x + PAD, ty, e.active() ? PRIMARY : KEY_COLOR, false);
            r.text(e.key(), x + width - r.textWidth(e.key(), false) - PAD, ty, KEY_COLOR, false);
            rowY += ROW_H + ROW_GAP;
        }
    }
}
