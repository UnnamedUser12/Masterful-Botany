package velrondevs.masterfulbotany.common.crafting;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import velrondevs.botania.registry.BotaniaRecipeTypes;

public class InfuserRecipe implements Recipe<StackListInput> {

    private static final PlacementInfo NO_PLACEMENT = PlacementInfo.NOT_PLACEABLE;

    private final ItemStackTemplate output;
    private final NonNullList<Ingredient> inputs;
    private final int mana;
    private final int fromColor;
    private final int toColor;

    public InfuserRecipe(ItemStackTemplate output, int mana, int fromColor, int toColor, List<Ingredient> inputs) {
        this.output = output;
        this.mana = mana;
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.inputs = NonNullList.copyOf(inputs);
    }

    @Nonnull
    @Override
    public RecipeType<InfuserRecipe> getType() {
        return ModRecipes.infuser;
    }

    @Nonnull
    @Override
    public RecipeSerializer<InfuserRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Nonnull
    @Override
    public String group() {
        return "";
    }

    @Nonnull
    @Override
    public PlacementInfo placementInfo() {
        return NO_PLACEMENT;
    }

    @Nonnull
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public int getManaUsage() {
        return this.mana;
    }

    public int fromColor() {
        return this.fromColor;
    }

    public int toColor() {
        return this.toColor;
    }

    @Override
    public boolean matches(@Nonnull StackListInput inv, @Nonnull Level level) {
        List<Ingredient> ingredientsMissing = new ArrayList<>(this.inputs);
        IntStream.range(0, inv.size()).boxed().map(inv::getItem).filter(stack -> !stack.isEmpty()).forEach(stack ->
                ingredientsMissing.stream().filter(ingredient -> ingredient.test(stack)).findFirst().ifPresent(ingredientsMissing::remove)
        );
        return ingredientsMissing.isEmpty();
    }

    @Nonnull
    public ItemStack getResultItem() {
        return this.output.create();
    }

    @Nonnull
    public ItemStack getResultItem(@Nonnull HolderLookup.Provider registries) {
        return this.getResultItem();
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull StackListInput container) {
        return this.getResultItem().copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ItemStack result(List<ItemStack> inputs) {
        if (inputs.size() != this.inputs.size()) return ItemStack.EMPTY;
        outer: for (Ingredient item : this.inputs) {
            for (ItemStack stack : inputs) {
                if (item.test(stack))
                    continue outer;
            }
            return ItemStack.EMPTY;
        }
        return this.output.create();
    }

    @Nonnull
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    public static boolean isItemValidInput(Level level, ItemStack stack) {
        for (RecipeHolder<InfuserRecipe> holder : BotaniaRecipeTypes.getAllRecipesFor(level, ModRecipes.infuser)) {
            for (Ingredient ingredient : holder.value().getIngredients()) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public static Pair<InfuserRecipe, ItemStack> getOutput(Level level, List<ItemStack> inputs) {
        if (!inputs.isEmpty()) {
            if (inputs.stream().anyMatch(stack -> stack.getCount() != 1)) {
                return null;
            }
            for (RecipeHolder<InfuserRecipe> holder : BotaniaRecipeTypes.getAllRecipesFor(level, ModRecipes.infuser)) {
                InfuserRecipe infuserRecipe = holder.value();
                ItemStack stack = infuserRecipe.result(inputs);
                if (!stack.isEmpty()) return Pair.of(infuserRecipe, stack.copy());
            }
        }
        return null;
    }

    public static class Serializer {

        public static final MapCodec<InfuserRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStackTemplate.CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                com.mojang.serialization.Codec.INT.fieldOf("mana").forGetter(InfuserRecipe::getManaUsage),
                com.mojang.serialization.Codec.INT.fieldOf("fromColor").forGetter(InfuserRecipe::fromColor),
                com.mojang.serialization.Codec.INT.fieldOf("toColor").forGetter(InfuserRecipe::toColor),
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(InfuserRecipe::getIngredients)
        ).apply(instance, InfuserRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, InfuserRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
                ByteBufCodecs.VAR_INT, InfuserRecipe::getManaUsage,
                ByteBufCodecs.INT, InfuserRecipe::fromColor,
                ByteBufCodecs.INT, InfuserRecipe::toColor,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), InfuserRecipe::getIngredients,
                InfuserRecipe::new
        );

        public static final RecipeSerializer<InfuserRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {

        }
    }
}
