package velrondevs.masterfulbotany.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.entity.AlfPixie;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class AlfPixieModel extends EntityModel<AlfPixie> {

    private final ModelPart body;
    private final ModelPart leftWingT;
    private final ModelPart leftWingB;
    private final ModelPart rightWingT;
    private final ModelPart rightWingB;

    public AlfPixieModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild("body");
        this.leftWingT = root.getChild("leftWingT");
        this.leftWingB = root.getChild("leftWingB");
        this.rightWingT = root.getChild("rightWingT");
        this.rightWingB = root.getChild("rightWingB");
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer buffer, int light, int overlay, int color) {
        this.body.render(poseStack, buffer, light, overlay, color);
        this.leftWingT.render(poseStack, buffer, light, overlay, color);
        this.leftWingB.render(poseStack, buffer, light, overlay, color);
        this.rightWingT.render(poseStack, buffer, light, overlay, color);
        this.rightWingB.render(poseStack, buffer, light, overlay, color);
    }

    @Override
    public void setupAnim(@Nonnull AlfPixie entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.rightWingT.yRot = -(Mth.cos(ageInTicks * 1.7F) * 3.1415927F * 0.5F);
        this.leftWingT.yRot = Mth.cos(ageInTicks * 1.7F) * 3.1415927F * 0.5F;
        this.rightWingB.yRot = -(Mth.cos(ageInTicks * 1.7F) * 3.1415927F * 0.25F);
        this.leftWingB.yRot = Mth.cos(ageInTicks * 1.7F) * 3.1415927F * 0.25F;
    }
}
