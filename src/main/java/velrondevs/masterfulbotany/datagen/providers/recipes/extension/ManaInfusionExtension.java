package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;
import velrondevs.botania.common.crafting.ManaInfusionRecipe;

public interface ManaInfusionExtension extends RecipeExtension {

    default void manaInfusion(ItemLike input, ItemLike result, int mana) {
        this.manaInfusion(input, new ItemStack(result), mana);
    }

    default void manaInfusion(TagKey<Item> input, ItemLike result, int mana) {
        this.manaInfusion(input, new ItemStack(result), mana);
    }

    default void manaInfusion(Ingredient input, ItemLike result, int mana) {
        this.manaInfusion(input, new ItemStack(result), mana);
    }
    
    default void manaInfusion(ItemLike input, ItemStack result, int mana) {
        this.manaInfusion(Ingredient.of(input), result, mana);
    }
    
    default void manaInfusion(TagKey<Item> input, ItemStack result, int mana) {
        this.manaInfusion(Ingredient.of(input), result, mana);
    }
    
    default void manaInfusion(Ingredient input, ItemStack result, int mana) {
        this.output().accept(this.provider().loc(result.getItem(), "mana_infusion"), new ManaInfusionRecipe(result, input, mana, null, null), null);
    }

}
