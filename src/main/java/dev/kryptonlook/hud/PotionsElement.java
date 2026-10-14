package dev.kryptonlook.hud;

import dev.kryptonlook.KryptonLookAddon;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PotionsElement extends KElement {
    public static final HudElementInfo<PotionsElement> INFO = new HudElementInfo<>(
        KryptonLookAddon.HUD_GROUP, "kl-potions", "Active potion effects.", PotionsElement::new);

    public PotionsElement() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        List<String> lines = new ArrayList<>();
        if (preview()) {
            lines.add("SPEED II  1:30");
        } else {
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString().toUpperCase();
                int secs = Math.max(0, e.getDuration() / 20);
                lines.add(name + " " + (e.getAmplifier() + 1) + "  " + (secs / 60) + ":" + String.format("%02d", secs % 60));
            }
        }
        if (lines.isEmpty()) lines.add("NO EFFECTS");
        textPanel(renderer, "POTIONS", lines);
    }
}
