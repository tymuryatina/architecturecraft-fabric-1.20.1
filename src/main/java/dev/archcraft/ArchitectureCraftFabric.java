package dev.archcraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ArchitectureCraftFabric implements ModInitializer {
    public static final String MOD_ID = "archcraft";
    private static Block register(String name, Block block) {
        Identifier id = new Identifier(MOD_ID, name);
        Block registered = Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(registered, new Item.Settings()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(registered));
        return registered;
    }
    private static AbstractBlock.Settings wood() { return AbstractBlock.Settings.copy(Blocks.OAK_PLANKS); }
    private static AbstractBlock.Settings stone() { return AbstractBlock.Settings.copy(Blocks.STONE); }

    @Override public void onInitialize() {
        register("sawbench", new Block(wood()));
        register("railing_oak", new ShapeBlock(wood(), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,3,16), Block.createCuboidShape(0,13,0,16,16,16),
            Block.createCuboidShape(2,3,2,5,13,5), Block.createCuboidShape(11,3,11,14,13,14))));
        register("pillar_stone", new ShapeBlock(stone(), Block.createCuboidShape(3,0,3,13,16,13)));
        register("pillar_capital_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(3,0,3,13,12,13),Block.createCuboidShape(1,12,1,15,16,15))));
        register("pillar_base_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(3,4,3,13,16,13),Block.createCuboidShape(1,0,1,15,4,15))));
        register("beam_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,4,4,16,12,12)));
        register("beam_stone", new ShapeBlock(stone(), Block.createCuboidShape(0,4,4,16,12,12)));
        register("slab_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,8,16)));
        register("slab_stone", new ShapeBlock(stone(), Block.createCuboidShape(0,0,0,16,8,16)));
        register("corner_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,8,8),Block.createCuboidShape(0,0,8,8,16,16))));
        register("moulding_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,4,16),Block.createCuboidShape(0,4,0,16,8,12))));
        register("moulding_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,4,16),Block.createCuboidShape(0,4,0,16,8,12))));
        register("window_frame_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,3,16),Block.createCuboidShape(0,13,0,16,16,16),Block.createCuboidShape(0,3,0,3,13,16),Block.createCuboidShape(13,3,0,16,13,16))));
        register("trim_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,3,16)));
        register("trim_stone", new ShapeBlock(stone(), Block.createCuboidShape(0,0,0,16,3,16)));
        register("post_oak", new ShapeBlock(wood(), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_stone", new ShapeBlock(stone(), Block.createCuboidShape(5,0,5,11,16,11)));
    }

    public static class ShapeBlock extends Block {
        private final VoxelShape shape;
        public ShapeBlock(AbstractBlock.Settings settings, VoxelShape shape) { super(settings); this.shape = shape; }
        @Override public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return shape; }
        @Override public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return shape; }
    }
}
