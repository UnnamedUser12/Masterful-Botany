package velrondevs.masterfulbotany.client.render.entity;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.model.AlfPixieModel;
import velrondevs.masterfulbotany.common.entity.AlfPixie;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import velrondevs.botania.client.model.BotaniaModelLayers;
import velrondevs.botania.client.render.entity.PixieRenderer;

public class AlfPixieRender extends MobRenderer<AlfPixie, PixieRenderer.State, AlfPixieModel> {

    private static final Identifier TEXTURE = MasterfulBotany.getInstance().id("textures/entity/alf_pixie.png");

    public AlfPixieRender(EntityRendererProvider.Context context) {
        super(context, new AlfPixieModel(context.bakeLayer(BotaniaModelLayers.PIXIE)), 0);
    }

    @Nonnull
    @Override
    public PixieRenderer.State createRenderState() {
        return new PixieRenderer.State();
    }

    @Nonnull
    @Override
    public Identifier getTextureLocation(@Nonnull PixieRenderer.State state) {
        return TEXTURE;
    }
}
