package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.*;

/** Enabled-module list, right-aligned, accent bar on the right edge, new entries slide in. */
public class ModuleListElement extends KElement {
    public static final HudElementInfo<ModuleListElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-module-list", "Enabled modules, Krypton style.", ModuleListElement::new);

    public enum SortMode { Length, Alphabetical, Category }

    private final Setting<SortMode> sort = settings.getDefaultGroup().add(new EnumSetting.Builder<SortMode>()
        .name("sort-mode").description("How the list is ordered.").defaultValue(SortMode.Length).build());

    private record Row(String name, String category) {}

    private final Map<String, Long> born = new HashMap<>();

    public ModuleListElement() { super(INFO); }

    @Override
    public void render(HudRenderer r) {
        List<Row> rows = new ArrayList<>();
        if (preview()) {
            rows.add(new Row("FULLBRIGHT", "Render"));
            rows.add(new Row("NAME PROTECT", "Misc"));
            rows.add(new Row("SPRINT", "Misc"));
        } else {
            for (Module m : Modules.get().getActive()) rows.add(new Row(m.title.toUpperCase(), m.category.name));
        }
        if (rows.isEmpty()) rows.add(new Row("NO MODULES", ""));

        switch (sort.get()) {
            case Length -> rows.sort(Comparator.comparingDouble((Row x) -> r.textWidth(x.name(), false)).reversed());
            case Alphabetical -> rows.sort(Comparator.comparing(Row::name));
            case Category -> rows.sort(Comparator.comparing(Row::category).thenComparing(Row::name));
        }

        long now = System.currentTimeMillis();
        Set<String> present = new HashSet<>();
        for (Row row : rows) { present.add(row.name()); born.putIfAbsent(row.name(), now); }
        born.keySet().retainAll(present);

        double th = r.textHeight(false);
        double rowH = th + PAD * 2 - 2;
        double maxW = 0;
        for (Row row : rows) maxW = Math.max(maxW, r.textWidth(row.name(), false));
        double w = maxW + PAD * 2 + 3;

        setSize(w, rowH * rows.size());

        double ry = y;
        int i = 0;
        for (Row row : rows) {
            double rw = r.textWidth(row.name(), false) + PAD * 2 + 3;
            double t = Math.min(1, (now - born.get(row.name())) / 220.0);
            double ease = 1 - Math.pow(1 - t, 3);
            double rx = x + w - rw + (1 - ease) * (rw + 4);

            panel(r, rx, ry, rw, rowH);
            r.quad(rx + rw - 2, ry, 2, rowH, fade(acc(i * 0.06), ease));
            r.text(row.name(), rx + PAD, ry + (rowH - th) / 2, fade(TEXT, ease), false);
            ry += rowH;
            i++;
        }
    }
}
