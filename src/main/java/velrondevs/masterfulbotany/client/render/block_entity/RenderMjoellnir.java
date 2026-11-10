package velrondevs.masterfulbotany.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.TileMjoellnir;
import velrondevs.masterfulbotany.registry.ModBlocks;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import velrondevs.botania.client.core.helper.RenderHelper;

public class RenderMjoellnir implements BlockEntityRenderer<TileMjoellnir, RenderMjoellnir.State> {

    public static class State extends BlockEntityRenderState {

        public ItemStack stack = ItemStack.EMPTY;
        @Nullable
        public Level level;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@Nonnull TileMjoellnir blockEntity, @Nonnull State state, float partialTicks, @Nonnull Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.stack = blockEntity.getStack();
        state.level = blockEntity.getLevel();
    }

    @Override
    public void submit(@Nonnull State state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState camera) {
        renderHammer(state.level, state.stack, poseStack, collector, state.lightCoords);
    }

    public static void renderHammer(@Nullable Level level, @Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (stack != null && stack.getItem() == ModBlocks.mjoellnir.asItem()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.ZP.rotationDegrees(135));
            RenderHelper.renderItem(stack, ItemDisplayContext.FIXED, poseStack, collector, light, OverlayTexture.NO_OVERLAY, level, 0);
            poseStack.popPose();
        }
    }
}
