package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.TickRate;
import net.minecraft.client.network.PlayerListEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Top-left pills in two rows, like the reference: row 1 = watermark + info bar,
 * row 2 = coordinates, ping, ticks, bps. Pills are 26 px tall with 10 px side padding.
 */
public class StatusRowsElement extends KElement {
    public static final HudElementInfo<StatusRowsElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-status-rows", "Watermark, info bar and stat pills.", StatusRowsElement::new);

    private static final double H = 26, GAP = 4, ROW2_Y = 34, PADX = 10, SEG_GAP = 14;
    private static final SimpleDateFormat CLOCK = new SimpleDateFormat("HH:mm:ss");

    private final SettingGroup sg = settings.createGroup("Pills");
    private final Setting<Boolean> watermark = bool("watermark", true);
    private final Setting<String> watermarkText = sg.add(new StringSetting.Builder()
        .name("watermark-text").description("Text in the watermark pill.").defaultValue("KRYPTON+").build());
    private final Setting<Boolean> infoBar = bool("info-bar", true);
    private final Setting<Boolean> playerName = bool("player-name", true);
    private final Setting<Boolean> fps = bool("fps", true);
    private final Setting<Boolean> time = bool("time", true);
    private final Setting<Boolean> coordinates = bool("coordinates", true);
    private final Setting<Boolean> ping = bool("ping", true);
    private final Setting<Boolean> tps = bool("tps", true);
    private final Setting<Boolean> bps = bool("bps", true);

    private long lastSampleNs = 0;
    private double lastX, lastZ, blocksPerSecond = 0;

    public StatusRowsElement() { super(INFO); }

    private Setting<Boolean> bool(String name, boolean def) {
        return sg.add(new BoolSetting.Builder().name(name).description("Show " + name + ".").defaultValue(def).build());
    }

    private record Pill(List<String> segments, boolean accentText) {}

    private void sampleBps() {
        if (preview()) return;
        long now = System.nanoTime();
        if (lastSampleNs == 0) {
            lastSampleNs = now; lastX = mc.player.getX(); lastZ = mc.player.getZ();
            return;
        }
        double elapsed = (now - lastSampleNs) / 1e9;
        if (elapsed >= 0.05) {
            blocksPerSecond = Math.hypot(mc.player.getX() - lastX, mc.player.getZ() - lastZ) / elapsed;
            lastX = mc.player.getX(); lastZ = mc.player.getZ(); lastSampleNs = now;
        }
    }

    private int latency() {
        if (preview() || mc.getNetworkHandler() == null) return 0;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e == null ? 0 : e.getLatency();
    }

    @Override
    public void render(HudRenderer r) {
        tickDt();
        sampleBps();

        List<Pill> row1 = new ArrayList<>();
        List<Pill> row2 = new ArrayList<>();
        boolean pv = preview();

        if (watermark.get()) row1.add(new Pill(List.of(watermarkText.get()), true));

        if (infoBar.get()) {
            List<String> segs = new ArrayList<>();
            if (playerName.get()) segs.add(pv ? "Player" : mc.player.getName().getString());
            if (fps.get()) segs.add(mc.getCurrentFps() + " Fps");
            if (time.get()) segs.add(CLOCK.format(new Date()));
            if (!segs.isEmpty()) row1.add(new Pill(segs, false));
        }

        if (coordinates.get())
            row2.add(new Pill(List.of(pv ? "0 | 64 | 0" : String.format("%.0f | %.0f | %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ())), false));
        if (ping.get()) row2.add(new Pill(List.of(latency() + " Ping"), false));
        if (tps.get()) row2.add(new Pill(List.of(String.format("%.1f Ticks", pv ? 20.0 : TickRate.INSTANCE.getTickRate())), false));
        if (bps.get()) row2.add(new Pill(List.of(String.format("%.1f Bps", blocksPerSecond)), false));

        double w1 = rowWidth(r, row1), w2 = rowWidth(r, row2);
        double row2y = row1.isEmpty() ? 0 : ROW2_Y;
        double height = row2.isEmpty() ? (row1.isEmpty() ? H : H) : row2y + H;
        setSize(Math.max(Math.max(w1, w2), 40), height);

        drawRow(r, row1, 0);
        drawRow(r, row2, row2y);
    }

    private double pillWidth(HudRenderer r, Pill p) {
        double w = PADX * 2;
        for (String s : p.segments()) w += r.textWidth(s, false);
        w += (p.segments().size() - 1) * SEG_GAP;
        return w;
    }

    private double rowWidth(HudRenderer r, List<Pill> row) {
        double w = 0;
        for (Pill p : row) w += pillWidth(r, p) + GAP;
        return row.isEmpty() ? 0 : w - GAP;
    }

    private void drawRow(HudRenderer r, List<Pill> row, double yOff) {
        Color fill = new Color(8, 18, 60, alpha());
        double th = r.textHeight(false);
        double px = x;
        int idx = 0;
        for (Pill p : row) {
            double w = pillWidth(r, p);
            double py = y + yOff;
            rrect(r, px, py, w, H, 3, fill);

            double tx = px + PADX;
            double ty = py + (H - th) / 2;
            for (int i = 0; i < p.segments().size(); i++) {
                String s = p.segments().get(i);
                if (i > 0) {
                    r.quad(tx + 6, py + 4, 2, H - 8, SEPARATOR);
                    tx += SEG_GAP;
                }
                r.text(s, tx, ty, p.accentText() ? accent(idx) : SECONDARY, false);
                tx += r.textWidth(s, false);
            }
            px += w + GAP;
            idx++;
        }
    }
}
