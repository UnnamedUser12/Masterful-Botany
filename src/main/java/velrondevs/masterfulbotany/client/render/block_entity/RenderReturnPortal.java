package velrondevs.masterfulbotany.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import velrondevs.botania.client.core.handler.ClientTickHandler;
import velrondevs.botania.client.core.helper.RenderHelper;
import velrondevs.botania.client.render.block_entity.BotaniaBlockEntityRenderer;

public class RenderReturnPortal extends BotaniaBlockEntityRenderer<TileReturnPortal> {

    private static final Identifier SWIRL = Identifier.fromNamespaceAndPath("botania", "block/alfheim_portal_swirl");

    @Override
    protected void render(@Nullable TileReturnPortal blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, SubmitNodeCollector collector, int light, int overlay) {
        float alpha = (float) Math.min(1, (Math.sin(((double) ((float) ClientTickHandler.ticksInGame + partialTicks) / 8d) + 1) / 7d) + 0.7);
        TextureAtlasSprite sprite = RenderHelper.blockSprite(SWIRL);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.translate(0, 0, -0.5);
        poseStack.scale(1/3f, 1/3f, 1/3f);
        poseStack.translate(0, 0, -0.5);
        this.renderPortal(poseStack, buffer, sprite, 0, 0, 3, 3, alpha, overlay);
        poseStack.translate(3, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.renderPortal(poseStack, buffer, sprite, 0, 0, 3, 3, alpha, overlay);
        poseStack.popPose();
    }

    @SuppressWarnings("SameParameterValue")
    private void renderPortal(PoseStack poseStack, MultiBufferSource buffer, TextureAtlasSprite sprite, int x, int y, int width, int height, float alpha, int overlay) {
        VertexConsumer vertex = buffer.getBuffer(Sheets.translucentItemSheet());
        PoseStack.Pose pose = poseStack.last();
        vertex.addVertex(pose, (float) x, (float) (y + height), 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(overlay).setLight(15728880).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertex.addVertex(pose, (float) (x + width), (float) (y + height), 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(overlay).setLight(15728880).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertex.addVertex(pose, (float) (x + width), (float) y, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(overlay).setLight(15728880).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertex.addVertex(pose, (float) x, (float) y, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(overlay).setLight(15728880).setNormal(pose, 1.0F, 0.0F, 0.0F);
    }
}
