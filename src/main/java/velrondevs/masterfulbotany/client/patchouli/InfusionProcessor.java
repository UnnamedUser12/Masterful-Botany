package velrondevs.masterfulbotany.client.patchouli;

import velrondevs.masterfulbotany.common.crafting.InfuserRecipe;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import velrondevs.botania.registry.BotaniaRecipeTypes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class InfusionProcessor implements IComponentProcessor {

    protected InfuserRecipe recipe;
    protected Identifier recipeId;

    public InfusionProcessor() {

    }

    @Override
    public void setup(Level level, IVariableProvider variables) {
        Identifier id = Identifier.parse(variables.get("recipe", level.registryAccess()).asString());
        this.recipe = null;
        this.recipeId = null;
        RecipeHolder<?> holder = BotaniaRecipeTypes.recipeMap(level).byKey(ResourceKey.create(Registries.RECIPE, id));
        if (holder == null) {
            MasterfulBotany.logger.warn("Missing masterfulbotany infusion recipe: " + id);
        } else if (!(holder.value() instanceof InfuserRecipe infuserRecipe)) {
            MasterfulBotany.logger.warn("Recipe is not a masterfulbotany infusion recipe: " + id);
        } else {
            this.recipe = infuserRecipe;
            this.recipeId = id;
        }
    }

    @Override
    public IVariable process(Level level, String key) {
        if (this.recipe == null) {
            return null;
        } else {
            return switch (key) {
                case "output" -> IVariable.from(this.recipe.getResultItem(level.registryAccess()), level.registryAccess());
                case "recipe" -> IVariable.wrap(this.recipeId.toString());
                case "heading" -> IVariable.from(this.recipe.getResultItem(level.registryAccess()).getHoverName(), level.registryAccess());
                case "mana" -> IVariable.wrap(this.recipe.getManaUsage());
                default -> null;
            };
        }
    }
}
