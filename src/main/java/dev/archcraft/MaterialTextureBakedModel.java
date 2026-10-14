package dev.archcraft;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

/**
 * Reuses the architecture model geometry while remapping its UVs to the selected
 * source block's particle sprite. The sprite is resolved from the live model
 * registry, so namespaces, modded blocks and resource packs are supported.
 */
public final class MaterialTextureBakedModel extends ForwardingBakedModel {
    public MaterialTextureBakedModel(BakedModel wrapped) { this.wrapped = wrapped; }

    @Override public boolean isVanillaAdapter() { return false; }

    @Override public void emitBlockQuads(BlockRenderView view, BlockState state, BlockPos pos,
            Supplier<Random> random, RenderContext context) {
        Sprite target = null;
        if (view.getBlockEntity(pos) instanceof MaterialShapeBlockEntity materialEntity) {
            Identifier id = materialEntity.getMaterialIdentifier();
            if (id != null && Registries.BLOCK.containsId(id)) {
                BlockState materialState = Registries.BLOCK.get(id).getDefaultState();
                target = MinecraftClient.getInstance().getBlockRenderManager().getModel(materialState).getParticleSprite();
            }
        }
        emitWithSprite(target, random, context, () -> ((net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel) wrapped)
            .emitBlockQuads(view, state, pos, random, context));
    }

    @Override public void emitItemQuads(ItemStack stack, Supplier<Random> random, RenderContext context) {
        Sprite target = null;
        if (stack != null && stack.hasNbt() && stack.getSubNbt("BlockEntityTag") != null) {
            String idText = stack.getSubNbt("BlockEntityTag").getString("MaterialBlock");
            try {
                Identifier id = new Identifier(idText);
                if (Registries.BLOCK.containsId(id)) target = MinecraftClient.getInstance().getBlockRenderManager()
                    .getModel(Registries.BLOCK.get(id).getDefaultState()).getParticleSprite();
            } catch (RuntimeException ignored) { }
        }
        emitWithSprite(target, random, context, () -> ((net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel) wrapped)
            .emitItemQuads(stack, random, context));
    }

    private void emitWithSprite(Sprite target, Supplier<Random> random, RenderContext context, Runnable emit) {
        if (target == null) { emit.run(); return; }
        context.pushTransform(quad -> remap(quad, target));
        try { emit.run(); } finally { context.popTransform(); }
    }

    private boolean remap(MutableQuadView quad, Sprite target) {
        Sprite source = wrapped.getParticleSprite();
        if (source == null || source == target) return true;
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = source.getFrameU(quad.spriteU(0, vertex));
            float v = source.getFrameV(quad.spriteV(0, vertex));
            float normalizedU = (u - source.getMinU()) / (source.getMaxU() - source.getMinU());
            float normalizedV = (v - source.getMinV()) / (source.getMaxV() - source.getMinV());
            quad.sprite(0, vertex, target.getMinU() + normalizedU * (target.getMaxU() - target.getMinU()),
                target.getMinV() + normalizedV * (target.getMaxV() - target.getMinV()));
        }
        return true;
    }
}
