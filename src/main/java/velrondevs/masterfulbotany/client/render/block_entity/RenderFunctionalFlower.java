package velrondevs.masterfulbotany.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import velrondevs.botania.client.render.block_entity.BotaniaBlockEntityRenderer;
import velrondevs.botania.client.render.block_entity.FloatingFlowerBlockEntityRenderer;
import velrondevs.botania.client.render.block_entity.SpecialFlowerBlockEntityRenderer;
import velrondevs.botania.common.item.equipment.bauble.ManaseerMonocleItem;
import velrondevs.botania.xplat.BotaniaConfig;

public class RenderFunctionalFlower<T extends FunctionalFlowerBase> extends BotaniaBlockEntityRenderer<T> {

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Override
    protected void render(@Nullable T blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, SubmitNodeCollector collector, int light, int overlay) {
        if (blockEntity == null) {
            return;
        }
        if (blockEntity.isFloating() && !BotaniaConfig.client().staticFloaters()) {
            FloatingFlowerBlockEntityRenderer.renderFloatingIsland(blockEntity, partialTicks, poseStack, buffer, overlay);
        }
        if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity view) {
            if (ManaseerMonocleItem.hasMonocle(view)) {
                HitResult ray = Minecraft.getInstance().hitResult;
                if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = ((BlockHitResult) ray).getBlockPos();
                    boolean binding = SpecialFlowerBlockEntityRenderer.hasBindingAttempt(view, blockEntity.getBlockPos());
                    if (blockEntity.getBlockPos().equals(pos) || binding) {
                        poseStack.pushPose();
                        if (binding) {
                            poseStack.translate(0, 0.005, 0);
                        }
                        SpecialFlowerBlockEntityRenderer.renderRadius(blockEntity, poseStack, buffer, blockEntity.getRadius());
                        poseStack.translate(0, 0.002, 0);
                        SpecialFlowerBlockEntityRenderer.renderRadius(blockEntity, poseStack, buffer, blockEntity.getSecondaryRadius());
                        poseStack.popPose();
                    }
                }
            }
        }
    }
}
