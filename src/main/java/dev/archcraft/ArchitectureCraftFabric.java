package dev.archcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.inventory.Inventory;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockWithEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ArchitectureCraftFabric implements ModInitializer {
    public static final String MOD_ID = "archcraft";
    public static Block SAWBENCH_BLOCK;
    public static BlockEntityType<SawbenchBlockEntity> SAWBENCH_BLOCK_ENTITY;
    public static ScreenHandlerType<SawbenchScreenHandler> SAWBENCH_SCREEN_HANDLER;
    public static BlockEntityType<MaterialShapeBlockEntity> MATERIAL_SHAPE_BLOCK_ENTITY;
    private static final List<Block> MATERIAL_SHAPE_BLOCKS = new ArrayList<>();
    /** One entry per geometric shape; material-specific legacy blocks are not separate choices. */
    private static List<Block> getUniqueSawbenchShapes() {
        Map<String, Block> unique = new LinkedHashMap<>();
        for (Block block : MATERIAL_SHAPE_BLOCKS) {
            String path = Registries.BLOCK.getId(block).getPath();
            String key = canonicalShapeKey(path);
            Block previous = unique.get(key);
            if (previous == null || materialPreference(path) < materialPreference(Registries.BLOCK.getId(previous).getPath()))
                unique.put(key, block);
        }
        return new ArrayList<>(unique.values());
    }
    private static String canonicalShapeKey(String path) {
        String[] materials = {"dark_oak", "spruce", "birch", "acacia", "jungle", "mangrove", "cherry", "crimson", "warped", "stone", "custom", "oak"};
        for (String material : materials) {
            String suffix = "_" + material;
            if (path.endsWith(suffix)) return path.substring(0, path.length() - suffix.length());
        }
        return path;
    }
    private static int materialPreference(String path) {
        if (!path.contains("_")) return 0;
        if (path.endsWith("_custom")) return 1;
        if (path.endsWith("_oak")) return 2;
        if (path.endsWith("_stone")) return 3;
        return 4;
    }
    public static int getSawbenchShapeCount() { return getUniqueSawbenchShapes().size(); }
    public static Identifier getSawbenchShapeId(int index) {
        List<Block> shapes = getUniqueSawbenchShapes();
        if (index < 0 || index >= shapes.size()) return null;
        return Registries.BLOCK.getId(shapes.get(index));
    }
    public static Block getSawbenchShapeBlock(int index) {
        List<Block> shapes = getUniqueSawbenchShapes();
        return index < 0 || index >= shapes.size() ? null : shapes.get(index);
    }
    public static String getSawbenchShapeName(int index) {
        Identifier id = getSawbenchShapeId(index);
        return id == null ? "Unknown" : canonicalShapeKey(id.getPath()).replace('_', ' ');
    }
    private static Block register(String name, Block block) {
        Identifier id = new Identifier(MOD_ID, name);
        Block registered = Registry.register(Registries.BLOCK, id, block);
        if (block instanceof ShapeBlock || block instanceof AxisShapeBlock) MATERIAL_SHAPE_BLOCKS.add(registered);
        Registry.register(Registries.ITEM, id, new BlockItem(registered, new Item.Settings()));
        ItemGroupEvents.modifyEntriesEvent(name.equals("sawbench") ? ItemGroups.FUNCTIONAL : ItemGroups.BUILDING_BLOCKS).register(entries -> entries.add(registered));
        return registered;
    }
    private static AbstractBlock.Settings wood() { return AbstractBlock.Settings.copy(Blocks.OAK_PLANKS); }
    private static AbstractBlock.Settings stone() { return AbstractBlock.Settings.copy(Blocks.STONE); }

    @Override public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "sawbench_mode"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int mode = buf.readVarInt();
            server.execute(() -> {
                if (mode >= 0 && mode <= 3 && player.squaredDistanceTo(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5) <= 64.0) {
                    BlockState current = player.getWorld().getBlockState(pos);
                    if (current.getBlock() instanceof SawbenchBlock) player.getWorld().setBlockState(pos, current.with(SawbenchBlock.MODE, mode));
                }
            });
        });
        register("arch_d1", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,12,16,12))));
        register("arch_d2", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,10,12), Block.createCuboidShape(12,0,4,16,10,12), Block.createCuboidShape(4,8,4,12,12,12), Block.createCuboidShape(2,10,4,6,14,12), Block.createCuboidShape(10,10,4,14,14,12), Block.createCuboidShape(4,12,4,12,16,12))));
        register("arch_d3a", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,6,14,12), Block.createCuboidShape(10,8,4,13,14,12), Block.createCuboidShape(6,12,4,10,16,12))));
        register("arch_d3b", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,6,12,12), Block.createCuboidShape(10,8,4,13,12,12), Block.createCuboidShape(6,10,4,10,14,12), Block.createCuboidShape(4,14,4,12,16,12))));
        register("arch_d3c", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,7,12,12), Block.createCuboidShape(9,8,4,12,12,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4a", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_d4b", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4c", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,6,12,12), Block.createCuboidShape(10,8,4,12,12,12), Block.createCuboidShape(6,10,4,8,14,12), Block.createCuboidShape(8,10,4,10,14,12), Block.createCuboidShape(6,14,4,10,16,12))));
        register("arch_round_d1", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,9,12), Block.createCuboidShape(13,0,4,16,9,12), Block.createCuboidShape(3,8,4,13,11,12), Block.createCuboidShape(4,10,4,12,13,12), Block.createCuboidShape(5,12,4,11,14,12), Block.createCuboidShape(6,13,4,10,15,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_round_d2", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,2,10,12), Block.createCuboidShape(14,0,4,16,10,12), Block.createCuboidShape(2,9,4,14,11,12), Block.createCuboidShape(3,10,4,13,13,12), Block.createCuboidShape(4,12,4,12,14,12), Block.createCuboidShape(6,13,4,10,15,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_round_d3", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,8,12), Block.createCuboidShape(12,0,4,16,8,12), Block.createCuboidShape(4,7,4,12,10,12), Block.createCuboidShape(3,9,4,13,12,12), Block.createCuboidShape(4,11,4,12,14,12), Block.createCuboidShape(5,13,4,11,15,12), Block.createCuboidShape(6,14,4,10,16,12))));
        register("arch_round_d4", new ShapeBlock(wood(), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,11,12), Block.createCuboidShape(13,0,4,16,11,12), Block.createCuboidShape(3,10,4,13,12,12), Block.createCuboidShape(4,11,4,12,14,12), Block.createCuboidShape(5,13,4,11,15,12), Block.createCuboidShape(6,14,4,10,16,12))));
        register("arch_d1_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,12,16,12))));
        register("arch_d2_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,10,12), Block.createCuboidShape(12,0,4,16,10,12), Block.createCuboidShape(4,8,4,12,12,12), Block.createCuboidShape(2,10,4,6,14,12), Block.createCuboidShape(10,10,4,14,14,12), Block.createCuboidShape(4,12,4,12,16,12))));
        register("arch_d3a_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,6,14,12), Block.createCuboidShape(10,8,4,13,14,12), Block.createCuboidShape(6,12,4,10,16,12))));
        register("arch_d3b_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,6,12,12), Block.createCuboidShape(10,8,4,13,12,12), Block.createCuboidShape(6,10,4,10,14,12), Block.createCuboidShape(4,14,4,12,16,12))));
        register("arch_d3c_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,7,12,12), Block.createCuboidShape(9,8,4,12,12,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4a_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_d4b_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4c_spruce", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,6,12,12), Block.createCuboidShape(10,8,4,12,12,12), Block.createCuboidShape(6,10,4,8,14,12), Block.createCuboidShape(8,10,4,10,14,12), Block.createCuboidShape(6,14,4,10,16,12))));
        register("arch_d1_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,12,16,12))));
        register("arch_d2_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,10,12), Block.createCuboidShape(12,0,4,16,10,12), Block.createCuboidShape(4,8,4,12,12,12), Block.createCuboidShape(2,10,4,6,14,12), Block.createCuboidShape(10,10,4,14,14,12), Block.createCuboidShape(4,12,4,12,16,12))));
        register("arch_d3a_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,6,14,12), Block.createCuboidShape(10,8,4,13,14,12), Block.createCuboidShape(6,12,4,10,16,12))));
        register("arch_d3b_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,6,12,12), Block.createCuboidShape(10,8,4,13,12,12), Block.createCuboidShape(6,10,4,10,14,12), Block.createCuboidShape(4,14,4,12,16,12))));
        register("arch_d3c_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,7,12,12), Block.createCuboidShape(9,8,4,12,12,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4a_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_d4b_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4c_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,6,12,12), Block.createCuboidShape(10,8,4,12,12,12), Block.createCuboidShape(6,10,4,8,14,12), Block.createCuboidShape(8,10,4,10,14,12), Block.createCuboidShape(6,14,4,10,16,12))));
        register("arch_d1_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,12,16,12))));
        register("arch_d2_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,10,12), Block.createCuboidShape(12,0,4,16,10,12), Block.createCuboidShape(4,8,4,12,12,12), Block.createCuboidShape(2,10,4,6,14,12), Block.createCuboidShape(10,10,4,14,14,12), Block.createCuboidShape(4,12,4,12,16,12))));
        register("arch_d3a_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,6,14,12), Block.createCuboidShape(10,8,4,13,14,12), Block.createCuboidShape(6,12,4,10,16,12))));
        register("arch_d3b_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,6,12,12), Block.createCuboidShape(10,8,4,13,12,12), Block.createCuboidShape(6,10,4,10,14,12), Block.createCuboidShape(4,14,4,12,16,12))));
        register("arch_d3c_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,7,12,12), Block.createCuboidShape(9,8,4,12,12,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4a_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_d4b_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4c_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,6,12,12), Block.createCuboidShape(10,8,4,12,12,12), Block.createCuboidShape(6,10,4,8,14,12), Block.createCuboidShape(8,10,4,10,14,12), Block.createCuboidShape(6,14,4,10,16,12))));
        register("arch_d1_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,12,16,12))));
        register("arch_d2_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,10,12), Block.createCuboidShape(12,0,4,16,10,12), Block.createCuboidShape(4,8,4,12,12,12), Block.createCuboidShape(2,10,4,6,14,12), Block.createCuboidShape(10,10,4,14,14,12), Block.createCuboidShape(4,12,4,12,16,12))));
        register("arch_d3a_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,6,14,12), Block.createCuboidShape(10,8,4,13,14,12), Block.createCuboidShape(6,12,4,10,16,12))));
        register("arch_d3b_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,6,12,12), Block.createCuboidShape(10,8,4,13,12,12), Block.createCuboidShape(6,10,4,10,14,12), Block.createCuboidShape(4,14,4,12,16,12))));
        register("arch_d3c_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,7,12,12), Block.createCuboidShape(9,8,4,12,12,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4a_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,12,12), Block.createCuboidShape(13,0,4,16,12,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,14,4,9,16,12))));
        register("arch_d4b_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,3,10,12), Block.createCuboidShape(13,0,4,16,10,12), Block.createCuboidShape(3,8,4,5,12,12), Block.createCuboidShape(11,8,4,13,12,12), Block.createCuboidShape(5,10,4,7,14,12), Block.createCuboidShape(9,10,4,11,14,12), Block.createCuboidShape(7,12,4,9,16,12))));
        register("arch_d4c_stone", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.STONE), VoxelShapes.union(Block.createCuboidShape(0,0,4,4,12,12), Block.createCuboidShape(12,0,4,16,12,12), Block.createCuboidShape(4,8,4,6,12,12), Block.createCuboidShape(10,8,4,12,12,12), Block.createCuboidShape(6,10,4,8,14,12), Block.createCuboidShape(8,10,4,10,14,12), Block.createCuboidShape(6,14,4,10,16,12))));
        SAWBENCH_BLOCK = register("sawbench", new SawbenchBlock(wood()));
        SAWBENCH_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "sawbench"), BlockEntityType.Builder.create(SawbenchBlockEntity::new, SAWBENCH_BLOCK).build(null));
        SAWBENCH_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "sawbench"), new ScreenHandlerType<>(SawbenchScreenHandler::new, FeatureSet.empty()));
        register("railing_oak", new ShapeBlock(wood(), VoxelShapes.union(
            Block.createCuboidShape(0,0,0,16,3,16), Block.createCuboidShape(0,13,0,16,16,16),
            Block.createCuboidShape(2,3,2,5,13,5), Block.createCuboidShape(11,3,11,14,13,14))));
        register("pillar_stone", new ShapeBlock(stone(), Block.createCuboidShape(3,0,3,13,16,13)));
        register("pillar_capital_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(3,0,3,13,12,13),Block.createCuboidShape(1,12,1,15,16,15))));
        register("pillar_base_stone", new ShapeBlock(stone(), VoxelShapes.union(Block.createCuboidShape(3,4,3,13,16,13),Block.createCuboidShape(1,0,1,15,4,15))));
        register("beam_oak", new AxisShapeBlock(wood()));
        register("beam_stone", new AxisShapeBlock(stone()));
        // Generic material-backed outputs support arbitrary BlockItems in the sawbench.
        register("beam_custom", new AxisShapeBlock(wood()));
        register("slab_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,8,16)));
        register("slab_stone", new ShapeBlock(stone(), Block.createCuboidShape(0,0,0,16,8,16)));
        register("slab_custom", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,8,16)));
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
        register("pillar_custom", new ShapeBlock(wood(), Block.createCuboidShape(3,0,3,13,16,13)));

        register("trim_oak", new ShapeBlock(wood(), Block.createCuboidShape(0,0,0,16,3,16)));
        register("trim_stone", new ShapeBlock(stone(), Block.createCuboidShape(0,0,0,16,3,16)));
        register("post_oak", new ShapeBlock(wood(), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_stone", new ShapeBlock(stone(), Block.createCuboidShape(5,0,5,11,16,11)));
        register("post_custom", new ShapeBlock(wood(), Block.createCuboidShape(5,0,5,11,16,11)));

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

        // Complete the sawbench's wood-family outputs so every vanilla wood can be crafted.
        register("pillar_birch", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        register("pillar_dark_oak", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        register("pillar_acacia", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.ACACIA_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        register("post_jungle", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("slab_jungle", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("pillar_jungle", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        register("slab_mangrove", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.MANGROVE_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("pillar_mangrove", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.MANGROVE_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        register("slab_cherry", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.CHERRY_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("pillar_cherry", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.CHERRY_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        register("post_crimson", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.CRIMSON_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("slab_crimson", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.CRIMSON_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("pillar_crimson", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.CRIMSON_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        register("post_warped", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.WARPED_PLANKS), Block.createCuboidShape(5,0,5,11,16,11)));
        register("slab_warped", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.WARPED_PLANKS), Block.createCuboidShape(0,0,0,16,8,16)));
        register("pillar_warped", new ShapeBlock(AbstractBlock.Settings.copy(Blocks.WARPED_PLANKS), Block.createCuboidShape(3,0,3,13,16,13)));
        MATERIAL_SHAPE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "material_shape"), BlockEntityType.Builder.create(MaterialShapeBlockEntity::new, MATERIAL_SHAPE_BLOCKS.toArray(new Block[0])).build(null));


    }


    private static void copyMaterialFromStack(World world, BlockPos pos, ItemStack stack) {
        if (world.isClient || stack == null || !stack.hasNbt()) return;
        net.minecraft.nbt.NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag == null || !tag.contains("MaterialBlock")) return;
        try {
            Identifier materialId = new Identifier(tag.getString("MaterialBlock"));
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof MaterialShapeBlockEntity materialEntity
                    && Registries.BLOCK.containsId(materialId)) {
                materialEntity.setMaterialIdentifier(materialId);
            }
        } catch (RuntimeException ignored) {
            // Ignore malformed item data rather than preventing block placement.
        }
    }

    /** Separate workbench block with a server-backed inventory screen. */
    public static class SawbenchBlock extends BlockWithEntity {
        public SawbenchBlock(AbstractBlock.Settings settings) {
            super(settings);
            setDefaultState(getDefaultState().with(MODE, 0));
        }
        public static final IntProperty MODE = IntProperty.of("mode", 0, 3);
        @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(MODE); }
        @Override public BlockState getPlacementState(ItemPlacementContext ctx) { return getDefaultState().with(MODE, 0); }
        @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new SawbenchBlockEntity(pos, state); }
        @Override public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
            if (!state.isOf(newState.getBlock()) && !world.isClient) {
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof Inventory inventory) ItemScatterer.spawn(world, pos, inventory);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
        @Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
        @Override public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
            if (!world.isClient) {
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof SawbenchBlockEntity bench) player.openHandledScreen(bench);
            }
            return ActionResult.SUCCESS;
        }
    }

    /** Axis-aware beam with matching placement, collision and selection geometry. */
    public static class AxisShapeBlock extends BlockWithEntity {
        public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.of("axis", Direction.Axis.class);

        public AxisShapeBlock(AbstractBlock.Settings settings) {
            super(settings);
            setDefaultState(getDefaultState().with(AXIS, Direction.Axis.Y));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            builder.add(AXIS);
        }

        @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new MaterialShapeBlockEntity(pos, state); }
        @Override public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, ItemStack stack) { super.onPlaced(world, pos, state, placer, stack); copyMaterialFromStack(world, pos, stack); }
        @Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

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

    public static class ShapeBlock extends BlockWithEntity {
        private final VoxelShape shape;
        public ShapeBlock(AbstractBlock.Settings settings, VoxelShape shape) { super(settings); this.shape = shape; }
        @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new MaterialShapeBlockEntity(pos, state); }
        @Override public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, ItemStack stack) { super.onPlaced(world, pos, state, placer, stack); copyMaterialFromStack(world, pos, stack); }
        @Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
        @Override public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return shape; }
        @Override public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return shape; }
    }
}
