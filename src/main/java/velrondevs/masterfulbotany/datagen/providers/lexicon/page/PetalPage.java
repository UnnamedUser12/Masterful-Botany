package velrondevs.masterfulbotany.datagen.providers.lexicon.page;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.datagen.provider.patchouli.content.RecipePage;

public class PetalPage extends RecipePage {

    public PetalPage(ResourceLocation recipe) {
        this(List.of(recipe), null);
    }
    
    private PetalPage(List<ResourceLocation> recipes, @Nullable String caption) {
        super("botania:petal_apothecary", recipes, caption);
    }

    @Override
    protected int lineSkip() {
        return 14;
    }

    @Override
    protected PetalPage withCaption(String caption) {
        return new PetalPage(this.recipes, caption);
    }
}
