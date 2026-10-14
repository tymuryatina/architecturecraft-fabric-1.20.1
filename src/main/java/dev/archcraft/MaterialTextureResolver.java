package dev.archcraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * Resolves source textures from Minecraft's active baked-model registry.
 * This avoids hardcoded texture paths and namespaces, allowing vanilla, modded
 * blocks, and resource-pack model overrides to use the normal client model lookup.
 */
public final class MaterialTextureResolver {
    private MaterialTextureResolver() {}

    /**
     * Resolves the source block's default-state particle sprite, then falls back
     * to the actual item model's particle sprite if the block model is unavailable.
     * This is a GUI preview helper, not yet a texture remapper for output shapes.
     */
    public static Sprite findParticleSprite(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return null;

        if (stack.getItem() instanceof BlockItem blockItem) {
            Sprite blockSprite = findParticleSprite(blockItem.getBlock().getDefaultState());
            if (blockSprite != null) return blockSprite;
        }

        BakedModel itemModel = client.getItemRenderer().getModels().getModel(stack);
        return itemModel == null ? null : itemModel.getParticleSprite();
    }

    /**
     * Resolves a specific block state's particle sprite, including states from mods.
     * Callers do not need to know the texture's identifier or namespace.
     */
    public static Sprite findParticleSprite(BlockState state) {
        if (state == null) return null;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getBlockRenderManager() == null) return null;
        Block block = state.getBlock();
        if (block == null) return null;
        BakedModel model = client.getBlockRenderManager().getModel(state);
        return model == null ? null : model.getParticleSprite();
    }
}
