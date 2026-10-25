package velrondevs.masterfulbotany.datagen.providers.recipes.extension;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.crafting.RuneRitualRecipe;
import velrondevs.masterfulbotany.common.crafting.SpecialRuneInput;
import velrondevs.masterfulbotany.common.crafting.SpecialRuneOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.crafting.RecipeHelper;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;

public interface RuneRitualExtension extends RecipeExtension {

    default RuneRitualRecipeBuilder runeRitual(ItemLike centerRune) {
        return new RuneRitualRecipeBuilder(this, Ingredient.of(centerRune));
    }

    default RuneRitualRecipeBuilder runeRitual(TagKey<Item> centerRune) {
        return new RuneRitualRecipeBuilder(this, Ingredient.of(centerRune));
    }

    default RuneRitualRecipeBuilder runeRitual(Ingredient centerRune) {
        return new RuneRitualRecipeBuilder(this, centerRune);
    }
    
    class RuneRitualRecipeBuilder {

        private final RecipeExtension ext;
        private final Ingredient centerRune;
        private final List<RuneRitualRecipe.RunePosition> runes = new ArrayList<>();
        private int manaCost = 0;
        private int tickTime = 200;
        private final List<Ingredient> inputs = new ArrayList<>();
        private final List<ItemStack> outputs = new ArrayList<>();
        @Nullable
        private SpecialRuneInput specialInput;
        @Nullable
        private SpecialRuneOutput specialOutput;

        private RuneRitualRecipeBuilder(RecipeExtension ext, Ingredient centerRune) {
            this.ext = ext;
            this.centerRune = centerRune;
        }

        public RuneRitualRecipeBuilder rune(ItemLike rune, int x, int z, boolean consume) {
            return this.rune(Ingredient.of(rune), x, z, consume);
        }

        public RuneRitualRecipeBuilder rune(TagKey<Item> rune, int x, int z, boolean consume) {
            return this.rune(Ingredient.of(rune), x, z, consume);
        }

        public RuneRitualRecipeBuilder rune(Ingredient rune, int x, int z, boolean consume) {
            if (x < -5 || x > 5 || z < -5 || z > 5) {
                throw new IllegalStateException("Rune positions should not be more than 5 blocks away frm the central rune holder: (" + x + "," + z + ")");
            }
            this.runes.add(new RuneRitualRecipe.RunePosition(rune, x, z, consume));
            return this;
        }

        public RuneRitualRecipeBuilder rune4(ItemLike rune, int x, int z, boolean consume) {
            if (x == 0) {
                this.rune(rune, 0, -z, consume);
                this.rune(rune, 0, z, consume);
                this.rune(rune, -z, 0, consume);
                this.rune(rune, z, 0, consume);
            } else if (z == 0) {
                this.rune(rune, -x, 0, consume);
                this.rune(rune, x, 0, consume);
                this.rune(rune, 0, -x, consume);
                this.rune(rune, 0, x, consume);
            } else {
                this.rune(rune, -x, -z, consume);
                this.rune(rune, -x, z, consume);
                this.rune(rune, x, -z, consume);
                this.rune(rune, x, z, consume);
            }
            return this;
        }

        public RuneRitualRecipeBuilder rune4(TagKey<Item> rune, int x, int z, boolean consume) {
            if (x == 0) {
                this.rune(rune, 0, -z, consume);
                this.rune(rune, 0, z, consume);
                this.rune(rune, -z, 0, consume);
                this.rune(rune, z, 0, consume);
            } else if (z == 0) {
                this.rune(rune, -x, 0, consume);
                this.rune(rune, x, 0, consume);
                this.rune(rune, 0, -x, consume);
                this.rune(rune, 0, x, consume);
            } else {
                this.rune(rune, -x, -z, consume);
                this.rune(rune, -x, z, consume);
                this.rune(rune, x, -z, consume);
                this.rune(rune, x, z, consume);
            }
            return this;
        }

