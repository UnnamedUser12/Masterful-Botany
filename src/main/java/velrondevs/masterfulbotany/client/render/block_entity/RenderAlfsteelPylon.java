package velrondevs.masterfulbotany.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.TileAlfsteelPylon;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.render.ClientTickHandler;
import velrondevs.botania.client.core.helper.CoreShaders;
import velrondevs.botania.client.model.BotaniaModelLayers;
import velrondevs.botania.client.model.NaturaPylonModel;
import velrondevs.botania.client.render.block_entity.BotaniaBlockEntityRenderer;
import velrondevs.botania.client.render.block_entity.TEISR;

public class RenderAlfsteelPylon extends BotaniaBlockEntityRenderer<TileAlfsteelPylon> {

    public static final Identifier TEXTURE = MasterfulBotany.getInstance().id("textures/model/pylon_alfsteel.png");

    private static RenderType target;
    private static RenderType directTarget;
    private static ItemDisplayContext forceTransform = ItemDisplayContext.NONE;

    private final NaturaPylonModel model;

    public RenderAlfsteelPylon(BlockEntityRendererProvider.Context ctx) {
        this.model = new NaturaPylonModel(ctx.bakeLayer(BotaniaModelLayers.PYLON_NATURA));
    }

    @Override
    protected void render(@Nullable TileAlfsteelPylon pylon, float pticks, PoseStack poseStack, MultiBufferSource buffer, SubmitNodeCollector collector, int light, int overlay) {
        boolean direct = pylon == null && (forceTransform == ItemDisplayContext.GUI || forceTransform.firstPerson());
        RenderType glow = glowType(direct);

        poseStack.pushPose();
        float worldTime = (float) ClientTickHandler.ticksInGame() + pticks;
        worldTime += pylon == null ? 0.0F : (float) (new Random(pylon.getBlockPos().hashCode())).nextInt(360);
        poseStack.translate(0.0D, pylon == null ? 1.35D : 1.5D, 0.0D);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, -0.5D);
        if (pylon != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees(worldTime * 1.5F));
        }

        VertexConsumer vertex = buffer.getBuffer(RenderTypes.entityTranslucent(TEXTURE));
        this.model.renderRing(poseStack, vertex, light, overlay);
        if (pylon != null) {
            poseStack.translate(0.0D, Math.sin((double) worldTime / 20.0D) / 20.0D - 0.025D, 0.0D);
        }

        poseStack.popPose();
        poseStack.pushPose();
        if (pylon != null) {
            poseStack.translate(0.0D, Math.sin((double) worldTime / 20.0D) / 17.5D, 0.0D);
        }

        poseStack.translate(0.5D, 0.0D, -0.5D);
        if (pylon != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-worldTime));
        }

        vertex = buffer.getBuffer(glow);
        this.model.renderCrystal(poseStack, vertex, light, overlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    public static class ItemRenderer extends TEISR {

        public ItemRenderer(Block block) {
            super(block);
        }

        @Override
        public void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay) {
            forceTransform = context;
            super.render(stack, context, poseStack, collector, light, overlay);
        }
    }

    private static synchronized RenderType glowType(boolean direct) {
        if (target == null) {
            target = createRenderType(false);
            directTarget = createRenderType(true);
        }
        return direct ? directTarget : target;
    }

    private static RenderType createRenderType(boolean direct) {
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(CoreShaders.pylon())
                .withTexture("Sampler0", TEXTURE)
                .useLightmap()
                .useOverlay();
        if (!direct) {
            builder = builder.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET);
        }
        return RenderType.create("masterfulbotany:pylon" + (direct ? "_direct" : ""), builder.createRenderSetup());
    }
}
