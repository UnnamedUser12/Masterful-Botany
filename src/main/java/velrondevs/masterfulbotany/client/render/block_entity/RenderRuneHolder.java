package velrondevs.masterfulbotany.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder;
import velrondevs.masterfulbotany.common.helper.StackHandlers;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.moddingx.libx.render.ClientTickHandler;
import velrondevs.botania.client.core.helper.RenderHelper;

public class RenderRuneHolder implements BlockEntityRenderer<TileRuneHolder, RenderRuneHolder.State> {

    public static class State extends BlockEntityRenderState {

        public ItemStack stack = ItemStack.EMPTY;
        @Nullable
        public BlockPos target;
        public double floatProgress;
        public float partialTicks;
        @Nullable
        public Level level;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@Nonnull TileRuneHolder blockEntity, @Nonnull State state, float partialTicks, @Nonnull Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.stack = StackHandlers.get(blockEntity.getInventory(), 0);
        state.target = blockEntity.getTarget();
        state.floatProgress = blockEntity.getFloatProgress();
        state.partialTicks = partialTicks;
        state.level = blockEntity.getLevel();
    }

    @Override
    public AABB getRenderBoundingBox(TileRuneHolder blockEntity) {
        AABB aabb = new AABB(blockEntity.getBlockPos());
        BlockPos target = blockEntity.getTarget();
        if (target != null) {
            return aabb.expandTowards(target.getX() - blockEntity.getBlockPos().getX(), 0, target.getZ() - blockEntity.getBlockPos().getZ()).inflate(1);
        } else {
            return aabb;
        }
    }

    @Override
    public void submit(@Nonnull State state, @Nonnull PoseStack matrixStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState camera) {
        BlockPos current = state.blockPos;
        ItemStack stack = state.stack;
        if (stack.isEmpty()) {
            return;
        }
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.25, 0.5);
        if (state.target != null && state.floatProgress > 0 && !current.equals(state.target)) {
            BlockPos target = state.target;
            double floatProgress = state.floatProgress;
            matrixStack.translate((target.getX() - current.getX()) * floatProgress, Math.sin(floatProgress * Math.PI), (target.getZ() - current.getZ()) * floatProgress);
            Vector3f vec = new Vector3f(target.getX() - current.getX(), 0, target.getZ() - current.getZ());
            vec.cross(new Vector3f(0, 1, 0));
            vec.normalize();
            AxisAngle4f rotation = new AxisAngle4f((float) (floatProgress * Math.PI), vec);
            matrixStack.mulPose(new Quaternionf(rotation));
        }
        double rot = (BuiltInRegistries.ITEM.getId(stack.getItem()) % 2 == 0 ? 0.04 : -0.04) * (ClientTickHandler.ticksInGame() + state.partialTicks + (7 * current.getX() + 31 * current.getZ() + (current.getZ() == 0 ? 87 : Math.floorMod(current.getX(), current.getZ()))));
        matrixStack.mulPose(Axis.YP.rotationDegrees(31 * current.getX() + 7 * current.getZ()));
        matrixStack.mulPose(Axis.XP.rotationDegrees((float) (10 * Math.sin(rot))));
        matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (10 * Math.cos(rot))));
        matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        float scale = stack.getItem() == ModItems.fimbultyrTablet ? 0.9f : 0.7f;
        matrixStack.scale(scale, scale, scale);
        RenderHelper.renderItem(stack, ItemDisplayContext.FIXED, matrixStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.level, 0);
        matrixStack.popPose();
    }
}
