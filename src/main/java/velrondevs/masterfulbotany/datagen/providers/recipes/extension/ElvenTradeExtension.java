package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import java.util.Arrays;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import velrondevs.botania.common.crafting.ElvenTradeRecipe;

public interface ElvenTradeExtension extends MasterfulRecipeExtension {

    default void elvenTrade(ItemLike output, ItemLike... inputs) {
        this.elvenTrade(new ItemStack(output), inputs);
    }

    default void elvenTrade(ItemLike output, Ingredient... inputs) {
        this.elvenTrade(new ItemStack(output), inputs);
    }
    
    default void elvenTrade(ItemStack output, ItemLike... inputs) {
        this.elvenTrade(output, Arrays.stream(inputs).map(Ingredient::of).toArray(Ingredient[]::new));
    }

    default void elvenTrade(ItemStack output, Ingredient... inputs) {
        this.save(this.provider().id(output.getItem(), "elven_trade"), new ElvenTradeRecipe(new ItemStack[] { output }, inputs));
    }
}
