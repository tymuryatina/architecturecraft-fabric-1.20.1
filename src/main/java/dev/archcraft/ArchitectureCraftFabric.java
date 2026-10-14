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
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
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
        ItemGroupEvents.modifyEntriesEvent(name.equals("sawbench") ? ItemGroups.FUNCTIONAL : ItemGroups.BUILDING_BLOCKS).register(entries -> entries.add(registered));
        return registered;
    }
    private static AbstractBlock.Settings wood() { return AbstractBlock.Settings.copy(Blocks.OAK_PLANKS); }
    private static AbstractBlock.Settings stone() { return AbstractBlock.Settings.copy(Blocks.STONE); }

    @Override public void onInitialize() {
        register("arch_d1", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,12,16,12))));
        register("arch_d2", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,10,12), Block.createCuboidShape(12,0,4,16,10,12), Block.createCuboidShape(4,8,4,12,12,12), Block.createCuboidShape(2,10,4,6,14,12), Block.createCuboidShape(10,10,4,14,14,12), Block.createCuboidShape(4,12,4,12,16,12))));
        register("arch_d3a", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,6,14,12), Block.createCuboidShape(10,8,4,13,14,12), Block.createCuboidShape(6,12,4,10,16,12))));
        register("arch_d3b", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,6,12,12), Block.createCuboidShape(10,8,4,13,12,12), Block.createCuboidShape(6,10,4,10,14,12), Block.createCuboidShape(4,14,4,12,16,12))));
        register("arch_d3c", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,7,12,12), Block.createCuboidShape(9,8,4,12,12,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4a", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_d4b", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4c", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,6,12,12), Block.createCuboidShape(10,8,4,12,12,12), Block.createCuboidShape(6,10,4,8,14,12), Block.createCuboidShape(8,10,4,10,14,12), Block.createCuboidShape(6,14,4,10,16,12))));
        register("sawbench", new SawbenchBlock(wood()));
        register("railing_oak", new ShapeBlock(wood(), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,3,16), Block.createCuboidShape(0,13,0,16,16,16),
            Block.createCuboidShape(2,3,2,5,13,5), Block.createCuboidShape(11,3,11,14,13,14))));
        register("pillar_stone", new ShapeBlock(stone(), Block.createCuboidShape(3,0,3,13,16,13)));
        register("pillar_capital_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(3,0,3,13,12,13),Block.createCuboidShape(1,12,1,15,16,15))));
        register("pillar_base_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(3,4,3,13,16,13),Block.createCuboidShape(1,0,1,15,4,15))));
        register("beam_oak", new AxisShapeBlock(wood()));
        register("beam_stone", new AxisShapeBlock(stone()));
        register("slab_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,8,16)));
        register("slab_stone", new ShapeBlock(stone(), Block.createCuboidShape(0,0,0,16,8,16)));
        register("corner_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,8,8),Block.createCuboidShape(0,0,8,8,16,16))));
        register("moulding_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,4,16),Block.createCuboidShape(0,4,0,16,8,12))));
        register("moulding_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,4,16),Block.createCuboidShape(0,4,0,16,8,12))));
        register("window_frame_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,3,16),Block.createCuboidShape(0,13,0,16,16,16),Block.createCuboidShape(0,3,0,3,13,16),Block.createCuboidShape(13,3,0,16,13,16))));

        register("railing_stone", new ShapeBlock(stone(), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,3,16), Block.createCuboidShape(0,13,0,16,16,16),
            Block.createCuboidShape(2,3,2,5,13,5), Block.createCuboidShape(11,3,11,14,13,14))));
        register("window_frame_stone", new ShapeBlock(stone(), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,3,16), Block.createCuboidShape(0,13,0,16,16,16),
            Block.createCuboidShape(0,3,0,3,13,16), Block.createCuboidShape(13,3,0,16,13,16))));
        register("corner_stone", new ShapeBlock(stone(), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,8,8), Block.createCuboidShape(0,0,8,8,16,16))));
        register("corner_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,8,8), Block.createCuboidShape(0,0,8,8,16,16))));
        register("moulding_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,4,16), Block.createCuboidShape(0,4,0,16,8,12))));
        register("trim_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), Block.createCuboidShape(0,0,0,16,3,16)));
        register("pillar_oak", new ShapeBlock(wood(), Block.createCuboidShape(3,0,3,13,16,13)));
        register("pillar_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));

        register("trim_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,3,16)));
        register("trim_stone", new ShapeBlock(stone(), Block.createCuboidShape(0,0,0,16,3,16)));
        register("post_oak", new ShapeBlock(wood(), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_stone", new ShapeBlock(stone(), Block.createCuboidShape(5,0,5,11,16,11)));

        // Additional architectural profiles. These use real voxel geometry for collision and selection.
        register("arch_support_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,5,16,16), Block.createCuboidShape(11,0,0,16,16,16), Block.createCuboidShape(5,11,0,11,16,16))));
        register("bracket_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,4,16), Block.createCuboidShape(0,4,0,6,12,16))));
        register("bracket_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,4,16), Block.createCuboidShape(0,4,0,6,12,16))));
        register("cornice_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,5,16), Block.createCuboidShape(0,5,0,16,9,12), Block.createCuboidShape(0,9,0,16,12,8))));
        register("cornice_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,5,16), Block.createCuboidShape(0,5,0,16,9,12), Block.createCuboidShape(0,9,0,16,12,8))));
        register("sill_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,3,16), Block.createCuboidShape(0,3,0,16,6,12))));
        register("sill_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,3,16), Block.createCuboidShape(0,3,0,16,6,12))));
        register("baluster_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(5,0,5,11,3,11), Block.createCuboidShape(6,3,6,10,12,10), Block.createCuboidShape(4,12,4,12,15,12), Block.createCuboidShape(5,15,5,11,16,11))));
        register("lattice_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,3,16,16), Block.createCuboidShape(13,0,0,16,16,16), Block.createCuboidShape(3,0,0,13,3,16), Block.createCuboidShape(3,13,0,13,16,16), Block.createCuboidShape(7,3,0,9,13,16))));
        register("panel_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,0,16,16,2), Block.createCuboidShape(2,2,2,14,14,4))));
        register("roof_tile_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,3,16)));
        register("roof_ridge_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,16,8,12), Block.createCuboidShape(0,8,6,16,12,10))));
        register("column_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(4,0,4,12,16,12), Block.createCuboidShape(3,0,3,13,2,13), Block.createCuboidShape(3,14,3,13,16,13))));
        register("column_capital_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(5,0,5,11,10,11), Block.createCuboidShape(2,10,2,14,13,14), Block.createCuboidShape(0,13,0,16,16,16))));
        register("column_base_oak", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(5,5,5,11,16,11), Block.createCuboidShape(2,2,2,14,5,14), Block.createCuboidShape(0,0,0,16,2,16))));
        register("beam_spruce", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS)));
        register("beam_birch", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS)));
        register("beam_dark_oak", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS)));
        register("post_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));

        register("beam_acacia", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.ACACIA_PLANKS)));
        register("beam_jungle", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_PLANKS)));
        register("beam_mangrove", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.MANGROVE_PLANKS)));
        register("beam_cherry", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.CHERRY_PLANKS)));
        register("beam_crimson", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.CRIMSON_PLANKS)));
        register("beam_warped", new AxisShapeBlock(AbstractBlock.Settings.copy(Blocks.WARPED_PLANKS)));
        register("post_acacia", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.ACACIA_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_mangrove", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.MANGROVE_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_cherry", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.CHERRY_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("slab_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("slab_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("slab_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("slab_acacia", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.ACACIA_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));


    }


    /**
     * First usable sawbench implementation. It turns a stack of planks (or stone)
     * into a matching beam; crouching makes a post where that material exists.
     * The full upstream selection GUI and arbitrary-shape cutting are still TODO.
     */
    public static class SawbenchBlock extends Block {
        public static final IntProperty MODE = IntProperty.of("mode", 0, 3);

        public SawbenchBlock(AbstractBlock.Settings settings) {
            super(settings);
            setDefaultState(getDefaultState().with(MODE, 0));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            builder.add(MODE);
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext ctx) {
            return getDefaultState().with(MODE, 0);
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
            ItemStack held = player.getStackInHand(hand);
            if (held.isEmpty()) {
                if (!world.isClient) {
                    int nextMode = (state.get(MODE) + 1) % 4;
                    world.setBlockState(pos, state.with(MODE, nextMode));
                    String[] labels = {"Beam", "Post", "Slab", "Pillar"};
                    player.sendMessage(Text.literal("Sawbench mode: " + labels[nextMode] + ". Use planks or stone to cut."), true);
                }
                return ActionResult.SUCCESS;
            }
            if (world.isClient) return ActionResult.SUCCESS;

            String inputPath = Registries.ITEM.getId(held.getItem()).getPath();
            String material;
            if (inputPath.equals("stone")) {
                material = "stone";
            } else if (inputPath.endsWith("_planks")) {
                material = inputPath.substring(0, inputPath.length() - "_planks".length());
            } else {
                player.sendMessage(Text.literal("Use planks or stone on the sawbench."), true);
                return ActionResult.CONSUME;
            }

            String prefix = switch (state.get(MODE)) {
                case 1 -> "post_";
                case 2 -> "slab_";
                case 3 -> "pillar_";
                default -> "beam_";
            };
            String outputName = prefix + material;
            Identifier outputId = new Identifier(MOD_ID, outputName);
            if (!Registries.ITEM.containsId(outputId)) {
                outputName = "beam_" + material;
                outputId = new Identifier(MOD_ID, outputName);
                if (!Registries.ITEM.containsId(outputId)) {
                    player.sendMessage(Text.literal("No matching shape exists for this material yet."), true);
                    return ActionResult.CONSUME;
                }
                player.sendMessage(Text.literal("That profile is not available for this material; cut a beam instead."), true);
            }

            if (!player.getAbilities().creativeMode) held.decrement(1);
            ItemStack result = new ItemStack(Registries.ITEM.get(outputId), 4);
            if (!player.getInventory().insertStack(result)) player.dropItem(result, false);
            player.sendMessage(Text.literal("Cut 4 " + outputName.replace('_', ' ') + "."), true);
            return ActionResult.CONSUME;
        }
    }

    /** Axis-aware beam with matching placement, collision and selection geometry. */
    public static class AxisShapeBlock extends Block {
        public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.of("axis", Direction.Axis.class);

        public AxisShapeBlock(AbstractBlock.Settings settings) {
            super(settings);
            setDefaultState(getDefaultState().with(AXIS, Direction.Axis.Y));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            builder.add(AXIS);
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext ctx) {
            return getDefaultState().with(AXIS, ctx.getSide().getAxis());
        }

        private VoxelShape shapeFor(BlockState state) {
            return switch (state.get(AXIS)) {
                case X -> Block.createCuboidShape(0, 4, 4, 16, 12, 12);
                case Y -> Block.createCuboidShape(4, 0, 4, 12, 16, 12);
                case Z -> Block.createCuboidShape(4, 4, 0, 12, 12, 16);
            };
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return shapeFor(state);
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return shapeFor(state);
        }
    }

    public static class ShapeBlock extends Block {
        private final VoxelShape shape;
        public ShapeBlock(AbstractBlock.Settings settings, VoxelShape shape) { super(settings); this.shape = shape; }
        @Override public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return shape; }
        @Override public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return shape; }
    }
}
