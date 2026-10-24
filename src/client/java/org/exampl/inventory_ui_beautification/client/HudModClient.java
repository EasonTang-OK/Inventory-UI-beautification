package org.exampl.inventory_ui_beautification.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import org.exampl.inventory_ui_beautification.Inventory_ui_beautification;
import org.exampl.inventory_ui_beautification.client.hud.CombatHud;
import org.exampl.inventory_ui_beautification.client.hud.DamageNumbers;
import org.exampl.inventory_ui_beautification.client.hud.EffectHud;
import org.exampl.inventory_ui_beautification.client.hud.HealthHud;
import org.exampl.inventory_ui_beautification.client.hud.HudConfig;
import org.exampl.inventory_ui_beautification.client.hud.HudConfigScreen;
import org.exampl.inventory_ui_beautification.client.hud.HudModule;
import org.exampl.inventory_ui_beautification.client.hud.HungerHud;
import org.exampl.inventory_ui_beautification.client.hud.InfoHud;
import org.exampl.inventory_ui_beautification.client.hud.TargetHud;
import org.exampl.inventory_ui_beautification.client.hud.XpHud;

public class HudModClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Inventory_ui_beautification.id("hud_keys"));

    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.inventory_ui_beautification.hud_config",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_H,
                CATEGORY
        ));

        HudConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new HudConfigScreen());
                }
            }
            HudModule.onClientTick();
            CombatHud.tick();
            DamageNumbers.tick();
        });

        HudElementRegistry.addLast(Inventory_ui_beautification.id("hud_health"), (g, t) -> HealthHud.INSTANCE.render(g, t));
        HudElementRegistry.addLast(Inventory_ui_beautification.id("hud_hunger"), (g, t) -> HungerHud.INSTANCE.render(g, t));
        HudElementRegistry.addLast(Inventory_ui_beautification.id("hud_xp"), (g, t) -> XpHud.INSTANCE.render(g, t));
        HudElementRegistry.addLast(Inventory_ui_beautification.id("hud_effects"), (g, t) -> EffectHud.INSTANCE.render(g, t));
        HudElementRegistry.addLast(Inventory_ui_beautification.id("hud_target"), (g, t) -> TargetHud.INSTANCE.render(g, t));
        HudElementRegistry.addLast(Inventory_ui_beautification.id("hud_info"), (g, t) -> InfoHud.INSTANCE.render(g, t));
        HudElementRegistry.addLast(Inventory_ui_beautification.id("hud_combat"), (g, t) -> CombatHud.INSTANCE.render(g, t));

        WorldRenderEvents.AFTER_ENTITIES.register(DamageNumbers::renderWorld);
    }
}
