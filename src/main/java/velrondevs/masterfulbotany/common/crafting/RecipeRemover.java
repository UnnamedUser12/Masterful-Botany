package velrondevs.masterfulbotany.common.crafting;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.config.MasterfulConfig;

public class RecipeRemover {

    public static final Identifier GAIA_PYLON = Identifier.fromNamespaceAndPath("botania", "gaia_pylon");
    public static final Identifier HARD_PYLON = MasterfulBotany.getInstance().id("gaia_pylon");

    private RecipeRemover() {

    }

    public static void removeRecipes(ModifyRecipeJsonsEvent event) {
        if (MasterfulConfig.replaceGaiaRecipe) {
            event.getRecipeJsons().remove(GAIA_PYLON);
        } else {
            event.getRecipeJsons().remove(HARD_PYLON);
        }
    }
}
