package velrondevs.masterfulbotany.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.TileYggdrasilBranch;
import velrondevs.masterfulbotany.common.helper.StackHandlers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.moddingx.libx.render.block.RotatedBlockRenderer;
import org.moddingx.libx.util.lazy.LazyValue;
import velrondevs.botania.client.core.helper.RenderHelper;
import velrondevs.botania.registry.BotaniaItems;

public class RenderYggdrasilBranch extends RotatedBlockRenderer<TileYggdrasilBranch, RenderYggdrasilBranch.State> {

    public static class State extends RotatedBlockRenderer.RenderState {

        public ItemStack stack = ItemStack.EMPTY;
        @Nullable
        public Level level;
    }

    private final LazyValue<ItemStack> twig = new LazyValue<>(() -> new ItemStack(BotaniaItems.livingwoodTwig));

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@Nonnull TileYggdrasilBranch blockEntity, @Nonnull State renderState, float partialTicks, @Nonnull Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTicks, cameraPos, breakProgress);
        renderState.stack = StackHandlers.get(blockEntity.getInventory(), 0);
        renderState.level = blockEntity.getLevel();
    }

    @Override
    protected void doRender(@Nonnull State state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.9, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(160));
        poseStack.scale(0.8f, 0.8f, 0.8f);
        RenderHelper.renderItem(this.twig.get(), ItemDisplayContext.GROUND, poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.level, 0);
        poseStack.popPose();
        if (!state.stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.1, 0.25);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            RenderHelper.renderItem(state.stack, ItemDisplayContext.GROUND, poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.level, 0);
            poseStack.popPose();
        }
    }
}
