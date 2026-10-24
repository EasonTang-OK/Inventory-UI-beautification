package org.exampl.inventory_ui_beautification.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiHudMixin {

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void hud$cancelHearts(GuiGraphics graphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void hud$cancelArmor(GuiGraphics graphics, Player player, int i, int j, int k, int l, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void hud$cancelFood(GuiGraphics graphics, Player player, int i, int j, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void hud$cancelEffects(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
        ci.cancel();
    }

    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private static void hud$cancelLevelText(GuiGraphics graphics, Font font, int level) {
    }

    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void hud$hideXpBarBackground(ContextualBarRenderer renderer, GuiGraphics graphics, DeltaTracker tracker) {
        if (!isExperienceState()) {
            renderer.renderBackground(graphics, tracker);
        }
    }

    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void hud$hideXpBarRender(ContextualBarRenderer renderer, GuiGraphics graphics, DeltaTracker tracker) {
        if (!isExperienceState()) {
            renderer.render(graphics, tracker);
        }
    }

    private static boolean isExperienceState() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        MultiPlayerGameMode gm = mc.gameMode;
        if (p == null || gm == null) return false;
        boolean hasWp = p.connection.getWaypointManager().hasWaypoints();
        boolean riding = p.jumpableVehicle() != null;
        boolean xpActive = gm.hasExperience() && p.experienceDisplayStartTick + 100 > p.tickCount;
        if (hasWp) {
            if (riding && (p.getJumpRidingScale() > 0f || p.jumpableVehicle().getJumpCooldown() > 0)) return false;
            return xpActive;
        }
        if (riding) return false;
        return xpActive;
    }
}
