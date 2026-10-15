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
import net.minecraft.util.math.Direction;
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
        BlockState materialState = null;
        if (view.getBlockEntity(pos) instanceof MaterialShapeBlockEntity materialEntity) {
            Identifier id = materialEntity.getMaterialIdentifier();
            if (id != null && Registries.BLOCK.containsId(id)) materialState = Registries.BLOCK.get(id).getDefaultState();
        }
        BakedModel materialModel = materialState == null ? null
            : MinecraftClient.getInstance().getBlockRenderManager().getModel(materialState);
        emitWithMaterialModel(materialState, materialModel, random, context,
            () -> ((net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel) wrapped)
                .emitBlockQuads(view, state, pos, random, context));
    }

    @Override public void emitItemQuads(ItemStack stack, Supplier<Random> random, RenderContext context) {
        BlockState materialState = null;
        if (stack != null && stack.hasNbt() && stack.getSubNbt("BlockEntityTag") != null) {
            String idText = stack.getSubNbt("BlockEntityTag").getString("MaterialBlock");
            try {
                Identifier id = new Identifier(idText);
                if (Registries.BLOCK.containsId(id)) materialState = Registries.BLOCK.get(id).getDefaultState();
            } catch (RuntimeException ignored) { }
        }
        BakedModel materialModel = materialState == null ? null
            : MinecraftClient.getInstance().getBlockRenderManager().getModel(materialState);
        emitWithMaterialModel(materialState, materialModel, random, context,
            () -> ((net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel) wrapped)
                .emitItemQuads(stack, random, context));
    }

    private void emitWithMaterialModel(BlockState materialState, BakedModel materialModel,
            Supplier<Random> random, RenderContext context, Runnable emit) {
        if (materialState == null || materialModel == null) { emit.run(); return; }
        Sprite fallback = materialModel.getParticleSprite();
        context.pushTransform(quad -> {
            Direction face = quad.lightFace();
            Sprite target = fallback;
            if (face != null) {
                java.util.List<net.minecraft.client.render.model.BakedQuad> faceQuads =
                    materialModel.getQuads(materialState, face, random.get());
                if (!faceQuads.isEmpty() && faceQuads.get(0).getSprite() != null)
                    target = faceQuads.get(0).getSprite();
            }
            return remap(quad, target);
        });
        try { emit.run(); } finally { context.popTransform(); }
    }

    private boolean remap(MutableQuadView quad, Sprite target) {
        Sprite source = wrapped.getParticleSprite();
        if (source == null || source == target) return true;
        // FRAPI's parameter order is (vertexIndex, spriteIndex). These quads use sprite index 0.
        // spriteU/spriteV return atlas coordinates, so convert atlas -> source-local -> target-atlas.
        float sourceWidth = source.getMaxU() - source.getMinU();
        float sourceHeight = source.getMaxV() - source.getMinV();
        if (sourceWidth == 0.0f || sourceHeight == 0.0f) return true;
        float targetWidth = target.getMaxU() - target.getMinU();
        float targetHeight = target.getMaxV() - target.getMinV();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = quad.spriteU(vertex, 0);
            float v = quad.spriteV(vertex, 0);
            float normalizedU = (u - source.getMinU()) / sourceWidth;
            float normalizedV = (v - source.getMinV()) / sourceHeight;
            quad.sprite(vertex, 0, target.getMinU() + normalizedU * targetWidth,
                target.getMinV() + normalizedV * targetHeight);
        }
        return true;
    }
}
