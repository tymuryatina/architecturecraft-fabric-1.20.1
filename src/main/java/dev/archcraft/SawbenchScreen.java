package dev.archcraft;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SawbenchScreen extends HandledScreen<SawbenchScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    public SawbenchScreen(SawbenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
        playerInventoryTitleY = 72;
    }

    @Override protected void init() {
        super.init();
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        int count = Math.max(1, ArchitectureCraftFabric.getSawbenchShapeCount());
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
            int mode = (handler.getSelectedMode() + count - 1) % count;
            handler.setClientSelectedMode(mode);
            if (client != null && client.interactionManager != null)
                client.interactionManager.clickButton(handler.syncId, mode);
        }).dimensions(x + 8, y + 4, 24, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
            int mode = (handler.getSelectedMode() + 1) % count;
            handler.setClientSelectedMode(mode);
            if (client != null && client.interactionManager != null)
                client.interactionManager.clickButton(handler.syncId, mode);
        }).dimensions(x + backgroundWidth - 32, y + 4, 24, 18).build());
    }

    @Override protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFFC6B39A);
        context.fill(x + 4, y + 26, x + backgroundWidth - 4, y + 75, 0xFF6D5B49);
        context.fill(x + 42, y + 33, x + 62, y + 53, 0xFF302A24);
        context.fill(x + 114, y + 33, x + 134, y + 53, 0xFF302A24);
        // Show a framed, enlarged material swatch resolved from the active model registry.
        // Vanilla, modded block models, and resource-pack overrides share this lookup path.
        context.fill(x + 68, y + 31, x + 94, y + 57, 0xFF302A24);
        net.minecraft.item.ItemStack material = handler.getSlot(0).getStack();
        net.minecraft.client.texture.Sprite materialSprite = MaterialTextureResolver.findParticleSprite(material);
        if (materialSprite != null) {
            context.drawSprite(x + 71, y + 34, 0, 20, 20, materialSprite);
        } else {
            context.fill(x + 71, y + 34, x + 91, y + 54, 0xFF51483E);
        }
        context.drawText(textRenderer, Text.literal("Input"), x + 39, y + 59, 0xFFFFFF, false);
        context.drawText(textRenderer, Text.literal("Material"), x + 68, y + 61, 0xFFFFFF, false);
        if (!material.isEmpty()) {
            String materialName = textRenderer.trimToWidth(material.getName().getString(), 66);
            context.drawText(textRenderer, materialName, x + 68, y + 71, 0xFFF2E5D3, false);
        }
        context.drawText(textRenderer, Text.literal("Output"), x + 108, y + 59, 0xFFFFFF, false);
        context.fill(x + 7, y + 80, x + backgroundWidth - 7, y + backgroundHeight - 7, 0xFF8C7964);
    }

    @Override protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, title, 8, -10, 0x40352A, false);
        context.drawText(textRenderer, playerInventoryTitle, 8, playerInventoryTitleY, 0xFFFFFF, false);
        String shape = ArchitectureCraftFabric.getSawbenchShapeName(handler.getSelectedMode());
        context.drawText(textRenderer, "Shape: " + shape, 8, 26, 0xFFFFFF, false);
    }

    @Override public boolean shouldPause() { return false; }
}
