package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.*;

/** Lowercase module names in rounded rows with a 3-step accent bar on the right edge; rows slide in. */
public class ModuleListElement extends KElement {
    public static final HudElementInfo<ModuleListElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-module-list", "Enabled modules, right-aligned.", ModuleListElement::new);

    public enum SortMode { Length, Alphabetical, Category }

    private static final double PAD = 8, ROW_H = 20, STEP = 22, SPEED = 10;

    private final Setting<SortMode> sort = settings.getDefaultGroup().add(new EnumSetting.Builder<SortMode>()
        .name("sort-mode").description("How the list is ordered.").defaultValue(SortMode.Length).build());

    private static class Anim { double slide = 0, alpha = 0; }
    private record Row(String label, String category) {}

    private final Map<String, Anim> anims = new HashMap<>();

    public ModuleListElement() { super(INFO); }

    @Override
    public void render(HudRenderer r) {
        tickDt();

        List<Row> rows = new ArrayList<>();
        if (preview()) {
            rows.add(new Row("fullbright", "Render"));
            rows.add(new Row("name protect", "Misc"));
            rows.add(new Row("sprint", "Movement"));
        } else {
            for (Module m : Modules.get().getActive()) rows.add(new Row(m.title.toLowerCase(), m.category.name));
        }

        switch (sort.get()) {
            case Length -> rows.sort(Comparator.comparingDouble((Row x) -> r.textWidth(x.label(), false)).reversed());
            case Alphabetical -> rows.sort(Comparator.comparing(Row::label));
            case Category -> rows.sort(Comparator.comparing(Row::category).thenComparing(Row::label));
        }

        Set<String> live = new HashSet<>();
        for (Row row : rows) {
            live.add(row.label());
            Anim a = anims.computeIfAbsent(row.label(), k -> new Anim());
            a.slide = lerp(a.slide, 1, SPEED, dt);
            a.alpha = lerp(a.alpha, 1, SPEED, dt);
        }
        anims.keySet().retainAll(live);

        double widest = 0;
        for (Row row : rows) widest = Math.max(widest, r.textWidth(row.label(), false) + PAD * 2);
        setSize(Math.max(widest, 40), rows.isEmpty() ? ROW_H : rows.size() * STEP);

        double th = r.textHeight(false);
        double right = x + getWidth();
        double rowY = y;
        int i = 0;
        for (Row row : rows) {
            Anim a = anims.get(row.label());
            double labelW = r.textWidth(row.label(), false);
            double offset = (getWidth() + 10) * (1 - a.slide);
            double slidRight = right + offset;
            double slidLeft = slidRight - labelW - PAD * 2;

            rrect(r, slidLeft, rowY, slidRight - slidLeft, ROW_H, 3,
                new Color(30, 30, 40, (int) (alpha() * a.alpha)));

            Color acc = accent(i);
            for (int bar = 0; bar < 3; bar++) {
                r.quad(slidRight - 3 + bar, rowY, 1, ROW_H, withAlpha(acc, (int) ((255 - bar * 60) * a.alpha)));
            }
            Color text = rainbow.get() ? accent(i) : PRIMARY;
            r.text(row.label(), slidLeft + PAD, rowY + (ROW_H - th) / 2, fade(text, a.alpha), false);
            rowY += STEP;
            i++;
        }
    }
}
