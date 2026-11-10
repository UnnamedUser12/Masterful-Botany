package velrondevs.masterfulbotany.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.client.render.block_entity.RenderMjoellnir;
import velrondevs.masterfulbotany.common.entity.Mjoellnir;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RenderEntityMjoellnir extends EntityRenderer<Mjoellnir, RenderEntityMjoellnir.State> {

    public static class State extends EntityRenderState {

        public ItemStack stack = ItemStack.EMPTY;
        public boolean returning;
        public Vec3 motion = Vec3.ZERO;
        @Nullable
        public Level level;
    }

    public RenderEntityMjoellnir(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nonnull
    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@Nonnull Mjoellnir entity, @Nonnull State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.stack = entity.getStack();
        state.returning = entity.isReturning();
        state.motion = entity.getDeltaMovement();
        state.level = entity.level();
    }

    @Override
    public void submit(@Nonnull State state, @Nonnull PoseStack matrixStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState camera) {
        super.submit(state, matrixStack, collector, camera);
        matrixStack.pushPose();
        matrixStack.translate(0, 0.3, 0);
        Vec3 motion = state.motion;
        if (motion.z != 0) {
            float r = (float) (Math.atan2(motion.x, motion.z) + (Math.PI / 2));
            if (!Float.isNaN(r)) {
                matrixStack.mulPose(Axis.YP.rotation(r));
            }
        }
        if (state.returning) {
            matrixStack.scale(0.7f, 0.7f, 0.7f);
        } else {
            double g = Math.sqrt((motion.x * motion.x) + (motion.z * motion.z));
            matrixStack.mulPose(Axis.ZP.rotation((float) (Math.atan2(g, motion.y) + Math.PI)));
        }
        matrixStack.translate(-0.5, -0.5, -0.5);
        RenderMjoellnir.renderHammer(state.level, state.stack, matrixStack, collector, state.lightCoords);
        matrixStack.popPose();
    }
}
