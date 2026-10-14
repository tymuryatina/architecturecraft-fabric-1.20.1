package dev.archcraft;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;

/** Persists the source block whose texture should be used by this architectural shape. */
public final class MaterialShapeBlockEntity extends BlockEntity {
    private String materialBlock = "";

    public MaterialShapeBlockEntity(BlockPos pos, BlockState state) {
        super(ArchitectureCraftFabric.MATERIAL_SHAPE_BLOCK_ENTITY, pos, state);
    }

    public String getMaterialBlock() { return materialBlock; }

    /** Called when a sawbench-produced shape item is placed into the world. */
    public void setMaterialIdentifier(Identifier id) {
        String value = id == null ? "" : id.toString();
        if (materialBlock.equals(value)) return;
        materialBlock = value;
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        materialBlock = nbt.getString("MaterialBlock");
    }

    @Override public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!materialBlock.isEmpty()) nbt.putString("MaterialBlock", materialBlock);
    }

    @Override public NbtCompound toInitialChunkDataNbt() { return createNbt(); }

    @Override public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public Identifier getMaterialIdentifier() {
        if (materialBlock.isEmpty()) return null;
        try { return new Identifier(materialBlock); }
        catch (RuntimeException ignored) { return null; }
    }
}
