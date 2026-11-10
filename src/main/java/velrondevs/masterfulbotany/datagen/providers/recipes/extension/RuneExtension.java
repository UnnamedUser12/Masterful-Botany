package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import java.util.Arrays;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import velrondevs.botania.common.crafting.RunicAltarRecipe;

public interface RuneExtension extends MasterfulRecipeExtension {

    default void runeAltar(ItemLike output, int mana, ItemLike... inputs) {
        this.runeAltar(new ItemStack(output), mana, inputs);
    }

    default void runeAltar(ItemLike output, int mana, Ingredient... inputs) {
        this.runeAltar(new ItemStack(output), mana, inputs);
    }

    default void runeAltar(ItemStack output, int mana, ItemLike... inputs) {
        this.runeAltar(output, mana, Arrays.stream(inputs).map(Ingredient::of).toArray(Ingredient[]::new));
    }

    default void runeAltar(ItemStack output, int mana, Ingredient... inputs) {
        this.save(this.provider().id(output.getItem(), "runic_altar"), new RunicAltarRecipe(output, mana, inputs));
    }
}
