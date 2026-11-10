package velrondevs.masterfulbotany.datagen.providers.lexicon.page;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import org.moddingx.libx.datagen.provider.patchouli.content.RecipePage;

public class TradePage extends RecipePage {

    public TradePage(Identifier... recipes) {
        this(List.of(recipes), null);
    }
    
    private TradePage(List<Identifier> recipes, @Nullable String caption) {
        super("botania:elven_trade", recipes, caption);
    }

    @Override
    protected int lineSkip() {
        return 14;
    }

    @Override
    protected void addRecipeKey(JsonObject json) {
        JsonArray array = new JsonArray();
        for (Identifier recipe : this.recipes) {
            array.add(recipe.toString());
        }
        json.add("recipes", array);
    }

    @Override
    protected TradePage withCaption(String caption) {
        return new TradePage(this.recipes, caption);
    }
}
