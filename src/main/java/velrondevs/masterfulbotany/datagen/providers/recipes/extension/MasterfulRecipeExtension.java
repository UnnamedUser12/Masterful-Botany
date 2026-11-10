package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;

public interface MasterfulRecipeExtension extends RecipeExtension {

    default void save(Identifier id, Recipe<?> recipe) {
        this.output().accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }

    default Ingredient tagIngredient(TagKey<Item> tag) {
        return Ingredient.of(this.items().getOrThrow(tag));
    }
}
