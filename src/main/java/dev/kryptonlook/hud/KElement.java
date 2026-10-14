package dev.kryptonlook.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Shared look: dark translucent panel, thin accent bar, optional rainbow accent. */
public abstract class KElement extends HudElement {
    protected static final double PAD = 3;
    protected static final Color TEXT = new Color(215, 215, 230, 255);

    private final SettingGroup sg = settings.getDefaultGroup();

    protected final Setting<SettingColor> accent = sg.add(new ColorSetting.Builder()
        .name("accent").description("Accent color.")
        .defaultValue(new SettingColor(80, 190, 255, 255)).build());

    protected final Setting<Boolean> rainbow = sg.add(new BoolSetting.Builder()
        .name("rainbow").description("Cycle the accent through the rainbow.")
        .defaultValue(false).build());

    protected final Setting<Double> opacity = sg.add(new DoubleSetting.Builder()
        .name("opacity").description("Background opacity.")
        .defaultValue(1).min(0).max(1).sliderRange(0, 1).build());

    protected final Setting<Boolean> glass = sg.add(new BoolSetting.Builder()
        .name("glass").description("Frosted-glass look: soft highlight and thin edge.")
        .defaultValue(true).build());

    protected KElement(HudElementInfo<?> info) {
        super(info);
    }

    protected Color acc(double offset) {
        return rainbow.get() ? rainbowColor(offset) : accent.get();
    }

    protected Color bg() {
        return new Color(18, 18, 24, (int) (195 * opacity.get()));
    }

    protected static Color fade(Color c, double f) {
        return new Color(c.r, c.g, c.b, (int) (c.a * Math.max(0, Math.min(1, f))));
    }

    /** Background panel; with "glass" on it gets a light top sheen and a thin edge. */
    protected void panel(HudRenderer r, double px, double py, double w, double h) {
        r.quad(px, py, w, h, bg());
        if (glass.get()) {
            double o = opacity.get();
            r.quad(px, py, w, h * 0.45, new Color(255, 255, 255, (int) (16 * o)));
            Color edge = new Color(255, 255, 255, (int) (34 * o));
            r.quad(px, py, w, 1, edge);
            r.quad(px, py + h - 1, w, 1, edge);
            r.quad(px, py, 1, h, edge);
            r.quad(px + w - 1, py, 1, h, edge);
        }
    }

    /** True when there is no world (e.g. in the HUD editor on the title screen). */
    protected boolean preview() {
        return mc.player == null || mc.world == null;
    }

    protected static Color rainbowColor(double offset) {
        double h = ((System.currentTimeMillis() / 4000.0) + offset) % 1.0;
        double s = 0.65, v = 1.0;
        double c = v * s, hp = h * 6, x = c * (1 - Math.abs(hp % 2 - 1)), m = v - c;
        double r, g, b;
        if (hp < 1) { r = c; g = x; b = 0; }
        else if (hp < 2) { r = x; g = c; b = 0; }
        else if (hp < 3) { r = 0; g = c; b = x; }
        else if (hp < 4) { r = 0; g = x; b = c; }
        else if (hp < 5) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return new Color((int) ((r + m) * 255), (int) ((g + m) * 255), (int) ((b + m) * 255), 255);
    }

    /** Draws a panel with an optional accent-colored title and a list of lines. */
    protected void textPanel(HudRenderer r, String title, List<String> lines) {
        double th = r.textHeight(false);
        double w = title == null ? 0 : r.textWidth(title, false);
        for (String l : lines) w = Math.max(w, r.textWidth(l, false));

        double h = PAD * 2 + lines.size() * (th + 1) - 1;
        if (title != null) h += th + 2;

        setSize(w + PAD * 2 + 3, h);
        panel(r, x, y, getWidth(), getHeight());
        r.quad(x, y, 2, getHeight(), acc(0));

        double tx = x + PAD + 3, ty = y + PAD;
        if (title != null) {
            r.text(title, tx, ty, acc(0), false);
            ty += th + 2;
        }
        for (String l : lines) {
            r.text(l, tx, ty, TEXT, false);
            ty += th + 1;
        }
    }
}
