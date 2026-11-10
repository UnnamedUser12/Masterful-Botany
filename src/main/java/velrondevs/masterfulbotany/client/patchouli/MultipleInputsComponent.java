package velrondevs.masterfulbotany.client.patchouli;

import com.google.gson.annotations.SerializedName;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import velrondevs.masterfulbotany.common.crafting.InfuserRecipe;
import velrondevs.botania.api.recipe.BotaniaRecipe;
import velrondevs.botania.registry.BotaniaRecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

public class MultipleInputsComponent implements ICustomComponent {

    @SerializedName("recipe_name")
    public String recipeName;

    protected transient List<Ingredient> ingredients;
    protected transient int x;
    protected transient int y;

    public void build(int componentX, int componentY, int pageNum) {
        this.x = componentX != -1 ? componentX : 17;
        this.y = componentY;
        this.ingredients = this.makeIngredients();
    }

    private List<Ingredient> makeIngredients() {
        AtomicReference<Recipe<?>> recipe = new AtomicReference<>(null);
        var holder = BotaniaRecipeTypes.recipeMap(Minecraft.getInstance().level).byKey(ResourceKey.create(Registries.RECIPE, Identifier.parse(this.recipeName)));
        if (holder != null) {
            recipe.set(holder.value());
        }
        if (recipe.get() == null) {
            throw new RuntimeException("Missing recipe: " + this.recipeName);
        } else if (recipe.get() instanceof InfuserRecipe infuser) {
            return infuser.getIngredients();
        } else if (recipe.get() instanceof BotaniaRecipe botaniaRecipe) {
            return botaniaRecipe.getIngredients();
        } else {
            return recipe.get().placementInfo().ingredients();
        }
    }

    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, @Nonnull IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        int widthHalf = (20 * this.ingredients.size()) / 2;
        for (int i = 0; i < this.ingredients.size(); i++) {
            context.renderIngredient(graphics, this.x - widthHalf + 9 + (20 * i), this.y, mouseX, mouseY, this.ingredients.get(i));
        }
    }

    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.Provider registries) {
        this.recipeName = lookup.apply(IVariable.wrap(this.recipeName)).asString();
    }
}
