package org.exampl.inventory_ui_beautification.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmokerMenu;
import org.exampl.inventory_ui_beautification.Inventory_ui_beautification;
import org.exampl.inventory_ui_beautification.mixin.client.ContainerScreenAccessor;

public class Inventory_ui_beautificationClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Inventory_ui_beautification.id("keys"));

    private static KeyMapping sortKey;
    private static KeyMapping takeKey;
    private static KeyMapping depositKey;
    private static KeyMapping restockKey;
    private static KeyMapping stackKey;
    private static KeyMapping structHudKey;

    private static boolean structHudOn = true;
    private static String structLine1 = "";
    private static String structLine2 = "";
    private static int structTick = 0;

    @Override
    public void onInitializeClient() {
        sortKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.inventory_ui_beautification.sort",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_K,
                CATEGORY
        ));
        takeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.inventory_ui_beautification.take",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_T,
                CATEGORY
        ));
        depositKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.inventory_ui_beautification.deposit",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_R,
                CATEGORY
        ));
        restockKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.inventory_ui_beautification.restock",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_C,
                CATEGORY
        ));
        stackKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.inventory_ui_beautification.stack",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_Z,
                CATEGORY
        ));
        structHudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.inventory_ui_beautification.struct_hud",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_O,
                CATEGORY
        ));

        ClientPlayNetworking.registerGlobalReceiver(Inventory_ui_beautification.StructureInfoPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    structLine1 = payload.line1();
                    structLine2 = payload.line2();
                }));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sortKey.consumeClick()) {
                if (client.screen == null && client.player != null) {
                    ClientPlayNetworking.send(new Inventory_ui_beautification.SortRequestPayload());
                }
            }
            while (structHudKey.consumeClick()) {
                structHudOn = !structHudOn;
            }
            if (client.player != null && client.level != null) {
                structTick++;
                if (structTick >= 60) {
                    structTick = 0;
                    ClientPlayNetworking.send(new Inventory_ui_beautification.StructureRequestPayload());
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> container) {
                ContainerScreenAccessor acc = (ContainerScreenAccessor)(Object) container;
                int right = acc.getLeftPos() + acc.getImageWidth();
                Screens.getButtons(container).add(Button.builder(
                        Component.literal("整理"),
                        b -> {
                            if (client.player != null) {
                                ClientPlayNetworking.send(new Inventory_ui_beautification.SortRequestPayload());
                            }
                        }
                ).bounds(right - 24, acc.getTopPos() + 4, 20, 20).build());
                Screens.getButtons(container).add(Button.builder(
                        Component.literal("拿取"),
                        b -> {
                            if (client.player != null) {
                                ClientPlayNetworking.send(new Inventory_ui_beautification.TakeAllPayload());
                            }
                        }
                ).bounds(right - 48, acc.getTopPos() + 4, 20, 20).build());
                Screens.getButtons(container).add(Button.builder(
                        Component.literal("放回"),
                        b -> {
                            if (client.player != null) {
                                ClientPlayNetworking.send(new Inventory_ui_beautification.DepositAllPayload());
                            }
                        }
                ).bounds(right - 72, acc.getTopPos() + 4, 20, 20).build());

                ScreenEvents.afterRender(container).register((s, graphics, mouseX, mouseY, tickProgress) -> {
                    int left = acc.getLeftPos();
                    int top = acc.getTopPos();
                    int w = acc.getImageWidth();
                    int h = acc.getImageHeight();
                    int blue = 0xFF66CCFF;
                    int purple = 0xFFAA66FF;
                    int step = 4;
                    for (int x = left - 2; x < left + w + 2; x += step) {
                        float t = (float)(x - left + 2) / (w + 4);
                        int c = lerp(blue, purple, t);
                        graphics.fill(x, top - 2, Math.min(x + step, left + w + 2), top - 1, c);
                        graphics.fill(x, top + h + 1, Math.min(x + step, left + w + 2), top + h + 2, c);
                    }
                    for (int y = top - 2; y < top + h + 2; y += step) {
                        float t = (float)(y - top + 2) / (h + 4);
                        int c = lerp(blue, purple, t);
                        graphics.fill(left - 2, y, left - 1, Math.min(y + step, top + h + 2), c);
                        graphics.fill(left + w + 1, y, left + w + 2, Math.min(y + step, top + h + 2), c);
                    }
                    drawCorner(graphics, left - 3, top - 3, true, true);
                    drawCorner(graphics, left + w + 2, top - 3, false, true);
                    drawCorner(graphics, left - 3, top + h + 2, true, false);
                    drawCorner(graphics, left + w + 2, top + h + 2, false, false);
                    int tx = acc.getTitleLabelX();
                    int ty = acc.getTitleLabelY() + 10;
                    int tw = client.font.width(s.getTitle());
                    for (int x = tx; x < tx + tw; x += 2) {
                        float t = (float)(x - tx) / Math.max(1, tw);
                        graphics.fill(x, ty, Math.min(x + 2, tx + tw), ty + 1, lerp(blue, purple, t));
                    }
                    graphics.fill(left + 7, top + h - 12, left + w - 7, top + h - 11, 0x60FFFFFF);
                    int cc = containerCount(container);
                    if (cc > 0) {
                        int used = 0;
                        for (int i = 0; i < cc; i++) {
                            if (container.getMenu().getSlot(i).hasItem()) used++;
                        }
                        graphics.drawString(client.font, "容器 " + used + "/" + cc, left + 8, top + h - 9, 0x80FFFFFF, true);
                    }
                });

                ScreenKeyboardEvents.beforeKeyPress(container).register(new ScreenKeyboardEvents.BeforeKeyPress() {
                    @Override
                    public void beforeKeyPress(Screen s, KeyEvent event) {
                        if (client.player == null) return;
                        if (sortKey.matches(event)) {
                            ClientPlayNetworking.send(new Inventory_ui_beautification.SortRequestPayload());
                        } else if (takeKey.matches(event)) {
                            ClientPlayNetworking.send(new Inventory_ui_beautification.TakeAllPayload());
                        } else if (depositKey.matches(event)) {
                            ClientPlayNetworking.send(new Inventory_ui_beautification.DepositAllPayload());
                        } else if (restockKey.matches(event)) {
                            ClientPlayNetworking.send(new Inventory_ui_beautification.RestockPayload());
                        } else if (stackKey.matches(event)) {
                            Slot hovered = acc.getHoveredSlot();
                            if (hovered != null) {
                                int idx = container.getMenu().slots.indexOf(hovered);
                                if (idx >= 0 && idx < containerCount(container)) {
                                    ClientPlayNetworking.send(new Inventory_ui_beautification.TakeStackPayload(idx));
                                }
                            }
                        }
                    }
                });
            }
        });

        HudElementRegistry.addLast(Inventory_ui_beautification.id("inventory_usage"), (graphics, tick) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            int used = 0;
            for (int i = 9; i < 36; i++) {
                if (!mc.player.getInventory().getItem(i).isEmpty()) used++;
            }
            String text = "背包 " + used + "/27";
            int tw = mc.font.width(text);
            int x = mc.getWindow().getGuiScaledWidth() - tw - 10;
            int y = mc.getWindow().getGuiScaledHeight() - 24;
            graphics.fill(x - 2, y - 1, x + tw + 3, y + 10, 0x66000000);
            graphics.fill(x - 2, y - 1, x + tw + 3, y, 0x9966CCFF);
            graphics.drawString(mc.font, text, x + 1, y + 1, 0xFFFFFFFF, true);
        });

        HudElementRegistry.addLast(Inventory_ui_beautification.id("structure_tracker"), (graphics, tick) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !structHudOn) return;
            int x = 5;
            int y = 5;
            String[] lines = {structLine1, structLine2};
            int maxW = 0;
            for (String l : lines) if (!l.isEmpty()) maxW = Math.max(maxW, mc.font.width(l));
            if (maxW == 0) return;
            graphics.fill(x - 3, y - 3, x + maxW + 4, y + 24, 0x88000000);
            graphics.fill(x - 3, y - 3, x + maxW + 4, y - 2, 0x9966CCFF);
            int yy = y;
            for (String l : lines) {
                if (l.isEmpty()) continue;
                graphics.drawString(mc.font, l, x, yy, 0xFFFFFFFF, true);
                yy += 10;
            }
        });
    }

    private static int containerCount(AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        if (!(menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu || menu instanceof HopperMenu
                || menu instanceof DispenserMenu || menu instanceof FurnaceMenu || menu instanceof SmokerMenu
                || menu instanceof BlastFurnaceMenu || menu instanceof BrewingStandMenu)) {
            return 0;
        }
        return menu.slots.size() - 36;
    }

    private static int lerp(int c1, int c2, float t) {
        int a = (int)(((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int)(((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int)(((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int)((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawCorner(GuiGraphics graphics, int x, int y, boolean right, boolean down) {
        int dx = right ? 1 : -1;
        int dy = down ? 1 : -1;
        fill(graphics, x, y, x + 7 * dx, y + dy, 0x60FFFFFF);
        fill(graphics, x, y, x + dx, y + 7 * dy, 0x60FFFFFF);
        fill(graphics, x + dx, y + dy, x + 6 * dx, y + 2 * dy, 0xFFFFFFFF);
        fill(graphics, x + dx, y + dy, x + 2 * dx, y + 6 * dy, 0xFFFFFFFF);
    }

    private static void fill(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2), color);
    }
}
