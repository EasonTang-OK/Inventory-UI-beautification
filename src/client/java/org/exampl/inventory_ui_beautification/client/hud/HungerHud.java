package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.food.FoodData;

public final class HungerHud extends HudModule {

    public static final HungerHud INSTANCE = new HungerHud();

    private int lastFood = 20;
    private long eatFlashUntil;

    private HungerHud() {
        super("hunger");
    }

    @Override
    protected int width() {
        return 116;
    }

    @Override
    protected int height() {
        return 20;
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker tracker) {
        if (!isEnabled() || isHiddenMode()) return;
        if (MC.player == null) return;
        FoodData fd = MC.player.getFoodData();
        int food = fd.getFoodLevel();
        float sat = fd.getSaturationLevel();
        long now = System.currentTimeMillis();
        if (food != lastFood) {
            if (food > lastFood) eatFlashUntil = now + 400;
            lastFood = food;
        }

        int[] xy = pos(width(), height());
        int x = xy[0];
        int y = xy[1];
        int w = width() - 8;
        int h = 5;

        boolean starving = food <= 0;
        float pulse = starving ? 0.6f + 0.4f * (float) Math.abs(Math.sin(ticks * 0.15f)) : 1.0f;

        int bx = x + 4;
        int by = y + (int) ((h - h * pulse) / 2.0f);
        int bh = Math.max(2, (int) (h * pulse));

        g.fill(bx - 1, by - 1, bx + w + 1, by + bh + 1, 0xE6101620);
        g.fill(bx, by, bx + w, by + bh, 0x55000000);

        int fw = (int) (w * food / 20f);
        if (fw > 0) {
            int col = starving ? 0xFFD03030 : (food < 6 ? 0xFFE0C030 : 0xFF50C860);
            g.fill(bx, by, bx + fw, by + bh, col);
        }
        int sw = (int) (w * Math.min(sat, food) / 20f);
        if (sw > 0) g.fill(bx, by, bx + sw, by + bh, 0x55FFFFFF);
        if (now < eatFlashUntil) g.fill(bx, by, bx + Math.max(2, fw), by + bh, 0x99FFFFFF);

        String label = starving ? "饥饿! " + food + "/20" : "饱食 " + food + "/20";
        int col = starving ? (now % 500 < 250 ? 0xFFFF5050 : 0xFFFF9090) : 0xFFFFFFFF;
        g.drawString(MC.font, label, x + 4, y + 8, col, true);
        g.drawString(MC.font, "饱和 " + fmt(sat), x + 6 + MC.font.width(label), y + 8, 0xFFC0C0C0, true);
    }
}
