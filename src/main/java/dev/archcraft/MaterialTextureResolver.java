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
 * This deliberately avoids hardcoded texture paths and mod namespaces, so models
 * supplied by vanilla, other mods, and resource packs use the same lookup path.
 */
public final class MaterialTextureResolver {
    private MaterialTextureResolver() {}

    /**
     * Resolves the default block-state texture for a block item. This is a preview
     * helper; it does not by itself remap the texture onto an ArchitectureCraft shape.
     */
    public static Sprite findParticleSprite(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) return null;
        return findParticleSprite(blockItem.getBlock().getDefaultState());
    }

    /**
     * Resolves a specific state's particle sprite. Callers can pass a modded state
     * (including a state with properties selected) without knowing its texture path.
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
