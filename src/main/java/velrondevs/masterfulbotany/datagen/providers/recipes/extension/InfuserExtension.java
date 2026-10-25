package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.crafting.InfuserRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.crafting.RecipeHelper;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;

public interface InfuserExtension extends RecipeExtension {

    default InfuserRecipeBuilder infuser(ItemLike result) {
        return new InfuserRecipeBuilder(this, new ItemStack(result)).setGroup("infuser");
    }

    default InfuserRecipeBuilder infuser(ItemStack result) {
        return new InfuserRecipeBuilder(this, result).setGroup("infuser");
    }
    
    class InfuserRecipeBuilder {

        private final RecipeExtension ext;
        private final ItemStack result;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private String group;
        private int manaCost = -1;
        private int fromColor = 0xFFFFFF;
        private int toColor = 0xFFFFFF;

        private InfuserRecipeBuilder(RecipeExtension ext, ItemStack result) {
            this.ext = ext;
            this.result = result;
        }

        public InfuserRecipeBuilder addIngredient(TagKey<Item> tag) {
            return this.addIngredient(Ingredient.of(tag));
        }

        public InfuserRecipeBuilder addIngredient(ItemLike item) {
            return this.addIngredient(Ingredient.of(item), 1);
        }

        public InfuserRecipeBuilder addIngredient(ItemLike item, int quantity) {
            for (int i = 0; i < quantity; i++) {
                this.addIngredient(Ingredient.of(item));
            }

            return this;
        }

        public InfuserRecipeBuilder addIngredient(Ingredient ingredient) {
            return this.addIngredient(ingredient, 1);
        }

        public InfuserRecipeBuilder addIngredient(Ingredient ingredient, int quantity) {
            for (int i = 0; i < quantity; i++) {
                this.ingredients.add(ingredient);
            }

            return this;
        }

        public InfuserRecipeBuilder setManaCost(int mana) {
            this.manaCost = mana;
            return this;
        }

        public InfuserRecipeBuilder setColors(int fromColor, int toColor) {
            this.fromColor = fromColor;
            this.toColor = toColor;
            return this;
        }

        public InfuserRecipeBuilder setGroup(String group) {
            this.group = group;
            return this;
        }

        public void build() {
            this.build(this.ext.provider().loc(this.result.getItem()));
        }

        public void build(ResourceLocation id) {
            this.validate(id);
            this.ext.output().accept(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "masterfulbotany_infusion/" + id.getPath()), new InfuserRecipe(this.result, this.manaCost, this.fromColor, this.toColor, this.ingredients), null);
        }

        private void validate(ResourceLocation id) {
            if (this.manaCost < 0) {
                throw new IllegalStateException("No mana cost set for " + id);
            }
        }
    }
}
