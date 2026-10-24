package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public final class CombatHud extends HudModule {

    public static final CombatHud INSTANCE = new CombatHud();

    static int combo;
    static int killStreak;
    static long lastHitAt;
    static long markerUntil;
    static long killMarkerUntil;
    static long comboPopUntil;
    static float vignette;

    private CombatHud() {
        super("combat");
    }

    @Override
    protected int width() {
        return 120;
    }

    @Override
    protected int height() {
        return 26;
    }

    static void onHit() {
        long now = System.currentTimeMillis();
        combo = (now - lastHitAt <= 3000) ? combo + 1 : 1;
        lastHitAt = now;
        markerUntil = now + 130;
        comboPopUntil = now + 250;
    }

    static void onKill() {
        killStreak++;
        killMarkerUntil = System.currentTimeMillis() + 600;
    }

    static void onPlayerHurt() {
        vignette = Math.max(vignette, 0.5f);
        combo = 0;
    }

    public static void tick() {
        if (vignette > 0) vignette = Math.max(0, vignette - 0.03f);
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker tracker) {
        if (!isEnabled()) return;
        long now = System.currentTimeMillis();
        int sw = MC.getWindow().getGuiScaledWidth();
        int sh = MC.getWindow().getGuiScaledHeight();

        if (vignette > 0.01f) {
            int a = (int) (vignette * 255);
            int col = (a << 24) | 0x00FF2020;
            int t = 26;
            g.fill(0, 0, sw, t, col);
            g.fill(0, sh - t, sw, sh, col);
            g.fill(0, t, t, sh - t, col);
            g.fill(sw - t, t, sw, sh - t, col);
        }

        if (now < markerUntil) {
            int cx = sw / 2;
            int cy = sh / 2;
            int s = 6;
            int e = (now - markerUntil > -60) ? 13 : 10;
            g.fill(cx - e, cy - e, cx - s, cy - s, 0xFFFFFFFF);
            g.fill(cx + s, cy - e, cx + e, cy - s, 0xFFFFFFFF);
            g.fill(cx - e, cy + s, cx - s, cy + e, 0xFFFFFFFF);
            g.fill(cx + s, cy + s, cx + e, cy + e, 0xFFFFFFFF);
        }
        if (now < killMarkerUntil) {
            int cx = sw / 2;
            int cy = sh / 2;
            int s = 5;
            int e = 11;
            g.fill(cx - e, cy - e, cx - s, cy - s, 0xFFFFD000);
            g.fill(cx + s, cy - e, cx + e, cy - s, 0xFFFFD000);
            g.fill(cx - e, cy + s, cx - s, cy + e, 0xFFFFD000);
            g.fill(cx + s, cy + s, cx + e, cy + e, 0xFFFFD000);
        }

        int[] xy = pos(width(), height());
        int cx = xy[0] + width() / 2;
        int cy = xy[1];
        if (combo > 1) {
            String text = "连击 x" + combo;
            int col = now < comboPopUntil ? 0xFFFFFFFF : 0xCCFFFFFF;
            g.drawString(MC.font, text, cx - MC.font.width(text) / 2, cy, col, true);
        }
        if (killStreak > 1) {
            String text = "击杀 x" + killStreak;
            g.drawString(MC.font, text, cx - MC.font.width(text) / 2, cy + 10, 0xFFFFD000, true);
        }
    }
}
