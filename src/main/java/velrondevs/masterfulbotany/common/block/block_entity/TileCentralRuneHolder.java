package velrondevs.masterfulbotany.common.block.block_entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.crafting.RuneRitualRecipe;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;
import velrondevs.masterfulbotany.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import velrondevs.masterfulbotany.common.helper.StackHandlers;
import velrondevs.botania.registry.BotaniaRecipeTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.base.tile.TickingBlock;
import velrondevs.botania.api.mana.ManaItemHandler;
import velrondevs.botania.client.fx.SparkleParticleData;
import velrondevs.botania.client.fx.WispParticleData;
import velrondevs.botania.registry.BotaniaItems;

public class TileCentralRuneHolder extends TileRuneHolder implements TickingBlock {

    private static final Map<Item, Integer> RUNE_COLORS = ImmutableMap.<Item, Integer>builder()
            .put(BotaniaItems.runeAir, 0x68B0EA)
            .put(BotaniaItems.runeSpring, 0xFF919F)
            .put(BotaniaItems.runeSummer, 0x00DDED)
            .put(BotaniaItems.runeAutumn, 0xE5C200)
            .put(BotaniaItems.runeWinter, 0xE0DBD5)
            .put(BotaniaItems.runeLust, 0xF346D1)
            .put(BotaniaItems.runeGluttony, 0x6E6E6E)
            .put(BotaniaItems.runeGreed, 0x009431)
            .put(BotaniaItems.runeSloth, 0xBB9661)
            .put(BotaniaItems.runeWrath, 0xFF2424)
            .put(BotaniaItems.runeEnvy, 0xC858E6)
            .put(BotaniaItems.runePride, 0x2C3237)
            .put(ModItems.asgardRune, 0xE1C500)
            .put(ModItems.vanaheimRune, 0x5FC748)
            .put(ModItems.alfheimRune, 0xFF76F7)
            .put(ModItems.midgardRune, 0x19B13B)
            .put(ModItems.joetunheimRune, 0xB16719)
            .put(ModItems.muspelheimRune, 0xBF0000)
            .put(ModItems.niflheimRune, 0x00B1BF)
            .put(ModItems.nidavellirRune, 0x6F6F6F)
            .put(ModItems.helheimRune, 0x780000)
            .build();
    
    @Nullable
    private RuneRitualRecipe recipe;
    @Nullable
    private Identifier recipeId;
    private int progress;
    private int transformId;
    private List<ItemStack> consumedStacks = new ArrayList<>();
    private CompoundTag specialNbt = new CompoundTag();

