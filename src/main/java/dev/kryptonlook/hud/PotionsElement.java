package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** "Active potions" panel: 28 px header, then one 22 px row per effect with the duration on the right. */
public class PotionsElement extends KElement {
    public static final HudElementInfo<PotionsElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-potions", "Active potion effects.", PotionsElement::new);

    private static final double ROW_H = 22, PAD = 8, RIGHT_PAD = 14, NAME_GAP = 14, HEADER_H = 28;
    private static final String TITLE = "Active potions";
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    public PotionsElement() { super(INFO); }

    private record Entry(String name, String duration) {}

    private static String roman(int v) { return v >= 1 && v <= 10 ? ROMAN[v - 1] : String.valueOf(v); }

    @Override
    public void render(HudRenderer r) {
        List<Entry> entries = new ArrayList<>();
        if (preview()) {
            entries.add(new Entry("Speed II", "1:30"));
            entries.add(new Entry("Strength", "0:45"));
        } else {
            List<StatusEffectInstance> list = new ArrayList<>(mc.player.getStatusEffects());
            list.sort(Comparator.comparing(e -> e.getEffectType().value().getTranslationKey()));
            for (StatusEffectInstance e : list) {
                String name = e.getEffectType().value().getName().getString();
                if (e.getAmplifier() > 0) name += " " + roman(e.getAmplifier() + 1);
                String dur;
                if (e.isInfinite()) dur = "**:**";
                else dur = (e.getDuration() / 1200) + ":" + String.format("%02d", e.getDuration() % 1200 / 20);
                entries.add(new Entry(name, dur));
            }
        }

        double th = r.textHeight(false);
        double widest = 0;
        for (Entry e : entries) widest = Math.max(widest, r.textWidth(e.name(), false) + NAME_GAP + r.textWidth(e.duration(), false));
        double titleW = PAD + r.textWidth(TITLE, false) + PAD;
        double width = Math.max(widest + PAD + RIGHT_PAD, titleW);
        double total = HEADER_H + 3 + entries.size() * ROW_H + PAD;
        if (entries.isEmpty()) total = HEADER_H;
        setSize(width, total);

        Color panel = new Color(30, 30, 40, alpha());
        rrect(r, x, y, width, HEADER_H, 5, panel);
        r.text(TITLE, x + PAD, y + (HEADER_H - th) / 2, PRIMARY, false);
        if (entries.isEmpty()) return;

        double bodyY = y + HEADER_H + 3;
        rrect(r, x, bodyY, width, entries.size() * ROW_H + PAD, 5, panel);
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            double rowY = bodyY + PAD / 2 + i * ROW_H;
            double ty = rowY + (ROW_H - th) / 2;
            r.text(e.name(), x + PAD, ty, PRIMARY, false);
            double dw = r.textWidth(e.duration(), false);
            r.text(e.duration(), x + width - dw - RIGHT_PAD, ty, withAlpha(accent(i), 180), false);
        }
    }
}
