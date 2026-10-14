package dev.archcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ArchitectureCraftClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (player.isSneaking() && world.getBlockState(hit.getBlockPos()).getBlock() instanceof ArchitectureCraftFabric.SawbenchBlock) {
                if (world.isClient) MinecraftClient.getInstance().setScreen(new SawbenchScreen(hit.getBlockPos()));
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private static final class SawbenchScreen extends Screen {
        private final BlockPos pos;
        private SawbenchScreen(BlockPos pos) { super(Text.literal("ArchitectureCraft Sawbench")); this.pos = pos.toImmutable(); }
        @Override protected void init() {
            int left = width / 2 - 100;
            int top = height / 2 - 55;
            addDrawableChild(ButtonWidget.builder(Text.literal("Beam"), b -> choose(0)).dimensions(left, top, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Post"), b -> choose(1)).dimensions(left, top + 25, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Slab"), b -> choose(2)).dimensions(left, top + 50, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Pillar"), b -> choose(3)).dimensions(left, top + 75, 200, 20).build());
        }
        private void choose(int mode) {
            var buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeVarInt(mode);
            ClientPlayNetworking.send(new Identifier("archcraft", "sawbench_mode"), buf);
            close();
        }
        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackground(context);
            context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 78, 0xFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, "Choose a profile, then use a material on the bench", width / 2, height / 2 + 48, 0xCCCCCC);
            super.render(context, mouseX, mouseY, delta);
        }
        @Override public boolean shouldPause() { return false; }
    }
}
