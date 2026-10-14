package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Module toggle cards: 280x65, dark rounded card, round badge, title + "Has been enabled/disabled.",
 * and a progress bar that drains over the duration (green = enabled, red = disabled).
 * Toggles are detected by comparing the active-module set each frame.
 */
public class NotificationElement extends KElement {
    public static final HudElementInfo<NotificationElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-notifications", "Module toggle notifications.", NotificationElement::new);

    public enum Corner { TopLeft, TopRight, BottomLeft, BottomRight }

    private static final double W = 280, H = 65, GAP = 12, MARGIN = 15, RADIUS = 8;
    private static final int MAX = 5;

    private final SettingGroup sg = settings.createGroup("Notifications");
    private final Setting<Corner> corner = sg.add(new EnumSetting.Builder<Corner>()
        .name("corner").description("Which side cards slide in from and stack towards.")
        .defaultValue(Corner.BottomRight).build());
    private final Setting<Double> duration = sg.add(new DoubleSetting.Builder()
        .name("duration").description("How long a card stays (ms).")
        .defaultValue(2500).min(1000).max(5000).sliderRange(1000, 5000).build());
    private final Setting<Boolean> sound = sg.add(new BoolSetting.Builder()
        .name("sound").description("Play a click when a card appears.").defaultValue(false).build());

    private static class Note {
        final String title; final boolean enabled; final long born = System.currentTimeMillis();
        final long life; double slide = 0;
        Note(String t, boolean e, long l) { title = t; enabled = e; life = l; }
        long age() { return System.currentTimeMillis() - born; }
        boolean expired() { return age() > life; }
        double fade() {
            long a = age();
            if (a < 200) return a / 200.0;
            if (a > life - 200) return Math.max(0, 1.0 - (a - (life - 200)) / 200.0);
            return 1;
        }
        double progress() { return Math.max(0, Math.min(1, 1.0 - age() / (double) life)); }
    }

    private final List<Note> notes = new ArrayList<>();
    private Set<Module> last = null;

    public NotificationElement() { super(INFO); }

    private static boolean ignored(Module m) {
        String t = m.title.toLowerCase();
        return t.contains("gui") || t.contains("hud") || t.contains("click");
    }

    private void poll() {
        Set<Module> now = new HashSet<>(Modules.get().getActive());
        if (last != null) {
            for (Module m : now) if (!last.contains(m) && !ignored(m)) push(m.title, true);
            for (Module m : last) if (!now.contains(m) && !ignored(m)) push(m.title, false);
        }
        last = now;   // first call only records the starting state
    }

    private void push(String title, boolean enabled) {
        notes.add(0, new Note(title, enabled, duration.get().longValue()));
        if (sound.get() && mc.getSoundManager() != null)
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public void render(HudRenderer r) {
        tickDt();
        if (preview()) {
            notes.clear();
            Note a = new Note("Fullbright", true, 100000); a.slide = 1;
            Note b = new Note("Sprint", false, 100000); b.slide = 1;
            notes.add(a); notes.add(b);
        } else {
            poll();
            notes.removeIf(Note::expired);
            while (notes.size() > MAX) notes.remove(notes.size() - 1);
        }

        boolean right = corner.get() == Corner.TopRight || corner.get() == Corner.BottomRight;
        boolean bottom = corner.get() == Corner.BottomLeft || corner.get() == Corner.BottomRight;
        int n = Math.max(1, notes.size());
        double total = n * H + (n - 1) * GAP;
        setSize(W, total);

        double th = r.textHeight(false);
        int index = 0;
        for (Note note : notes) {
            long age = note.age();
            if (age < 300) note.slide = lerp(note.slide, 1, 20, dt);
            else if (age < note.life - 300) note.slide = 1;
            else note.slide = lerp(note.slide, 0, 20, dt);

            double fade = preview() ? 1 : note.fade();
            if (fade <= 0) continue;

            double offset = (W + MARGIN) * (1 - note.slide);
            double nx = right ? x + offset : x - offset;
            double ny = bottom ? y + total - (index + 1) * H - index * GAP : y + index * (H + GAP);

            // card
            rrect(r, nx, ny, W, H, (int) RADIUS, new Color(18, 18, 22, (int) (200 * fade)));

            // badge
            double bx = nx + 35, by = ny + H / 2, br = 18;
            circle(r, bx, by, br, note.enabled ? new Color(50, 55, 60, (int) (220 * fade))
                                               : new Color(55, 45, 45, (int) (220 * fade)));
            String badge = note.enabled ? "ON" : "OFF";
            double bs = 0.8;
            r.text(badge, bx - r.textWidth(badge, false, bs) / 2, by - r.textHeight(false, bs) / 2,
                note.enabled ? new Color(40, 200, 80, (int) (255 * fade)) : new Color(240, 70, 70, (int) (255 * fade)),
                false, bs);

            // progress bar
            double barW = W - 16, barH = 3, barX = nx + 8, barY = ny + H - barH - 4;
            r.quad(barX, barY, barW, barH, new Color(30, 30, 35, (int) (150 * fade)));
            double filled = barW * (preview() ? 0.6 : note.progress());
            if (filled > 0) r.quad(barX, barY, filled, barH,
                note.enabled ? new Color(40, 200, 80, (int) (255 * fade)) : new Color(240, 70, 70, (int) (255 * fade)));

            // text
            r.text(note.title, nx + 68, ny + 14, fade(PRIMARY, fade), false, 1.1);
            r.text(note.enabled ? "Has been enabled." : "Has been disabled.", nx + 68, ny + 36,
                withAlpha(SECONDARY, (int) (220 * fade)), false, 0.85);
            index++;
        }
    }
}
