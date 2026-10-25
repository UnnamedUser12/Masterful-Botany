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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

public class InfuserRecipe implements Recipe<StackListInput> {

    private final ItemStack output;
    private final NonNullList<Ingredient> inputs;
    private final int mana;
    private final int fromColor;
    private final int toColor;

    public InfuserRecipe(ItemStack output, int mana, int fromColor, int toColor, List<Ingredient> inputs) {
        this.output = output;
        this.mana = mana;
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.inputs = NonNullList.copyOf(inputs);
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return ModRecipes.infuser;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
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
        return this.output;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull HolderLookup.Provider registries) {
        return this.getResultItem();
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull StackListInput container, @Nonnull HolderLookup.Provider registries) {
        return this.getResultItem().copy();
    }

    /**
     * Special recipes are not shown in the recipe book (the recipe book has no category for custom recipe types).
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
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
        return this.output.copy();
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Nullable
    public static Pair<InfuserRecipe, ItemStack> getOutput(Level level, List<ItemStack> inputs) {
        if (!inputs.isEmpty()) {
            if (inputs.stream().anyMatch(stack -> stack.getCount() != 1)) {
                return null;
            }
            for (RecipeHolder<InfuserRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.infuser)) {
                InfuserRecipe infuserRecipe = holder.value();
                ItemStack stack = infuserRecipe.result(inputs);
                if (!stack.isEmpty()) return Pair.of(infuserRecipe, stack.copy());
            }
        }
        return null;
    }

    public static class Serializer implements RecipeSerializer<InfuserRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<InfuserRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(InfuserRecipe::getResultItem),
                com.mojang.serialization.Codec.INT.fieldOf("mana").forGetter(InfuserRecipe::getManaUsage),
                com.mojang.serialization.Codec.INT.fieldOf("fromColor").forGetter(InfuserRecipe::fromColor),
                com.mojang.serialization.Codec.INT.fieldOf("toColor").forGetter(InfuserRecipe::toColor),
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(InfuserRecipe::getIngredients)
        ).apply(instance, InfuserRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, InfuserRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, InfuserRecipe::getResultItem,
                ByteBufCodecs.VAR_INT, InfuserRecipe::getManaUsage,
                ByteBufCodecs.INT, InfuserRecipe::fromColor,
                ByteBufCodecs.INT, InfuserRecipe::toColor,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), InfuserRecipe::getIngredients,
                InfuserRecipe::new
        );

        private Serializer() {

        }

        @Nonnull
        @Override
        public MapCodec<InfuserRecipe> codec() {
            return CODEC;
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, InfuserRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
