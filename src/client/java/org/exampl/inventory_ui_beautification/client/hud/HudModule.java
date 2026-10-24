package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.GameType;

import java.util.Locale;

public abstract class HudModule {

    protected static final Minecraft MC = Minecraft.getInstance();

    protected static int ticks;

    protected final String id;

    protected HudModule(String id) {
        this.id = id;
    }

    public static void onClientTick() {
        ticks++;
    }

    protected HudConfig.Entry cfg() {
        return HudConfig.of(id);
    }

    protected boolean isEnabled() {
        return cfg().enabled && !isSpectator();
    }

    protected boolean isSpectator() {
        return MC.gameMode != null && MC.gameMode.getPlayerMode() == GameType.SPECTATOR;
    }

    protected boolean isHiddenMode() {
        if (MC.gameMode == null) return true;
        GameType mode = MC.gameMode.getPlayerMode();
        return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
    }

    protected int[] pos(int w, int h) {
        HudConfig.Entry e = cfg();
        int sw = MC.getWindow().getGuiScaledWidth();
        int sh = MC.getWindow().getGuiScaledHeight();
        switch (e.anchor) {
            case 1:
                return new int[]{sw - w - e.dx, e.dy};
            case 2:
                return new int[]{e.dx, sh - h - e.dy};
            case 3:
                return new int[]{sw / 2 - w / 2 + e.dx, sh - h + e.dy};
            case 4:
                return new int[]{sw / 2 - w / 2 + e.dx, sh / 2 - h / 2 + e.dy};
            default:
                return new int[]{e.dx, e.dy};
        }
    }

    protected abstract int width();

    protected abstract int height();

    public abstract void render(GuiGraphics graphics, DeltaTracker tracker);

    protected static float approach(float cur, float target, float step) {
        if (cur < target) return Math.min(target, cur + step);
        return Math.max(target, cur - step);
    }

    protected static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    protected static String fmt(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format(Locale.ROOT, "%.1f", v);
    }

    protected static String fmt(double v) {
        return fmt((float) v);
    }

    public static int lerp(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int healthGradient(float t) {
        if (t <= 0.5f) return lerp(0xFFFF4040, 0xFFFFC040, t * 2f);
        return lerp(0xFFFFC040, 0xFF40E060, (t - 0.5f) * 2f);
    }

    public static int xpGradient(float t) {
        if (t <= 0.5f) return lerp(0xFF30D0FF, 0xFFA060FF, t * 2f);
        return lerp(0xFFA060FF, 0xFFFF60C0, (t - 0.5f) * 2f);
    }
}
