package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class InfoHud extends HudModule {

    public static final InfoHud INSTANCE = new InfoHud();

    private record Line(String text, int color) {
    }

    private long fpsWindowStart;
    private int fpsFrames;
    private int fps = 60;

    private InfoHud() {
        super("info");
    }

    @Override
    protected int width() {
        return 140;
    }

    @Override
    protected int height() {
        return 46;
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker tracker) {
        if (!isEnabled()) return;
        Player p = MC.player;
        if (p == null) return;

        long now = System.nanoTime();
        fpsFrames++;
        if (now - fpsWindowStart >= 1_000_000_000L) {
            fps = fpsFrames;
            fpsFrames = 0;
            fpsWindowStart = now;
        }

        List<Line> lines = new ArrayList<>();
        lines.add(new Line("FPS " + fps, fps >= 60 ? 0xFF60E060 : (fps >= 30 ? 0xFFE0C030 : 0xFFFF5050)));
        lines.add(new Line("X " + fmt(p.getX()) + " Y " + fmt(p.getY()) + " Z " + fmt(p.getZ()), 0xFFFFFFFF));
        String[] dir = direction(p.getYRot());
        lines.add(new Line(dir[0] + " " + dir[1] + "°", 0xFFFFFFFF));
        ItemStack held = p.getMainHandItem();
        if (held.isDamageableItem()) {
            int left = held.getMaxDamage() - held.getDamageValue();
            int max = held.getMaxDamage();
            float r = max <= 0 ? 1 : (float) left / max;
            lines.add(new Line("耐久 " + left + "/" + max, r > 0.6f ? 0xFF60E060 : (r > 0.3f ? 0xFFE0C030 : 0xFFFF5050)));
        } else {
            lines.add(new Line("空手", 0xFFFFFFFF));
        }
        if (MC.getConnection() != null) {
            PlayerInfo info = MC.getConnection().getPlayerInfo(p.getUUID());
            if (info != null) {
                int ping = info.getLatency();
                lines.add(new Line("延迟 " + ping + "ms", ping <= 200 ? 0xFFFFFFFF : 0xFFFF6060));
            }
        }

        int mw = 12;
        for (Line l : lines) mw = Math.max(mw, MC.font.width(l.text) + 12);
        int mh = lines.size() * 10 + 6;
        int[] xy = pos(mw, mh);
        int x = xy[0];
        int y = xy[1];

        g.fill(x, y, x + mw, y + mh, 0x66000000);
        g.fill(x, y, x + 2, y + mh, 0x9966CCFF);
        int ry = y + 3;
        for (Line l : lines) {
            g.drawString(MC.font, l.text, x + 6, ry, l.color, true);
            ry += 10;
        }
    }

    private static String[] direction(float yaw) {
        String[] dirs = {"南", "西南", "西", "西北", "北", "东北", "东", "东南"};
        int idx = Math.floorMod(Math.round(yaw / 45f), 8);
        int deg = Math.floorMod((int) Math.round(yaw), 360);
        return new String[]{dirs[idx], String.valueOf(deg)};
    }
}
