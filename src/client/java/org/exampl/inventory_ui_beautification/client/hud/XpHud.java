package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class XpHud extends HudModule {

    public static final XpHud INSTANCE = new XpHud();

    private float display;
    private int lastLevel = -1;
    private long levelUpUntil;

    private XpHud() {
        super("xp");
    }

    @Override
    protected int width() {
        return 150;
    }

    @Override
    protected int height() {
        return 18;
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker tracker) {
        if (!isEnabled() || isHiddenMode()) return;
        Player p = MC.player;
        if (p == null) return;
        float prog = p.experienceProgress;
        int level = p.experienceLevel;
        long now = System.currentTimeMillis();
        if (lastLevel >= 0 && level > lastLevel) levelUpUntil = now + 1200;
        lastLevel = level;

        if (prog < display - 0.02f) display = prog;
        display = approach(display, prog, tracker.getGameTimeDeltaTicks() * 0.045f);

        int[] xy = pos(width(), height());
        int x = xy[0];
        int y = xy[1];
        int w = width() - 20;
        int h = 5;
        int bx = x + 10;
        int by = y + 11;

        g.fill(bx - 1, by - 1, bx + w + 1, by + h + 1, 0xE6101620);
        g.fill(bx, by, bx + w, by + h, 0x55000000);

        float d = clamp01(display);
        int fw = (int) (w * d);
        if (fw > 0) {
            g.fill(bx, by, bx + fw, by + h, xpGradient(d));
            float sh = (ticks * 0.5f) % (w + 40) - 20;
            int sx = (int) Math.max(bx, bx + sh);
            int ex = (int) Math.min(bx + fw, bx + sh + 14);
            if (sx < ex) g.fill(sx, by, ex, by + h, 0x44FFFFFF);
        }

        boolean flash = now < levelUpUntil;
        if (flash) g.fill(bx, by, bx + fw, by + h, 0x55FFD000);

        g.drawString(MC.font, "Lv " + level, x + 2, y, flash ? 0xFFFFD000 : 0xFFFFFFFF, true);
        String pct = (int) (prog * 100) + "%";
        g.drawString(MC.font, pct, x + width() - MC.font.width(pct) - 2, y, 0xFFA0E0FF, true);
    }
}
