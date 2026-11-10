package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.crafting.InfuserRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface InfuserExtension extends MasterfulRecipeExtension {

    default InfuserRecipeBuilder infuser(ItemLike result) {
        return new InfuserRecipeBuilder(this, new ItemStack(result)).setGroup("infuser");
    }

    default InfuserRecipeBuilder infuser(ItemStack result) {
        return new InfuserRecipeBuilder(this, result).setGroup("infuser");
    }
    
    class InfuserRecipeBuilder {

        private final MasterfulRecipeExtension ext;
        private final ItemStack result;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private String group;
        private int manaCost = -1;
        private int fromColor = 0xFFFFFF;
        private int toColor = 0xFFFFFF;

        private InfuserRecipeBuilder(MasterfulRecipeExtension ext, ItemStack result) {
            this.ext = ext;
            this.result = result;
        }

        public InfuserRecipeBuilder addIngredient(TagKey<Item> tag) {
            return this.addIngredient(this.ext.tagIngredient(tag));
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
            this.build(this.ext.provider().id(this.result.getItem()));
        }

        public void build(Identifier id) {
            this.validate(id);
            this.ext.save(Identifier.fromNamespaceAndPath(id.getNamespace(), "masterfulbotany_infusion/" + id.getPath()), new InfuserRecipe(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(this.result), this.manaCost, this.fromColor, this.toColor, this.ingredients));
        }

        private void validate(Identifier id) {
            if (this.manaCost < 0) {
                throw new IllegalStateException("No mana cost set for " + id);
            }
        }
    }
}
