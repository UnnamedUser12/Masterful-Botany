package velrondevs.masterfulbotany.client.render.entity;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.model.AlfPixieModel;
import velrondevs.masterfulbotany.common.entity.AlfPixie;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import velrondevs.botania.client.model.BotaniaModelLayers;

public class AlfPixieRender extends MobRenderer<AlfPixie, AlfPixieModel> {
    
    private static final ResourceLocation TEXTURE = MasterfulBotany.getInstance().resource("textures/entity/alf_pixie.png");

    public AlfPixieRender(EntityRendererProvider.Context context) {
        super(context, new AlfPixieModel(context.bakeLayer(BotaniaModelLayers.PIXIE)), 0);
    }
    
    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull AlfPixie entity) {
        return TEXTURE;
    }
}