    public TileCentralRuneHolder(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        if (this.level == null) {
            return;
        }
        if (this.recipeId != null) {
            if (this.recipe == null) {
                RecipeHolder<?> foundRecipe = BotaniaRecipeTypes.recipeMap(this.level).byKey(ResourceKey.create(Registries.RECIPE, this.recipeId));
                if (foundRecipe != null && foundRecipe.value() instanceof RuneRitualRecipe foundRitual) {
                    this.recipe = foundRitual;
                    this.setChanged();
                    this.setDispatchable();
                } else {
                    this.recipeId = null;
                }
            }
        }
        if (!this.level.isClientSide()) {
            if (this.recipe != null) {
                if (!this.recipeValid(this.recipe, this.transformId)) {
                    this.cancelRecipe();
                } else {
                    if (this.progress == 0) {
                        this.setDispatchable();
                    }
                    this.progress += 1;
                    if (this.progress >= this.recipe.getTicks()) {
                        StackHandlers.set(this.getInventory(), 0, ItemStack.EMPTY);
                        for (ItemStack result : this.recipe.getOutputs()) {
                            ItemEntity ie = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, result.copy());
                            ie.setPickUpDelay(40);
                            ie.setGlowingTag(true);
                            this.level.addFreshEntity(ie);
                        }

                        for (RuneRitualRecipe.RunePosition rune : this.recipe.getRunes()) {
                            BlockPos runePos = this.worldPosition.offset(rune.getX(this.transformId), 0, rune.getZ(this.transformId));
                            BlockState state = Objects.requireNonNull(this.level).getBlockState(runePos);
                            if (state.getBlock() == ModBlocks.runeHolder) {
                                TileRuneHolder tile = ModBlocks.runeHolder.getBlockEntity(this.level, runePos);
                                tile.setTarget(null, 0, true);
                                ItemStack runeStack = StackHandlers.get(tile.getInventory(), 0);
                                StackHandlers.set(tile.getInventory(), 0, ItemStack.EMPTY);
                                if (!rune.isConsumed() && !runeStack.isEmpty()) {
                                    ItemEntity ie = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, runeStack);
                                    this.level.addFreshEntity(ie);
                                }
                            }
                        }
                        RuneRitualRecipe recipeCopy = this.recipe;
                        this.recipe = null;
                        this.recipeId = null;
                        this.progress = 0;
                        this.transformId = 0;
                        List<ItemStack> consumedStacksCops = this.consumedStacks;
                        this.consumedStacks = new ArrayList<>();
                        this.specialNbt = new CompoundTag();

                        if (recipeCopy.getSpecialOutput() != null) {
                            recipeCopy.getSpecialOutput().apply(this.level, this.worldPosition, consumedStacksCops);
                        }
                        this.setDispatchable();
                    } else {
                        this.updabePatterns(this.recipe, this.transformId, this.progress / (double) this.recipe.getTicks(), false);
                    }
                }
                this.setChanged();
            } else {
                this.recipeId = null;
                this.progress = 0;
                this.transformId = 0;
                if (!this.consumedStacks.isEmpty()) {
                    this.consumedStacks = new ArrayList<>();
                }
                this.specialNbt = new CompoundTag();
                this.setChanged();
                this.setDispatchable();
            }
        } else {
            if (this.recipe != null) {
                if (this.progress < this.recipe.getTicks() && this.progress > 0) {
                    this.progress += 1;
                    double progressScaled = this.progress / (double) this.recipe.getTicks();
                    this.updabePatterns(this.recipe, this.transformId, progressScaled, false);
                    if (this.progress == this.recipe.getTicks() - 1) {
                        this.level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, -1), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.45, this.worldPosition.getZ() + 0.5, 0, 0, 0);
                    } else if (this.progress < this.recipe.getTicks() - 2) {
                        progressScaled = Math.max(0, (this.progress - 2) / (double) this.recipe.getTicks());
                        for (RuneRitualRecipe.RunePosition rune : this.recipe.getRunes()) {
                            BlockEntity runeHolderBE = this.level.getBlockEntity(this.worldPosition.offset(rune.getX(this.transformId), 0, rune.getZ(this.transformId)));
                            if (runeHolderBE instanceof TileRuneHolder) {
                                ItemStack stack = StackHandlers.get(((TileRuneHolder) runeHolderBE).getInventory(), 0);
                                if (!stack.isEmpty()) {
                                    double x = rune.getX(this.transformId) * (1 - progressScaled);
                                    double y = Math.sin(progressScaled * Math.PI);
                                    double z = rune.getZ(this.transformId) * (1 - progressScaled);
                                    double xr = (this.level.getRandom().nextDouble() * 0.6) - 0.3;
                                    double yr = (this.level.getRandom().nextDouble() * 0.6) - 0.3;
                                    double zr = (this.level.getRandom().nextDouble() * 0.6) - 0.3;
                                    this.level.addParticle(this.getParticle(stack.getItem()), this.worldPosition.getX() + 0.5 + x + xr, this.worldPosition.getY() + 0.25 + y + yr, this.worldPosition.getZ() + 0.5 + z + zr, 0, 0, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void tryStartRitual(Player player) {
        this.tryStartRitual(
                player::sendSystemMessage,
                mana -> ManaItemHandler.instance().requestManaExact(new ItemStack(Items.COBBLESTONE), player, mana, false),
                mana -> ManaItemHandler.instance().requestManaExact(new ItemStack(Items.COBBLESTONE), player, mana, true)
        );
    }
    
    public void tryStartRitual(Consumer<Component> messages, Function<Integer, Boolean> manaBest, Consumer<Integer> manaRequest) {
        if (this.recipe != null) {
            messages.accept(Component.translatable("message.masterfulbotany.ritual_running").withStyle(ChatFormatting.GRAY));
        } else {
            Pair<RecipeHolder<RuneRitualRecipe>, Integer> recipe = this.findRecipe();
            if (recipe == null) {
                messages.accept(Component.translatable("message.masterfulbotany.ritual_wrong_shape").withStyle(ChatFormatting.GRAY));
            } else {
                this.tryStart(recipe.getLeft().id().identifier(), recipe.getLeft().value(), recipe.getRight(), messages, manaBest, manaRequest);
            }
        }
    }

    @Nullable
    private Pair<RecipeHolder<RuneRitualRecipe>, Integer> findRecipe() {
        if (this.level == null) {
            return null;
        }
        return BotaniaRecipeTypes.getAllRecipesFor(this.level, ModRecipes.runeRitual).stream()
                .flatMap(this::recipeMatches)
                .findFirst().orElse(null);
    }

    private Stream<Pair<RecipeHolder<RuneRitualRecipe>, Integer>> recipeMatches(RecipeHolder<RuneRitualRecipe> holder) {
        RuneRitualRecipe recipe = holder.value();
        if (!recipe.getCenterRune().test(StackHandlers.get(this.getInventory(), 0))) {
            return Stream.empty();
        }
        int transformId = -1;
        for (int i = 0; i < 8; i++) {
            if (this.runePatternMatches(recipe, i)) {
                transformId = i;
                break;
            }
        }
        if (transformId < 0) {
            return Stream.empty();
        }

        return Stream.of(Pair.of(holder, transformId));
    }
    
    private void tryStart(Identifier id, RuneRitualRecipe recipe, int transform, Consumer<Component> messages, Function<Integer, Boolean> manaBest, Consumer<Integer> manaRequest) {
        if (recipe.getMana() > 0) {
            if (!manaBest.apply(recipe.getMana())) {
                messages.accept(Component.translatable("message.masterfulbotany.ritual_less_mana").withStyle(ChatFormatting.GRAY));
                return;
            }
        }
        Vec3 center = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5);
        AABB aabb = new AABB(center, center).inflate(2);
        List<ItemEntity> inputs = Objects.requireNonNull(this.level).getEntities(EntityType.ITEM, aabb, e -> true);
        List<MutableTriple<ItemEntity, ItemStack, Integer>> stacks = inputs.stream()
                .map(e -> MutableTriple.of(e, e.getItem(), e.getItem().getCount()))
                .filter(t -> !t.getMiddle().isEmpty()).toList();
        List<ItemStack> consumedStacks = new ArrayList<>();
        ingredientLoop:
        for (Ingredient ingr : recipe.getInputs()) {
            for (MutableTriple<ItemEntity, ItemStack, Integer> triple : stacks) {
                if (triple.getRight() > 0 && ingr.test(triple.getMiddle())) {
                    ItemStack copy = triple.getMiddle().copy();
                    copy.setCount(1);
                    consumedStacks.add(copy);
                    triple.setRight(triple.getRight() - 1);
                    continue ingredientLoop;
                }
            }
            messages.accept(Component.translatable("message.masterfulbotany.ritual_wrong_items").withStyle(ChatFormatting.GRAY));
            return;
        }

        if (recipe.getSpecialInput() != null) {
            Either<MutableComponent, CompoundTag> result = recipe.getSpecialInput().apply(this.level, this.worldPosition, recipe);
            Optional<MutableComponent> tc = result.left();
            if (tc.isPresent()) {
                messages.accept(tc.get().withStyle(ChatFormatting.GRAY));
                return;
            }
            result.ifRight(nbt -> this.specialNbt = nbt);
        }
        
        manaRequest.accept(recipe.getMana());
        for (MutableTriple<ItemEntity, ItemStack, Integer> triple : stacks) {
            ItemStack stack = triple.getMiddle();
            stack.setCount(triple.getRight());
            triple.getLeft().setItem(stack);
        }

        this.recipe = recipe;
        this.recipeId = id;
        this.progress = 0;
        this.transformId = transform;
        this.consumedStacks = consumedStacks;

        this.setChanged();
        this.setDispatchable();
    }

    private boolean runePatternMatches(RuneRitualRecipe recipe, int idx) {
        for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
            BlockPos runePos = this.worldPosition.offset(rune.getX(idx), 0, rune.getZ(idx));
            BlockState state = Objects.requireNonNull(this.level).getBlockState(runePos);
            if (state.getBlock() != ModBlocks.runeHolder) {
                return false;
            }
            TileRuneHolder tile = ModBlocks.runeHolder.getBlockEntity(this.level, runePos);
            if (!rune.getRune().test(StackHandlers.get(tile.getInventory(), 0))) {
                return false;
            }
        }
        return true;
    }

    private void updabePatterns(RuneRitualRecipe recipe, int transformId, double progress, boolean sync) {
        if (transformId < 0 || transformId >= 8) transformId = 0;
        this.setTarget(this.worldPosition, 0, sync);
        for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
            BlockPos runePos = this.worldPosition.offset(rune.getX(transformId), 0, rune.getZ(transformId));
            BlockState state = Objects.requireNonNull(this.level).getBlockState(runePos);
            if (state.getBlock() == ModBlocks.runeHolder) {
                TileRuneHolder tile = ModBlocks.runeHolder.getBlockEntity(this.level, runePos);
                tile.setTarget(progress == 0 ? null : this.worldPosition, progress, sync);
            }
        }
    }

    public void cancelRecipe() {
        if (this.level != null && this.recipe != null) {
            this.updabePatterns(this.recipe, this.transformId, 0, true);
            for (ItemStack stack : this.consumedStacks) {
                ItemEntity ie = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, stack);
                this.level.addFreshEntity(ie);
            }
            if (this.recipe.getSpecialInput() != null) {
                this.recipe.getSpecialInput().cancel(this.level, this.worldPosition, this.recipe, this.specialNbt);
            }
            this.recipe = null;
            this.recipeId = null;
            this.progress = 0;
            this.transformId = 0;
            this.consumedStacks = new ArrayList<>();
            this.specialNbt = new CompoundTag();
            this.setChanged();
            this.setDispatchable();
        }
    }

    private boolean recipeValid(RuneRitualRecipe recipe, int transformId) {
        if (!recipe.getCenterRune().test(StackHandlers.get(this.getInventory(), 0))) {
            return false;
        }
        if (transformId < 0 || transformId >= 8) transformId = 0;
        for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
            BlockPos runePos = this.worldPosition.offset(rune.getX(transformId), 0, rune.getZ(transformId));
            BlockState state = Objects.requireNonNull(this.level).getBlockState(runePos);
            if (state.getBlock() != ModBlocks.runeHolder) {
                return false;
            }
            TileRuneHolder tile = ModBlocks.runeHolder.getBlockEntity(this.level, runePos);
            if (!rune.getRune().test(StackHandlers.get(tile.getInventory(), 0))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        this.recipeId = input.getString("recipe").map(Identifier::tryParse).orElse(null);
        this.progress = input.getIntOr("progress", 0);
        this.transformId = input.getIntOr("transform", 0);
        this.consumedStacks = new ArrayList<>();
        for (ItemStack stack : input.listOrEmpty("Consumed", ItemStack.OPTIONAL_CODEC)) {
            if (!stack.isEmpty()) {
                this.consumedStacks.add(stack);
            }
        }
        this.specialNbt = input.read("SpecialInputData", CompoundTag.CODEC).map(CompoundTag::copy).orElseGet(CompoundTag::new);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        if (this.recipe != null && this.recipeId != null) output.putString("recipe", this.recipeId.toString());
        output.putInt("progress", this.progress);
        output.putInt("transform", this.transformId);
        ValueOutput.TypedOutputList<ItemStack> consumed = output.list("Consumed", ItemStack.OPTIONAL_CODEC);
        for (ItemStack stack : this.consumedStacks) {
            consumed.add(stack);
        }
        output.store("SpecialInputData", CompoundTag.CODEC, this.specialNbt.copy());
    }
    
    private ParticleOptions getParticle(Item rune) {
        if (rune == ModItems.fimbultyrTablet) {
            return new ItemParticleOption(ParticleTypes.ITEM, Items.GOLD_BLOCK);
        } else if (rune == BotaniaItems.runeMana) {
            return WispParticleData.wisp(0.2f, 0, 0, 1, 0.3f);
        } else if (rune == BotaniaItems.runeFire) {
            return ParticleTypes.FLAME;
        } else if (rune == BotaniaItems.runeAir) {
            return ParticleTypes.CLOUD;
        } else if (rune == BotaniaItems.runeEarth) {
            return new ItemParticleOption(ParticleTypes.ITEM, Items.DIRT);
        } else if (rune == BotaniaItems.runeWater) {
            return ParticleTypes.DOLPHIN;
        } else {
            int color = RUNE_COLORS.getOrDefault(rune, 0xFFFFFF);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            return SparkleParticleData.sparkle(2, r, g, b, 6);
        }
    }
}
