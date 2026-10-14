package dev.archcraft;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class SawbenchBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private int selectedMode;
    private boolean crafting;

    public SawbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ArchitectureCraftFabric.SAWBENCH_BLOCK_ENTITY, pos, state);
        selectedMode = state.contains(ArchitectureCraftFabric.SawbenchBlock.MODE)
            ? state.get(ArchitectureCraftFabric.SawbenchBlock.MODE) : 0;
    }

    @Override public Text getDisplayName() { return Text.translatable("block.archcraft.sawbench"); }
    @Override public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SawbenchScreenHandler(syncId, playerInventory, this, ScreenHandlerContext.create(world, pos));
    }

    public int getSelectedMode() { return selectedMode; }
    public void setSelectedMode(int mode) {
        if (mode < 0 || mode > 3) return;
        selectedMode = mode;
        if (world != null && !world.isClient) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == ArchitectureCraftFabric.SAWBENCH_BLOCK)
                world.setBlockState(pos, state.with(ArchitectureCraftFabric.SawbenchBlock.MODE, mode), 3);
        }
        // Changing the selected shape must not consume input or produce items.
        // Crafting happens when input is inserted or output space becomes available.
        markDirty();
    }

    @Override public int size() { return inventory.size(); }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        if (!result.isEmpty()) { markDirty(); if (slot == 1) craft(); }
        return result;
    }
    @Override public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(inventory, slot);
        if (!result.isEmpty()) markDirty();
        return result;
    }
    @Override public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();
        if (slot == 0 && !crafting) craft();
    }
    @Override public void clear() { inventory.clear(); markDirty(); }
    @Override public boolean canPlayerUse(PlayerEntity player) {
        return world != null && world.getBlockEntity(pos) == this &&
            player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
    @Override public boolean isValid(int slot, ItemStack stack) {
        if (slot != 0) return false;
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        return path.endsWith("_planks") || path.equals("stone") || path.equals("cobblestone");
    }

    private void craft() {
        if (crafting || world == null || world.isClient) return;
        ItemStack input = inventory.get(0);
        if (input.isEmpty()) return;
        String path = Registries.ITEM.getId(input.getItem()).getPath();
        String material;
        if (path.endsWith("_planks")) material = path.substring(0, path.length() - "_planks".length());
        else if (path.equals("stone") || path.equals("cobblestone")) material = "stone";
        else return;

        String prefix = switch (selectedMode) {
            case 1 -> "post_";
            case 2 -> "slab_";
            case 3 -> "pillar_";
            default -> "beam_";
        };
        Identifier outputId = new Identifier(ArchitectureCraftFabric.MOD_ID, prefix + material);
        if (!Registries.ITEM.containsId(outputId)) return;
        ItemStack output = inventory.get(1);
        if (!output.isEmpty() && (!Registries.ITEM.getId(output.getItem()).equals(outputId) || output.getCount() > output.getMaxCount() - 4)) return;

        crafting = true;
        input.decrement(1);
        if (input.isEmpty()) inventory.set(0, ItemStack.EMPTY);
        if (output.isEmpty()) inventory.set(1, new ItemStack(Registries.ITEM.get(outputId), 4));
        else output.increment(4);
        crafting = false;
        markDirty();
    }

    @Override public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        selectedMode = nbt.contains("SelectedMode") ? nbt.getInt("SelectedMode") : 0;
    }
    @Override public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("SelectedMode", selectedMode);
    }
    @Override public void markDirty() {
        super.markDirty();
    }
}
