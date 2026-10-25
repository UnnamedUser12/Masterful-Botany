package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import java.util.Arrays;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;
import velrondevs.botania.common.lib.BotaniaTags;
import velrondevs.botania.common.crafting.PetalsRecipe;

public interface PetalExtension extends RecipeExtension {

    default Ingredient petal(DyeColor color) {
        return Ingredient.of(switch (color) {
            case WHITE -> BotaniaTags.Items.PETALS_WHITE;
            case ORANGE -> BotaniaTags.Items.PETALS_ORANGE;
            case MAGENTA -> BotaniaTags.Items.PETALS_MAGENTA;
            case LIGHT_BLUE -> BotaniaTags.Items.PETALS_LIGHT_BLUE;
            case YELLOW -> BotaniaTags.Items.PETALS_YELLOW;
            case LIME -> BotaniaTags.Items.PETALS_LIME;
            case PINK -> BotaniaTags.Items.PETALS_PINK;
            case GRAY -> BotaniaTags.Items.PETALS_GRAY;
            case LIGHT_GRAY -> BotaniaTags.Items.PETALS_LIGHT_GRAY;
            case CYAN -> BotaniaTags.Items.PETALS_CYAN;
            case PURPLE -> BotaniaTags.Items.PETALS_PURPLE;
            case BLUE -> BotaniaTags.Items.PETALS_BLUE;
            case BROWN -> BotaniaTags.Items.PETALS_BROWN;
            case GREEN -> BotaniaTags.Items.PETALS_GREEN;
            case RED -> BotaniaTags.Items.PETALS_RED;
            case BLACK -> BotaniaTags.Items.PETALS_BLACK;
        });
    }
    
    default void petalApothecary(ItemLike output, ItemLike... inputs) {
        this.petalApothecary(new ItemStack(output), inputs);
    }

    default void petalApothecary(ItemLike output, Ingredient... inputs) {
        this.petalApothecary(new ItemStack(output), inputs);
    }
    
    default void petalApothecary(ItemStack output, ItemLike... inputs) {
        this.petalApothecary(output, Arrays.stream(inputs).map(Ingredient::of).toArray(Ingredient[]::new));
    }

    default void petalApothecary(ItemStack output, Ingredient... inputs) {
        this.output().accept(this.provider().loc(output.getItem(), "petal_apothecary"), new PetalsRecipe(output, Ingredient.of(BotaniaTags.Items.SEED_APOTHECARY_REAGENT), inputs), null);
    }
}
