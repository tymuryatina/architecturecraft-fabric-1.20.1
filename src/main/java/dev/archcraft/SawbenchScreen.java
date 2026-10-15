package dev.archcraft;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SawbenchScreen extends HandledScreen<SawbenchScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");
    private static final int COLUMNS = 6;
    private static final int ROWS = 4;
    private static final int PAGE_SIZE = COLUMNS * ROWS;
    private int page;

    public SawbenchScreen(SawbenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 222;
        playerInventoryTitleY = 137;
    }

    @Override protected void init() {
        super.init();
        int maxPage = Math.max(0, (ArchitectureCraftFabric.getSawbenchShapeCount() - 1) / PAGE_SIZE);
        page = Math.min(page, maxPage);
    }

    @Override protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFFC6B39A);
        context.fill(x + 4, y + 4, x + backgroundWidth - 4, y + 83, 0xFF6D5B49);
        context.fill(x + 7, y + 7, x + 28, y + 24, 0xFF302A24);
        context.fill(x + backgroundWidth - 28, y + 7, x + backgroundWidth - 7, y + 24, 0xFF302A24);
        context.drawText(textRenderer, Text.literal("‹"), x + 14, y + 8, 0xFFFFFFFF, false);
        context.drawText(textRenderer, Text.literal("›"), x + backgroundWidth - 21, y + 8, 0xFFFFFFFF, false);

        int count = ArchitectureCraftFabric.getSawbenchShapeCount();
        for (int cell = 0; cell < PAGE_SIZE; cell++) {
            int index = page * PAGE_SIZE + cell;
            if (index >= count) break;
            int col = cell % COLUMNS;
            int row = cell / COLUMNS;
            int sx = x + 9 + col * 26;
            int sy = y + 29 + row * 13;
            boolean selected = index == handler.getSelectedMode();
            context.fill(sx - 1, sy - 1, sx + 17, sy + 17, selected ? 0xFFFFD36B : 0xFF302A24);
            context.drawItem(new ItemStack(ArchitectureCraftFabric.getSawbenchShapeBlock(index)), sx, sy);
        }
        context.drawText(textRenderer, Text.literal("Page " + (page + 1) + "/" + (Math.max(1, (count + PAGE_SIZE - 1) / PAGE_SIZE))),
            x + 61, y + 11, 0xFFFFFFFF, false);
        context.fill(x + 7, y + 88, x + backgroundWidth - 7, y + backgroundHeight - 7, 0xFF8C7964);
    }

    @Override protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, title, 8, -10, 0x40352A, false);
        context.drawText(textRenderer, playerInventoryTitle, 8, playerInventoryTitleY, 0xFFFFFF, false);
        String shape = ArchitectureCraftFabric.getSawbenchShapeName(handler.getSelectedMode());
        context.drawText(textRenderer, Text.literal("Selected: " + shape), 8, 76, 0xFFFFFF, false);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (width - backgroundWidth) / 2;
            int y = (height - backgroundHeight) / 2;
            double localX = mouseX - x;
            double localY = mouseY - y;
            int count = ArchitectureCraftFabric.getSawbenchShapeCount();
            int pages = Math.max(1, (count + PAGE_SIZE - 1) / PAGE_SIZE);
            if (localY >= 7 && localY <= 25 && localX >= 7 && localX <= 29) {
                page = Math.max(0, page - 1);
                return true;
            }
            if (localY >= 7 && localY <= 25 && localX >= backgroundWidth - 29 && localX <= backgroundWidth - 6) {
                page = Math.min(pages - 1, page + 1);
                return true;
            }
            if (localY >= 27 && localY < 81 && localX >= 8 && localX < 166) {
                int col = (int) (localX - 9) / 26;
                int row = (int) (localY - 29) / 13;
                if (col >= 0 && col < COLUMNS && row >= 0 && row < ROWS) {
                    int index = page * PAGE_SIZE + row * COLUMNS + col;
                    if (index < count) {
                        handler.setClientSelectedMode(index);
                        if (client != null && client.interactionManager != null)
                            client.interactionManager.clickButton(handler.syncId, index);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }
}
