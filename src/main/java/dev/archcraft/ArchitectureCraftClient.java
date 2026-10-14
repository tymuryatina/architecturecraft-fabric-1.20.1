package dev.archcraft;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public final class ArchitectureCraftClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        HandledScreens.register(ArchitectureCraftFabric.SAWBENCH_SCREEN_HANDLER, SawbenchScreen::new);
    }
}
