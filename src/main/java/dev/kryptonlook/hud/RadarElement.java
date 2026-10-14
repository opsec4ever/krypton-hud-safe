package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Round radar: accent ring, dark disc, crosshair, two faint inner rings, N/E/S/W letters that rotate
 * with your view, player blips with a halo, and a view cone at the top. Up = where you look.
 */
public class RadarElement extends KElement {
    public static final HudElementInfo<RadarElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-radar", "Nearby players on a radar.", RadarElement::new);

    private final Setting<Double> size = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("size").description("Radar radius in pixels.").defaultValue(120).min(60).max(200).sliderRange(60, 200).build());
    private final Setting<Double> range = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("range").description("Detection range in blocks.").defaultValue(64).min(16).max(128).sliderRange(16, 128).build());
    private final Setting<Boolean> players = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("players").description("Show players.").defaultValue(true).build());

    private final Map<UUID, double[]> blips = new HashMap<>();

    public RadarElement() { super(INFO); }

    private void letter(HudRenderer r, String s, double cx, double cy, double angle, double dist, Color c) {
        double lx = cx + Math.sin(angle) * dist;
        double ly = cy + Math.cos(angle) * dist;
        r.text(s, lx - r.textWidth(s, false) / 2, ly - r.textHeight(false) / 2, c, false);
    }

    @Override
    public void render(HudRenderer r) {
        double R = size.get();
        setSize(R * 2, R * 2);
        double cx = x + R, cy = y + R;
        Color acc = accent(0);
        Color clear = new Color(0, 0, 0, 0);

        circle(r, cx, cy, R + 1, withAlpha(acc, 100));                                       // ring
        circle(r, cx, cy, R, new Color(20, 20, 30, (int) (opacity.get() * 100)));            // disc

        double reach = R * 0.85;
        Color cross = new Color(255, 255, 255, 50);
        r.quad(cx - reach, cy, reach * 2, 1, cross);
        r.quad(cx, cy - reach, 1, reach * 2, cross);

        for (int i = 1; i <= 2; i++) {
            circle(r, cx, cy, (R - 10) * i / 2.0, new Color(255, 255, 255, 30));
        }

        double yaw = preview() ? 0 : Math.toRadians(mc.player.getYaw());
        double letterDist = R - 14;
        Color white = new Color(255, 255, 255, 100);
        letter(r, "N", cx, cy, yaw, letterDist, withAlpha(acc, 200));
        letter(r, "S", cx, cy, yaw + Math.PI, letterDist, white);
        letter(r, "E", cx, cy, yaw - Math.PI / 2, letterDist, white);
        letter(r, "W", cx, cy, yaw + Math.PI / 2, letterDist, white);

        Set<UUID> seen = new HashSet<>();
        if (!preview() && players.get()) {
            double rng = range.get();
            double scale = (R - 15) / rng;
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player) continue;
                double dx = p.getX() - mc.player.getX();
                double dz = p.getZ() - mc.player.getZ();
                if (Math.sqrt(dx * dx + dz * dz) > rng) continue;

                double rotX = dx * Math.cos(yaw) + dz * Math.sin(yaw);
                double rotZ = dz * Math.cos(yaw) - dx * Math.sin(yaw);
                double tx = cx - rotX * scale, ty = cy - rotZ * scale;

                UUID id = p.getUuid();
                seen.add(id);
                double[] prev = blips.get(id);
                double bx = prev == null ? tx : prev[0] * 0.85 + tx * 0.15;
                double by = prev == null ? ty : prev[1] * 0.85 + ty * 0.15;
                blips.put(id, new double[]{bx, by});

                if (Math.hypot(bx - cx, by - cy) <= R - 15) {
                    double half = 3.5;
                    circle(r, bx, by, half + 2, withAlpha(acc, 70));
                    rrect(r, bx - half, by - half, half * 2, half * 2, 2, new Color(10, 10, 15, 220));
                    r.quad(bx - 2, by - 2, 4, 4, acc);   // stand-in for the player head
                }
            }
        }
        blips.keySet().retainAll(seen);

        // you
        circle(r, cx, cy, 6, withAlpha(acc, 120));
        circle(r, cx, cy, 4, acc);

        // view cone at the top
        double coneHalf = 6, coneBottom = cy - R + 11;
        rrect(r, cx - coneHalf - 1, coneBottom - coneHalf - 2, coneHalf * 2 + 3, coneHalf + 4, 4, withAlpha(acc, 40));
        for (int i = 0; i < coneHalf; i++) {
            r.quad(cx - i, coneBottom - coneHalf + i, i * 2 + 1, 1, acc);
        }
    }
}
