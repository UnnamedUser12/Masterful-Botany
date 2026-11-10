package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import velrondevs.botania.common.crafting.ManaInfusionRecipe;

public interface ManaInfusionExtension extends MasterfulRecipeExtension {

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
        this.manaInfusion(this.tagIngredient(input), result, mana);
    }
    
    default void manaInfusion(Ingredient input, ItemStack result, int mana) {
        this.save(this.provider().id(result.getItem(), "mana_infusion"), new ManaInfusionRecipe(result, input, mana, null, null));
    }

}
