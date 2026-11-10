package velrondevs.masterfulbotany.datagen.providers.lexicon.page;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import org.moddingx.libx.datagen.provider.patchouli.content.RecipePage;

public class InfuserPage extends RecipePage {

    public InfuserPage(Identifier recipe) {
        this(List.of(recipe), null);
    }
    
    private InfuserPage(List<Identifier> recipes, @Nullable String caption) {
        super("botania:masterfulbotany_infuser", recipes, caption);
    }

    @Override
    protected int lineSkip() {
        return 14;
    }

    @Override
    protected InfuserPage withCaption(String caption) {
        return new InfuserPage(this.recipes, caption);
    }
}
