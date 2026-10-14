package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.world.TickRate;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Ping, TPS and speed (blocks per second). */
public class StatsElement extends KElement {
    public static final HudElementInfo<StatsElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-stats", "Ping, TPS and BPS.", StatsElement::new);

    private final Setting<Boolean> ping = bool("ping");
    private final Setting<Boolean> tps = bool("tps");
    private final Setting<Boolean> bps = bool("bps");

    public StatsElement() { super(INFO); }

    private Setting<Boolean> bool(String name) {
        return settings.getDefaultGroup().add(new BoolSetting.Builder()
            .name(name).description("Show " + name + ".").defaultValue(true).build());
    }

    @Override
    public void render(HudRenderer renderer) {
        List<String> lines = new ArrayList<>();

        if (ping.get()) {
            int p = 0;
            if (!preview() && mc.getNetworkHandler() != null) {
                PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (e != null) p = e.getLatency();
            }
            lines.add("PING  " + p + " MS");
        }
        if (tps.get()) lines.add(String.format("TPS   %.1f", preview() ? 20.0 : TickRate.INSTANCE.getTickRate()));
        if (bps.get()) {
            double v = 0;
            if (!preview()) {
                var vel = mc.player.getVelocity();
                v = Math.hypot(vel.x, vel.z) * 20;
            }
            lines.add(String.format("BPS   %.1f", v));
        }
        if (lines.isEmpty()) lines.add("STATS");
        textPanel(renderer, null, lines);
    }
}
