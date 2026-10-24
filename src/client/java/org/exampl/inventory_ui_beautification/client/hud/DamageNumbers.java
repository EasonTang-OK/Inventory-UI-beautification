package org.exampl.inventory_ui_beautification.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DamageNumbers {

    private static final class Tracked {
        LivingEntity entity;
        float prev;
        long lastSeen;

        Tracked(LivingEntity entity, float prev, long lastSeen) {
            this.entity = entity;
            this.prev = prev;
            this.lastSeen = lastSeen;
        }
    }

    private static final class FloatNum {
        Vec3 pos;
        String text;
        int color;
        long born;
        float rise;

        FloatNum(Vec3 pos, String text, int color, long born, float rise) {
            this.pos = pos;
            this.text = text;
            this.color = color;
            this.born = born;
            this.rise = rise;
        }
    }

    private static final Map<Integer, Tracked> tracked = new HashMap<>();
    private static final List<FloatNum> nums = new ArrayList<>();
    private static float playerPrevHp = -1;

    private DamageNumbers() {
    }

    public static void onAttack(Entity target) {
        if (target instanceof LivingEntity le) {
            tracked.put(le.getId(), new Tracked(le, le.getHealth(), System.currentTimeMillis()));
        }
    }

    public static void onDamageEvent(int entityId, int causeId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Entity e = mc.level.getEntity(entityId);
        if (e == null || !(e instanceof LivingEntity le)) return;
        if (causeId != mc.player.getId()) return;
        Tracked tr = tracked.get(entityId);
        if (tr == null) {
            tracked.put(entityId, new Tracked(le, le.getHealth(), System.currentTimeMillis()));
        } else {
            tr.lastSeen = System.currentTimeMillis();
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        if (mc.player != null) {
            float hp = mc.player.getHealth();
            if (playerPrevHp < 0) {
                playerPrevHp = hp;
            } else if (playerPrevHp - hp > 0.4f) {
                CombatHud.onPlayerHurt();
            }
            playerPrevHp = hp;
            if (hp <= 0) CombatHud.killStreak = 0;
        }
        Iterator<Map.Entry<Integer, Tracked>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            Tracked tr = it.next().getValue();
            if (!tr.entity.isAlive()) {
                CombatHud.onKill();
                spawn(tr.entity, "击杀!", 0xFFFFD000, 0.45f);
                it.remove();
                continue;
            }
            if (now - tr.lastSeen > 5000) {
                it.remove();
                continue;
            }
            float cur = tr.entity.getHealth();
            float delta = tr.prev - cur;
            if (delta > 0.4f) {
                spawn(tr.entity, "-" + fmt(delta), 0xFFFFFFFF, 0.32f);
                CombatHud.onHit();
                tr.prev = cur;
            } else if (delta < -0.4f) {
                tr.prev = cur;
            }
        }
        nums.removeIf(n -> now - n.born > 1400);
        if (nums.size() > 64) nums.subList(64, nums.size()).clear();
    }

    private static void spawn(Entity e, String text, int color, float rise) {
        Vec3 pos = e.getPosition(0).add(0, e.getBbHeight() * 0.6f + 0.4f, 0);
        nums.add(new FloatNum(pos, text, color, System.currentTimeMillis(), rise));
    }

    public static void renderWorld(WorldRenderContext ctx) {
        if (nums.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        PoseStack stack = ctx.matrices();
        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 camPos = cam.position();
        MultiBufferSource.BufferSource bufs = mc.renderBuffers().bufferSource();
        Font font = mc.font;
        long now = System.currentTimeMillis();
        for (FloatNum n : nums) {
            float age = (now - n.born) / 1000f;
            if (age >= 1.4f) continue;
            Vec3 p = n.pos.add(0, age * n.rise, 0);
            float a = 1f - age / 1.4f;
            int alpha = (int) (a * 255);
            stack.pushPose();
            stack.translate(p.x - camPos.x, p.y - camPos.y, p.z - camPos.z);
            stack.mulPose(cam.rotation());
            stack.scale(-0.025f, -0.025f, 0.025f);
            int color = (n.color & 0xFFFFFF) | (alpha << 24);
            String text = n.text;
            font.drawInBatch(text, -font.width(text) / 2f, 0, color, true, stack.last().pose(), bufs, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
            stack.popPose();
        }
        bufs.endBatch();
    }

    private static String fmt(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format(Locale.ROOT, "%.1f", v);
    }
}
