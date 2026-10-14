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
 * Pop-up panels when a module is enabled or disabled.
 * Detects toggles by comparing the active-module set each frame, so it needs no event hooks.
 */
public class NotificationElement extends KElement {
    public static final HudElementInfo<NotificationElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-notifications", "Module toggle notifications.", NotificationElement::new);

    public enum Corner { TopLeft, TopRight, BottomLeft, BottomRight }
    public enum Severity { INFO, SUCCESS, WARN, ERROR }

    private static class Note {
        final String title, msg; final Severity sev; final long born = System.currentTimeMillis();
        Note(String t, String m, Severity s) { title = t; msg = m; sev = s; }
    }

    private final SettingGroup sg = settings.createGroup("Notifications");

    private final Setting<Corner> corner = sg.add(new EnumSetting.Builder<Corner>()
        .name("corner").description("Which corner the stack grows from.").defaultValue(Corner.TopRight).build());
    private final Setting<Double> duration = sg.add(new DoubleSetting.Builder()
        .name("duration").description("Seconds a notification stays.").defaultValue(3).min(0.5).sliderRange(0.5, 10).build());
    private final Setting<Integer> max = sg.add(new IntSetting.Builder()
        .name("max-visible").description("Most notifications shown at once.").defaultValue(5).min(1).sliderRange(1, 10).build());
    private final Setting<Boolean> sound = sg.add(new BoolSetting.Builder()
        .name("sound").description("Play a click when one appears.").defaultValue(false).build());

    private final List<Note> notes = new ArrayList<>();
    private Set<Module> last = null;

    public NotificationElement() { super(INFO); }

    private void poll() {
        Set<Module> now = new HashSet<>(Modules.get().getActive());
        if (last != null) {
            for (Module m : now) if (!last.contains(m)) push(m.title, "ENABLED", Severity.SUCCESS);
            for (Module m : last) if (!now.contains(m)) push(m.title, "DISABLED", Severity.ERROR);
        }
        last = now; // first call only records the starting state, so no burst on load
    }

    private void push(String title, String msg, Severity sev) {
        notes.add(0, new Note(title.toUpperCase(), msg, sev));
        if (sound.get() && mc.getSoundManager() != null)
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    private Color sevColor(Severity s) {
        return switch (s) {
            case SUCCESS -> new Color(90, 220, 140, 255);
            case WARN -> new Color(240, 190, 70, 255);
            case ERROR -> new Color(240, 90, 90, 255);
            case INFO -> acc(0);
        };
    }

    @Override
    public void render(HudRenderer r) {
        long now = System.currentTimeMillis();
        long life = (long) (duration.get() * 1000);

        if (preview()) {
            notes.clear();
            notes.add(new Note("FULLBRIGHT", "ENABLED", Severity.SUCCESS));
            notes.add(new Note("SPRINT", "DISABLED", Severity.ERROR));
        } else {
            poll();
            notes.removeIf(n -> now - n.born > life);
            while (notes.size() > max.get()) notes.remove(notes.size() - 1);
        }

        double th = r.textHeight(false);
        double rowH = th * 2 + PAD * 2 + 1;
        double gap = 2;
        boolean right = corner.get() == Corner.TopRight || corner.get() == Corner.BottomRight;
        boolean bottom = corner.get() == Corner.BottomLeft || corner.get() == Corner.BottomRight;

        double w = 90;
        for (Note n : notes) w = Math.max(w, Math.max(r.textWidth(n.title, false), r.textWidth(n.msg, false)) + PAD * 2 + 3);
        int count = Math.max(1, notes.size());
        double total = count * rowH + (count - 1) * gap;
        setSize(w, total);

        int i = 0;
        for (Note n : notes) {
            long age = preview() ? 1000 : now - n.born;
            double in = Math.min(1, age / 200.0);
            double ease = 1 - Math.pow(1 - in, 3);
            double out = preview() ? 1 : Math.min(1, (life - age) / 300.0); // fade over the last 0.3s
            double a = ease * Math.max(0, out);

            double rw = Math.max(r.textWidth(n.title, false), r.textWidth(n.msg, false)) + PAD * 2 + 3;
            double slide = (1 - ease) * (right ? rw : -rw);
            double rx = (right ? x + w - rw : x) + slide;
            double ry = bottom ? y + total - (i + 1) * rowH - i * gap : y + i * (rowH + gap);

            Color c = sevColor(n.sev);
            panel(r, rx, ry, rw, rowH);
            r.quad(rx, ry, 2, rowH, fade(c, a));
            r.text(n.title, rx + PAD + 3, ry + PAD, fade(c, a), false);
            r.text(n.msg, rx + PAD + 3, ry + PAD + th + 1, fade(TEXT, a), false);
            i++;
        }
    }
}
