package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class TargetHud extends HudModule {

    public static final TargetHud INSTANCE = new TargetHud();

    private TargetHud() {
        super("target");
    }

    @Override
    protected int width() {
        return 120;
    }

    @Override
    protected int height() {
        return 34;
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker tracker) {
        if (!isEnabled()) return;
        HitResult hit = MC.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;
        if (!(hit instanceof EntityHitResult ehr)) return;
        Entity e = ehr.getEntity();
        if (!(e instanceof LivingEntity le) || e == MC.player) return;

        int[] xy = pos(width(), height());
        int x = xy[0];
        int y = xy[1];
        int w = width() - 20;

        String name = le.getDisplayName().getString();
        if (MC.font.width(name) > w) {
            name = MC.font.plainSubstrByWidth(name, w - 6) + "...";
        }
        g.drawString(MC.font, name, x + 10 + (w - MC.font.width(name)) / 2, y, 0xFFFFFFFF, true);

        float max = Math.max(1, le.getMaxHealth());
        float ratio = clamp01(le.getHealth() / max);
        int bx = x + 10;
        int by = y + 12;
        int bh = 5;
        g.fill(bx - 1, by - 1, bx + w + 1, by + bh + 1, 0xE6101620);
        g.fill(bx, by, bx + w, by + bh, 0x55000000);
        int fw = (int) (w * ratio);
        if (fw > 0) g.fill(bx, by, bx + fw, by + bh, healthGradient(ratio));

        String hp = "血量 " + fmt(le.getHealth()) + "/" + fmt(max);
        g.drawString(MC.font, hp, x + 10 + (w - MC.font.width(hp)) / 2, y + 21, 0xFFFFFFFF, true);
    }
}
