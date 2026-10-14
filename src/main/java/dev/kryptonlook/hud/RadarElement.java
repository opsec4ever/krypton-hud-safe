package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Square radar, rotates with your view (up = where you look). */
public class RadarElement extends KElement {
    public static final HudElementInfo<RadarElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-radar", "Nearby players on a radar.", RadarElement::new);

    private final Setting<Double> size = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("size").description("Radar size in pixels.").defaultValue(90).min(40).sliderRange(40, 200).build());
    private final Setting<Double> range = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("range").description("Detection range in blocks.").defaultValue(32).min(8).sliderRange(8, 128).build());
    private final Setting<Boolean> playersOnly = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("players-only").description("Only show players.").defaultValue(true).build());

    public RadarElement() { super(INFO); }

    @Override
    public void render(HudRenderer r) {
        double s = size.get();
        setSize(s, s);
        panel(r, x, y, s, s);

        Color a = acc(0);
        r.quad(x, y, s, 1, a);                       // top accent line
        double cx = x + s / 2, cy = y + s / 2;
        r.quad(cx - s / 2, cy, s, 1, new Color(255, 255, 255, 25));   // crosshair
        r.quad(cx, cy - s / 2, 1, s, new Color(255, 255, 255, 25));
        r.quad(cx - 1.5, cy - 1.5, 3, 3, a);          // you

        if (preview()) return;

        double yaw = Math.toRadians(mc.player.getYaw());
        double sin = Math.sin(yaw), cos = Math.cos(yaw);
        double half = s / 2 - 3, rng = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            boolean player = e instanceof PlayerEntity;
            if (playersOnly.get() ? !player : !(e instanceof LivingEntity)) continue;

            double dx = e.getX() - mc.player.getX();
            double dz = e.getZ() - mc.player.getZ();
            double sx = -dx * cos - dz * sin;   // screen right
            double sy = dx * sin - dz * cos;    // screen down
            if (Math.hypot(sx, sy) > rng) continue;

            double px = cx + sx / rng * half;
            double py = cy + sy / rng * half;
            r.quad(px - 1.5, py - 1.5, 3, 3, player ? new Color(255, 70, 70, 255) : new Color(230, 230, 230, 255));
        }
    }
}
