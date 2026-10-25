package velrondevs.masterfulbotany.common.crafting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public class RecipeRemover {

    public static final ResourceLocation GAIA_PYLON = ResourceLocation.fromNamespaceAndPath("botania", "gaia_pylon");
    public static final ResourceLocation HARD_PYLON = MasterfulBotany.getInstance().resource("gaia_pylon");

    private RecipeRemover() {

    }

    public static void removeRecipes(RecipeManager rm) {
        Set<ResourceLocation> recipesToRemove = new HashSet<>();
        if (MasterfulConfig.replaceGaiaRecipe) {
            recipesToRemove.add(GAIA_PYLON);
        } else {
            recipesToRemove.add(HARD_PYLON);
        }

        List<RecipeHolder<?>> remaining = rm.getRecipes().stream().filter(holder -> !recipesToRemove.contains(holder.id())).toList();
        if (remaining.size() != rm.getRecipes().size()) {
            rm.replaceRecipes(remaining);
        }
    }
}
