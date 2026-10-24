package org.exampl.inventory_ui_beautification.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class HudConfigScreen extends Screen {

    private static final String[] IDS = {"health", "hunger", "xp", "effects", "target", "info", "combat"};
    private static final String[] NAMES = {"血条", "饥饿条", "经验条", "药水效果", "目标信息", "信息面板", "战斗反馈"};
    private static final String[] ANCHORS = {"左上", "右上", "左下", "底部居中", "屏幕中心"};

    private static final HudModule[] MODULES = {
            HealthHud.INSTANCE, HungerHud.INSTANCE, XpHud.INSTANCE, EffectHud.INSTANCE,
            TargetHud.INSTANCE, InfoHud.INSTANCE, CombatHud.INSTANCE
    };

    private int selected;

    public HudConfigScreen() {
        super(Component.literal("HUD 配置中心"));
    }

    @Override
    protected void init() {
        for (int i = 0; i < IDS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(NAMES[i]), b -> selected = idx)
                    .bounds(10, 32 + i * 22, 96, 20).build());
        }
        int px = 130;
        addRenderableWidget(Button.builder(Component.literal("切换开关"), b -> toggle())
                .bounds(px, 32, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("重置位置"), b -> resetPos())
                .bounds(px, 57, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("全部启用"), b -> setAll(true))
                .bounds(px, 82, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("全部禁用"), b -> setAll(false))
                .bounds(px, 107, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("保存并退出"), b -> onClose())
                .bounds(px, 132, 90, 20).build());
    }

    private HudConfig.Entry entry() {
        return HudConfig.of(IDS[selected]);
    }

    private void toggle() {
        HudConfig.Entry e = entry();
        e.enabled = !e.enabled;
    }

    private void resetPos() {
        HudConfig.Entry e = entry();
        e.dx = 0;
        e.dy = 0;
    }

    private void setAll(boolean v) {
        for (String id : IDS) HudConfig.of(id).enabled = v;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        HudConfig.Entry e = entry();
        int step = (event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0 ? 8 : 1;
        switch (event.key()) {
            case GLFW.GLFW_KEY_UP:
                e.dy -= step;
                return true;
            case GLFW.GLFW_KEY_DOWN:
                e.dy += step;
                return true;
            case GLFW.GLFW_KEY_LEFT:
                e.dx -= step;
                return true;
            case GLFW.GLFW_KEY_RIGHT:
                e.dx += step;
                return true;
            case GLFW.GLFW_KEY_LEFT_BRACKET:
                e.anchor = Math.floorMod(e.anchor - 1, 5);
                return true;
            case GLFW.GLFW_KEY_RIGHT_BRACKET:
                e.anchor = (e.anchor + 1) % 5;
                return true;
            case GLFW.GLFW_KEY_SPACE:
                e.enabled = !e.enabled;
                return true;
            case GLFW.GLFW_KEY_R:
                e.dx = 0;
                e.dy = 0;
                return true;
            default:
                return super.keyPressed(event);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        HudConfig.Entry e = entry();
        int dx = (int) Math.round(dragX);
        int dy = (int) Math.round(dragY);
        switch (e.anchor) {
            case 1:
                e.dx -= dx;
                break;
            case 2:
                e.dy -= dy;
                break;
            case 3:
                e.dx += dx;
                e.dy -= dy;
                break;
            case 4:
                e.dx += dx;
                e.dy += dy;
                break;
            default:
                e.dx += dx;
                e.dy += dy;
                break;
        }
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        g.fill(0, 0, sw, sh, 0x33000000);

        for (HudModule m : MODULES) {
            HudConfig.Entry e = HudConfig.of(m.id);
            int[] p = m.pos(m.width(), m.height());
            int x = p[0];
            int y = p[1];
            int w = m.width();
            int h = m.height();
            if (e.enabled) {
                m.render(g, mc.getDeltaTracker());
            } else {
                g.fill(x, y, x + w, y + 1, 0x60FFFFFF);
                g.fill(x, y + h - 1, x + w, y + h, 0x60FFFFFF);
                g.fill(x, y, x + 1, y + h, 0x60FFFFFF);
                g.fill(x + w - 1, y, x + w, y + h, 0x60FFFFFF);
            }
        }

        HudModule sm = MODULES[selected];
        int[] sp = sm.pos(sm.width(), sm.height());
        g.fill(sp[0] - 2, sp[1] - 2, sp[0] + sm.width() + 2, sp[1] - 1, 0xFFFFFFFF);
        g.fill(sp[0] - 2, sp[1] + sm.height() + 1, sp[0] + sm.width() + 2, sp[1] + sm.height() + 2, 0xFFFFFFFF);
        g.fill(sp[0] - 2, sp[1] - 2, sp[0] - 1, sp[1] + sm.height() + 2, 0xFFFFFFFF);
        g.fill(sp[0] + sm.width() + 1, sp[1] - 2, sp[0] + sm.width() + 2, sp[1] + sm.height() + 2, 0xFFFFFFFF);

        HudConfig.Entry sel = entry();
        String info = "选中: " + NAMES[selected] + " | " + (sel.enabled ? "开" : "关")
                + " | 锚点: " + ANCHORS[Math.max(0, Math.min(4, sel.anchor))]
                + " | 偏移: (" + sel.dx + ", " + sel.dy + ")";
        g.drawString(mc.font, info, 10, 8, 0xFFFFFFFF, true);
        g.drawString(mc.font, "拖拽移动 | 方向键微调 Shift大步 | [ ]锚点 | 空格开关 | R重置 | Esc保存退出",
                10, sh - 14, 0x90FFFFFF, true);
    }

    @Override
    public void removed() {
        HudConfig.save();
    }
}
