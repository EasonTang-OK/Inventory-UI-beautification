package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class HealthHud extends HudModule {

    public static final HealthHud INSTANCE = new HealthHud();

    private float ghost;
    private long ghostHoldUntil;

    private HealthHud() {
        super("health");
    }

    @Override
    protected int width() {
        return 116;
    }

    @Override
    protected int height() {
        return 26;
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker tracker) {
        if (!isEnabled() || isHiddenMode()) return;
        Player p = MC.player;
        if (p == null) return;
        float max = Math.max(1, p.getMaxHealth());
        float hp = p.getHealth();
        float abs = p.getAbsorptionAmount();
        float total = max + abs;
        float target = hp + abs;

        if (target < ghost - 0.001f) {
            if (ghostHoldUntil == 0) ghostHoldUntil = System.currentTimeMillis() + 600;
        } else {
            ghost = target;
            ghostHoldUntil = 0;
        }
        if (ghostHoldUntil != 0 && System.currentTimeMillis() > ghostHoldUntil) {
            ghost = Math.max(target, ghost - tracker.getGameTimeDeltaTicks() * max * 0.06f);
            if (ghost <= target + 0.01f) ghostHoldUntil = 0;
        }

        int[] xy = pos(width(), height());
        int x = xy[0];
        int y = xy[1];
        int w = width() - 8;
        int h = 8;

        boolean regen = p.hasEffect(MobEffects.REGENERATION);
        boolean poison = p.hasEffect(MobEffects.POISON);
        boolean wither = p.hasEffect(MobEffects.WITHER);
        float pulse = regen ? 1.0f + 0.22f * (float) Math.sin(ticks * 0.35f) : 1.0f;

        int bx = x + 4;
        int by = y + (int) ((h - h * pulse) / 2.0f);
        int bh = Math.max(2, (int) (h * pulse));

        g.fill(bx - 1, by - 1, bx + w + 1, by + bh + 1, 0xE6101620);
        g.fill(bx, by, bx + w, by + bh, 0x55000000);

        int ghostW = (int) (w * Math.min(1, ghost / total));
        if (ghostW > 0) g.fill(bx, by, bx + ghostW, by + bh, 0xFFFFE060);

        int hpW = (int) (w * Math.min(1, hp / total));
        if (hpW > 0) {
            g.fill(bx, by, bx + hpW, by + bh, healthGradient(hp / max));
            if (poison) g.fill(bx, by, bx + hpW, by + bh, 0x5530C030);
            if (wither) g.fill(bx, by, bx + hpW, by + bh, 0x55252525);
        }

        int absW = (int) (w * Math.min(1, (hp + abs) / total)) - hpW;
        if (absW > 0) g.fill(bx + hpW, by, bx + hpW + absW, by + bh, 0xFFE0B030);

        if (p.hurtTime > 0) g.fill(bx, by, bx + Math.max(2, hpW), by + bh, 0x99FFFFFF);

        g.drawString(MC.font, "HP " + fmt(hp) + "/" + fmt(max), x + 4, y + 10, 0xFFFFFFFF, true);
        int armor = p.getArmorValue();
        if (armor > 0 || abs > 0) {
            String extra = "";
            if (armor > 0) extra = "护甲 " + armor;
            if (abs > 0) extra += (extra.isEmpty() ? "" : "  ") + "吸收 +" + fmt(abs);
            g.drawString(MC.font, extra, x + 4, y + 19, 0xFFE0B030, true);
        }
    }
}
