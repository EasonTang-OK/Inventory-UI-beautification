package org.exampl.inventory_ui_beautification.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import org.exampl.inventory_ui_beautification.client.hud.DamageNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    private void hud$onDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        DamageNumbers.onDamageEvent(packet.entityId(), packet.sourceCauseId());
    }
}
