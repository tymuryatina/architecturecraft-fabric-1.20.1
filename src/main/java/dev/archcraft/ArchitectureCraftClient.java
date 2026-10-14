package dev.archcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.Identifier;

public final class ArchitectureCraftClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        HandledScreens.register(ArchitectureCraftFabric.SAWBENCH_SCREEN_HANDLER, SawbenchScreen::new);
        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register(
            (model, bakeContext) -> {
                Identifier id = bakeContext.id();
                if (model == null || !id.getNamespace().equals(ArchitectureCraftFabric.MOD_ID)
                    || !(id.getPath().startsWith("block/") || id.getPath().startsWith("item/"))) return model;
                return new MaterialTextureBakedModel(model);
            }
        ));
    }
}
