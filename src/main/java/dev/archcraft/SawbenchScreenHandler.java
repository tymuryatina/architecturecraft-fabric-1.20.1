package dev.archcraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public final class SawbenchScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final ScreenHandlerContext context;
    private int selectedMode;

    public SawbenchScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(2), ScreenHandlerContext.EMPTY);
    }

    public SawbenchScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, ScreenHandlerContext context) {
        super(ArchitectureCraftFabric.SAWBENCH_SCREEN_HANDLER, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;
        this.context = context;
        if (inventory instanceof SawbenchBlockEntity bench) selectedMode = bench.getSelectedMode();
        inventory.onOpen(playerInventory.player);

        addSlot(new Slot(inventory, 0, 44, 35) {
            @Override public boolean canInsert(ItemStack stack) { return inventory.isValid(0, stack); }
        });
        addSlot(new Slot(inventory, 1, 116, 35) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; ++col)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
    }

    public int getSelectedMode() { return selectedMode; }
    public void setClientSelectedMode(int mode) { selectedMode = mode; }

    @Override public boolean onButtonClick(PlayerEntity player, int id) {
        if (id < 0 || id >= ArchitectureCraftFabric.getSawbenchShapeCount()) return false;
        selectedMode = id;
        context.run((world, pos) -> {
            if (world.getBlockEntity(pos) instanceof SawbenchBlockEntity bench) bench.setSelectedMode(id);
        });
        return true;
    }

    @Override public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player) && ScreenHandler.canUse(context, player, ArchitectureCraftFabric.SAWBENCH_BLOCK);
    }

    @Override public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasStack()) return original;
        ItemStack stack = slot.getStack();
        original = stack.copy();
        if (slotIndex == 1) {
            if (!insertItem(stack, 2, 38, true)) return ItemStack.EMPTY;
            slot.onQuickTransfer(stack, original);
        } else if (slotIndex != 0) {
            if (inventory.isValid(0, stack)) {
                if (!insertItem(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if (slotIndex < 29) {
                if (!insertItem(stack, 29, 38, false)) return ItemStack.EMPTY;
            } else if (!insertItem(stack, 2, 29, false)) return ItemStack.EMPTY;
        } else if (!insertItem(stack, 2, 38, false)) return ItemStack.EMPTY;

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, stack);
        return original;
    }

    @Override public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }
}