        public RuneRitualRecipeBuilder rune4(Ingredient rune, int x, int z, boolean consume) {
            if (x == 0) {
                this.rune(rune, 0, -z, consume);
                this.rune(rune, 0, z, consume);
                this.rune(rune, -z, 0, consume);
                this.rune(rune, z, 0, consume);
            } else if (z == 0) {
                this.rune(rune, -x, 0, consume);
                this.rune(rune, x, 0, consume);
                this.rune(rune, 0, -x, consume);
                this.rune(rune, 0, x, consume);
            } else {
                this.rune(rune, -x, -z, consume);
                this.rune(rune, -x, z, consume);
                this.rune(rune, x, -z, consume);
                this.rune(rune, x, z, consume);
            }
            return this;
        }

        public RuneRitualRecipeBuilder rune2(ItemLike rune, int x, int z, boolean consume) {
            this.rune(rune, -x, -z, consume);
            this.rune(rune, x, z, consume);
            return this;
        }

        public RuneRitualRecipeBuilder rune2(TagKey<Item> rune, int x, int z, boolean consume) {
            this.rune(rune, -x, -z, consume);
            this.rune(rune, x, z, consume);
            return this;
        }

        public RuneRitualRecipeBuilder rune2(Ingredient rune, int x, int z, boolean consume) {
            this.rune(rune, -x, -z, consume);
            this.rune(rune, x, z, consume);
            return this;
        }

        public RuneRitualRecipeBuilder rune(ItemLike rune, int x, int z) {
            return this.rune(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune(TagKey<Item> rune, int x, int z) {
            return this.rune(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune(Ingredient rune, int x, int z) {
            return this.rune(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune4(ItemLike rune, int x, int z) {
            return this.rune4(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune4(TagKey<Item> rune, int x, int z) {
            return this.rune4(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune4(Ingredient rune, int x, int z) {
            return this.rune4(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune2(ItemLike rune, int x, int z) {
            return this.rune2(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune2(TagKey<Item> rune, int x, int z) {
            return this.rune2(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder rune2(Ingredient rune, int x, int z) {
            return this.rune2(rune, x, z, false);
        }

        public RuneRitualRecipeBuilder mana(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public RuneRitualRecipeBuilder time(int tickTime) {
            this.tickTime = tickTime;
            return this;
        }

        public RuneRitualRecipeBuilder input(ItemLike input) {
            return this.input(Ingredient.of(input));
        }

        public RuneRitualRecipeBuilder input(TagKey<Item> input) {
            return this.input(Ingredient.of(input));
        }

        public RuneRitualRecipeBuilder input(Ingredient input) {
            this.inputs.add(input);
            return this;
        }

        public RuneRitualRecipeBuilder output(ItemLike output) {
            return this.output(new ItemStack(output));
        }

        public RuneRitualRecipeBuilder output(ItemStack output) {
            this.outputs.add(output);
            return this;
        }

        public RuneRitualRecipeBuilder special(@Nullable SpecialRuneInput special) {
            this.specialInput = special;
            return this;
        }

        public RuneRitualRecipeBuilder special(@Nullable SpecialRuneOutput special) {
            this.specialOutput = special;
            return this;
        }

        public void build() {
            ResourceLocation rl = null;
            if (this.outputs.size() == 1) {
                rl = this.ext.provider().loc(this.outputs.get(0).getItem());
            } else if (this.specialOutput != null) {
                rl = this.specialOutput.id;
            }
            if (rl == null) {
                throw new IllegalStateException("Failed to infer recipe id for rune ritual recipe.");
            }
            this.build(rl);
        }

        public void build(ResourceLocation id) {
            this.ext.output().accept(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "masterfulbotany_rune_rituals/" + id.getPath()), new RuneRitualRecipe(this.centerRune, this.runes, this.manaCost, this.tickTime, this.inputs, this.outputs, this.specialInput == null ? null : this.specialInput.id, this.specialOutput == null ? null : this.specialOutput.id), null);
        }
    }
}
