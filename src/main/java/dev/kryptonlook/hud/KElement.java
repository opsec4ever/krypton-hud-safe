package dev.kryptonlook.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Shared look for every element. Numbers (sizes, paddings, colors) were measured from the
 * reference HUD: navy panel fill, purple accent, white primary / lavender secondary text.
 */
public abstract class KElement extends HudElement {
    protected static final Color PRIMARY = new Color(255, 255, 255, 255);
    protected static final Color SECONDARY = new Color(195, 185, 235, 255);
    protected static final Color SEPARATOR = new Color(140, 90, 255, 90);

    private final SettingGroup sg = settings.getDefaultGroup();

    protected final Setting<SettingColor> accent = sg.add(new ColorSetting.Builder()
        .name("accent").description("Primary accent color.")
        .defaultValue(new SettingColor(155, 100, 255, 255)).build());

    protected final Setting<Boolean> rainbow = sg.add(new BoolSetting.Builder()
        .name("rainbow").description("Cycle the accent through the rainbow.")
        .defaultValue(false).build());

    protected final Setting<Double> opacity = sg.add(new DoubleSetting.Builder()
        .name("opacity").description("Opacity of the panels.")
        .defaultValue(0.75).min(0).max(1).sliderRange(0, 1).build());

    private long lastNs = 0;
    /** Seconds since this element last rendered; call tickDt() once at the top of render(). */
    protected double dt = 0.016;

    protected KElement(HudElementInfo<?> info) {
        super(info);
    }

    protected void tickDt() {
        long now = System.nanoTime();
        dt = lastNs == 0 ? 0.016 : Math.min(0.1, (now - lastNs) / 1e9);
        lastNs = now;
    }

    protected static double lerp(double from, double to, double speed, double dt) {
        double t = Math.max(0, Math.min(1, dt * speed));
        return from * (1 - t) + to * t;
    }

    protected int alpha() {
        return (int) (opacity.get() * 255);
    }

    protected Color accent(int index) {
        if (!rainbow.get()) return accent.get();
        double h = ((System.currentTimeMillis() / 4000.0) + index * 0.05) % 1.0;
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

    protected static Color withAlpha(Color c, int a) {
        return new Color(c.r, c.g, c.b, Math.max(0, Math.min(255, a)));
    }

    protected static Color fade(Color c, double f) {
        return withAlpha(c, (int) (c.a * Math.max(0, Math.min(1, f))));
    }

    /** Rounded rectangle built from stacked quads (corner radius in px). */
    protected void rrect(HudRenderer r, double x, double y, double w, double h, int rad, Color c) {
        if (c.a <= 0 || w <= 0 || h <= 0) return;
        rad = (int) Math.min(rad, Math.min(w, h) / 2);
        if (rad <= 0) { r.quad(x, y, w, h, c); return; }
        r.quad(x, y + rad, w, h - 2 * rad, c);
        for (int i = 0; i < rad; i++) {
            double dy = rad - i - 0.5;
            double inset = rad - Math.sqrt(rad * (double) rad - dy * dy);
            inset = Math.round(inset);
            r.quad(x + inset, y + i, w - 2 * inset, 1, c);
            r.quad(x + inset, y + h - 1 - i, w - 2 * inset, 1, c);
        }
    }

    /** Filled circle drawn as one quad per pixel row. */
    protected void circle(HudRenderer r, double cx, double cy, double radius, Color c) {
        if (c.a <= 0 || radius <= 0) return;
        int R = (int) Math.ceil(radius);
        for (int row = -R; row < R; row++) {
            double dy = row + 0.5;
            double half2 = radius * radius - dy * dy;
            if (half2 <= 0) continue;
            double half = Math.sqrt(half2);
            r.quad(cx - half, cy + row, half * 2, 1, c);
        }
    }

    /** True when there is no world (HUD editor on the title screen). */
    protected boolean preview() {
        return mc.player == null || mc.world == null;
    }
}
