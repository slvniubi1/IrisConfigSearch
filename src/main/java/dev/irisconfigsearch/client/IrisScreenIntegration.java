package dev.irisconfigsearch.client;

import dev.irisconfigsearch.IrisConfigSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Adds a small entry point to Iris/NeOculus shader-pack configuration screens.
 * The screen class check is deliberately reflective so Iris remains an optional dependency.
 */
@EventBusSubscriber(modid = IrisConfigSearch.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class IrisScreenIntegration {
    private IrisScreenIntegration() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!isShaderConfigurationScreen(screen)) {
            return;
        }

        int x = 8;
        int y = Math.max(8, screen.height - 30);
        event.addListener(Button.builder(
                Component.translatable("screen.irisconfigsearch.open_from_iris"),
                button -> Minecraft.getInstance().setScreen(new ShaderOptionSearchScreen()))
                .bounds(x, y, 120, 20)
                .build());
    }

    private static boolean isShaderConfigurationScreen(Screen screen) {
        String className = screen.getClass().getName().toLowerCase(java.util.Locale.ROOT);
        String title = screen.getTitle().getString().toLowerCase(java.util.Locale.ROOT);
        return className.contains("shaderpack")
                || className.contains("shader_pack")
                || className.contains("shaderoptions")
                || className.contains("shader_options")
                || title.contains("shader pack")
                || title.contains("shaderpack")
                || title.contains("着色器包")
                || title.contains("着色器配置");
    }
}
