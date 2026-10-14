package dev.archcraft;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * Resolves a material's visible texture through Minecraft's baked block-model registry.
 * No texture paths or mod namespaces are hardcoded: resource-pack overrides and models
 * registered by other mods are consulted by the same renderer Minecraft uses in-world.
 */
public final class MaterialTextureResolver {
    private MaterialTextureResolver() {}

    /**
     * Returns the particle sprite of the source block's default-state model, or null
     * when the input is not a block item or the client model registry is not available.
     */
    public static Sprite findParticleSprite(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) return null;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getBlockRenderManager() == null) return null;
        Block block = blockItem.getBlock();
        BakedModel model = client.getBlockRenderManager().getModel(block.getDefaultState());
        return model == null ? null : model.getParticleSprite();
    }
}
