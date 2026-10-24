package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EffectHud extends HudModule {

    public static final EffectHud INSTANCE = new EffectHud();

    private EffectHud() {
        super("effects");
    }

    @Override
    protected int width() {
        return 120;
    }

    @Override
    protected int height() {
        return 22;
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker tracker) {
        if (!isEnabled()) return;
        Player p = MC.player;
        if (p == null) return;
        List<MobEffectInstance> effects = new ArrayList<>(p.getActiveEffects());
        if (effects.isEmpty()) return;

        int maxText = 0;
        for (MobEffectInstance inst : effects) {
            int tw = MC.font.width(nameOf(inst) + " " + timeOf(inst));
            if (tw > maxText) maxText = tw;
        }
        int mw = 21 + Math.min(maxText, 140);
        int mh = effects.size() * 20 + 2;
        int[] xy = pos(mw, mh);
        int x = xy[0];
        int y = xy[1] + 1;

        for (MobEffectInstance inst : effects) {
            Holder<MobEffect> holder = inst.getEffect();
            MobEffect eff = holder.value();
            Identifier sprite = Gui.getMobEffectSprite(holder);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18);
            String name = nameOf(inst);
            String time = timeOf(inst);
            boolean infinite = inst.isInfiniteDuration();
            int secs = infinite ? Integer.MAX_VALUE : (inst.getDuration() + 19) / 20;
            int tcol = infinite ? 0xFFE0E0FF : secs >= 600 ? 0xFFFFFFFF : secs >= 300 ? 0xFFFFFF60 : 0xFFFF6060;
            int col = eff.isBeneficial() ? 0xFFD0FFD0 : 0xFFFFB0B0;
            g.drawString(MC.font, name, x + 21, y + 5, col, true);
            g.drawString(MC.font, time, x + 21 + MC.font.width(name) + 3, y + 5, tcol, true);
            y += 20;
        }
    }

    private static String nameOf(MobEffectInstance inst) {
        String name = inst.getEffect().value().getDisplayName().getString();
        if (inst.getAmplifier() > 0) name += " " + roman(inst.getAmplifier() + 1);
        return name;
    }

    private static String timeOf(MobEffectInstance inst) {
        return inst.isInfiniteDuration() ? "∞" : fmtDuration(inst.getDuration());
    }

    private static String fmtDuration(int ticks) {
        int sec = (ticks + 19) / 20;
        if (sec >= 3600) return String.format(Locale.ROOT, "%d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
        return String.format(Locale.ROOT, "%d:%02d", sec / 60, sec % 60);
    }

    private static String roman(int n) {
        return new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"}[Math.min(n - 1, 9)];
    }
}
