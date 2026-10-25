package velrondevs.masterfulbotany.common.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RuneRitualRecipe implements Recipe<StackListInput> {

    private static final Map<ResourceLocation, SpecialRuneInput> specialInputs = new HashMap<>();
    private static final Map<ResourceLocation, SpecialRuneOutput> specialOutputs = new HashMap<>();

    public static void registerSpecialInput(SpecialRuneInput action) {
        synchronized (specialInputs) {
            if (specialInputs.containsKey(action.id)) {
                throw new IllegalStateException("Special rune ritual input registered twice: " + action.id);
            }
            specialInputs.put(action.id, action);
        }
    }

    public static void registerSpecialOutput(SpecialRuneOutput action) {
        synchronized (specialOutputs) {
            if (specialOutputs.containsKey(action.id)) {
                throw new IllegalStateException("Special rune ritual output registered twice: " + action.id);
            }
            specialOutputs.put(action.id, action);
        }
    }

    private final Ingredient centerRune;
    private final List<RunePosition> runes;
    private final int mana;
    private final int ticks;
    private final List<Ingredient> inputs;
    private final List<ItemStack> outputs;
    @Nullable
    private final ResourceLocation specialInputId;
    @Nullable
    private final ResourceLocation specialOutputId;

    public RuneRitualRecipe(Ingredient centerRune, List<RunePosition> runes, int mana, int ticks, List<Ingredient> inputs, List<ItemStack> outputs, @Nullable ResourceLocation specialInputId, @Nullable ResourceLocation specialOutputId) {
        this.centerRune = centerRune;
        this.runes = ImmutableList.copyOf(runes);
        this.mana = mana;
        this.ticks = ticks;
        this.inputs = ImmutableList.copyOf(inputs);
        this.outputs = ImmutableList.copyOf(outputs);
        this.specialInputId = specialInputId;
        this.specialOutputId = specialOutputId;
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return ModRecipes.runeRitual;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public boolean matches(@Nonnull StackListInput inv, @Nonnull Level level) {
        return false;
    }

    @Nonnull
    public ItemStack getResultItem() {
        if (this.outputs.size() == 1) {
            return this.outputs.get(0);
        }
        return ItemStack.EMPTY;
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

    public Ingredient getCenterRune() {
        return this.centerRune;
    }

    public List<RunePosition> getRunes() {
        return this.runes;
    }

    public int getMana() {
        return this.mana;
    }

    public int getTicks() {
        return this.ticks;
    }

    public List<Ingredient> getInputs() {
        return this.inputs;
    }

    public List<ItemStack> getOutputs() {
        return this.outputs;
    }

    @Nullable
    public SpecialRuneInput getSpecialInput() {
        if (this.specialInputId == null) return null;
        synchronized (specialInputs) {
            SpecialRuneInput input = specialInputs.get(this.specialInputId);
            if (input == null) {
                throw new IllegalStateException("Unknown special rune input: " + this.specialInputId);
            }
            return input;
        }
    }

    @Nullable
    public SpecialRuneOutput getSpecialOutput() {
        if (this.specialOutputId == null) return null;
        synchronized (specialOutputs) {
            SpecialRuneOutput output = specialOutputs.get(this.specialOutputId);
            if (output == null) {
                throw new IllegalStateException("Unknown special rune output: " + this.specialOutputId);
            }
            return output;
        }
    }

    public static class RunePosition {

        public static final Codec<RunePosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("rune").forGetter(RunePosition::getRune),
                Codec.INT.fieldOf("x").forGetter(RunePosition::getX),
                Codec.INT.fieldOf("z").forGetter(RunePosition::getZ),
                Codec.BOOL.optionalFieldOf("consume", false).forGetter(RunePosition::isConsumed)
        ).apply(instance, RunePosition::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RunePosition> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, RunePosition::getRune,
                ByteBufCodecs.VAR_INT, RunePosition::getX,
                ByteBufCodecs.VAR_INT, RunePosition::getZ,
                ByteBufCodecs.BOOL, RunePosition::isConsumed,
                RunePosition::new
        );

        private static final int HFLIP = 1;
        private static final int VFLIP = 1 << 1;
        private static final int ROTATE = 1 << 2;

        private final Ingredient rune;
        private final int x;
        private final int z;
        private final int[] xcoords;
        private final int[] zcoords;
        private final boolean consume;

        public RunePosition(Ingredient rune, int x, int z, boolean consume) {
            this.rune = rune;
            this.x = x;
            this.z = z;
            this.consume = (x == 0 && z == 0) || consume;
            this.xcoords = new int[8];
            this.zcoords = new int[8];
            this.xcoords[0] = x;
            this.zcoords[0] = z;
            this.xcoords[HFLIP] = -x;
            this.zcoords[HFLIP] = z;
            this.xcoords[VFLIP] = x;
            this.zcoords[VFLIP] = -z;
            this.xcoords[HFLIP | VFLIP] = -x;
            this.zcoords[HFLIP | VFLIP] = -z;
            for (int i = 0; i < (HFLIP | VFLIP); i++) {
                this.xcoords[i | ROTATE] = -this.zcoords[i];
                this.zcoords[i | ROTATE] = this.xcoords[i];
            }
        }

        public Ingredient getRune() {
            return this.rune;
        }

        public int getX() {
            return this.x;
        }

        public int getZ() {
            return this.z;
        }

        public boolean isConsumed() {
            return this.consume;
        }

        public int getX(int transformIdx) {
            if (transformIdx < 0 || transformIdx >= 8) {
                return this.getX();
            } else {
                return this.xcoords[transformIdx];
            }
        }

        public int getZ(int transformIdx) {
            if (transformIdx < 0 || transformIdx >= 8) {
                return this.getZ();
            } else {
                return this.zcoords[transformIdx];
            }
        }
    }

    public static class Serializer implements RecipeSerializer<RuneRitualRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<RuneRitualRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("center").forGetter(RuneRitualRecipe::getCenterRune),
                RunePosition.CODEC.listOf().fieldOf("runes").forGetter(RuneRitualRecipe::getRunes),
                Codec.INT.optionalFieldOf("mana", 0).forGetter(RuneRitualRecipe::getMana),
                Codec.INT.optionalFieldOf("ticks", 200).forGetter(RuneRitualRecipe::getTicks),
                Ingredient.CODEC.listOf().optionalFieldOf("inputs", List.of()).forGetter(RuneRitualRecipe::getInputs),
                ItemStack.STRICT_CODEC.listOf().optionalFieldOf("outputs", List.of()).forGetter(RuneRitualRecipe::getOutputs),
                ResourceLocation.CODEC.optionalFieldOf("special_input").forGetter(r -> Optional.ofNullable(r.specialInputId)),
                ResourceLocation.CODEC.optionalFieldOf("special_output").forGetter(r -> Optional.ofNullable(r.specialOutputId))
        ).apply(instance, (center, runes, mana, ticks, inputs, outputs, specialInput, specialOutput) ->
                new RuneRitualRecipe(center, runes, mana, ticks, inputs, outputs, specialInput.orElse(null), specialOutput.orElse(null))));

        public static final StreamCodec<RegistryFriendlyByteBuf, RuneRitualRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        private Serializer() {

        }

        @Nonnull
        @Override
        public MapCodec<RuneRitualRecipe> codec() {
            return CODEC;
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RuneRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static RuneRitualRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient centerRune = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            List<RunePosition> runes = RunePosition.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
            int mana = buffer.readVarInt();
            int ticks = buffer.readVarInt();
            List<Ingredient> inputs = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
            List<ItemStack> outputs = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
            ResourceLocation specialInputId = buffer.readOptional(net.minecraft.network.FriendlyByteBuf::readResourceLocation).orElse(null);
            ResourceLocation specialOutputId = buffer.readOptional(net.minecraft.network.FriendlyByteBuf::readResourceLocation).orElse(null);
            return new RuneRitualRecipe(centerRune, runes, mana, ticks, inputs, outputs, specialInputId, specialOutputId);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, RuneRitualRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getCenterRune());
            RunePosition.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getRunes());
            buffer.writeVarInt(recipe.getMana());
            buffer.writeVarInt(recipe.getTicks());
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getInputs());
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getOutputs());
            buffer.writeOptional(Optional.ofNullable(recipe.specialInputId), net.minecraft.network.FriendlyByteBuf::writeResourceLocation);
            buffer.writeOptional(Optional.ofNullable(recipe.specialOutputId), net.minecraft.network.FriendlyByteBuf::writeResourceLocation);
        }
    }
}
