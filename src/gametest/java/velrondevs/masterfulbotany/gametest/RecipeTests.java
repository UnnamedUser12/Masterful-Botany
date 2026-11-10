package velrondevs.masterfulbotany.gametest;

import net.minecraft.world.level.block.entity.BlockEntity;
import velrondevs.botania.registry.BotaniaRecipeTypes;
import velrondevs.masterfulbotany.common.helper.StackHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import velrondevs.masterfulbotany.common.block.block_entity.TileCentralRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder;
import velrondevs.masterfulbotany.common.crafting.InfuserRecipe;
import velrondevs.masterfulbotany.common.crafting.RuneRitualRecipe;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;
import velrondevs.masterfulbotany.registry.ModRecipes;

public class RecipeTests {

    private static final BlockPos POS = TestUtil.CENTER;

    @GameTest(template = "empty")
    public static void allRecipesLoadWithResult(GameTestHelper helper) {
        List<RecipeHolder<?>> recipes = (List<RecipeHolder<?>>) (List<?>) helper.getLevel().getServer().getRecipeManager().recipeMap().values().stream()
                .filter(h -> h.id().identifier().getNamespace().equals("masterfulbotany")).toList();
        TestUtil.check(helper, recipes.size() >= 40, "expected at least 40 masterfulbotany recipes, found " + recipes.size());
        Map<String, Integer> byType = new HashMap<>();
        List<String> problems = new ArrayList<>();
        for (RecipeHolder<?> holder : recipes) {
            Recipe<?> recipe = holder.value();
            byType.merge(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer()).toString(), 1, Integer::sum);
            if (recipe instanceof RuneRitualRecipe ritual) {
                if (ritual.getOutputs().isEmpty() && ritual.getSpecialOutput() == null) problems.add(holder.id() + " has no output");
                if (ritual.getCenterRune().isEmpty()) problems.add(holder.id() + " has no center rune");
                continue;
            }
            if (recipe instanceof velrondevs.botania.api.recipe.ElvenTradeRecipe trade) {
                if (trade.getOutputs().isEmpty() || trade.getOutputs().stream().anyMatch(ItemStack::isEmpty)) problems.add(holder.id() + " has an empty output");
                continue;
            }
            if (recipe instanceof InfuserRecipe infuser) {
                if (infuser.getResultItem().isEmpty()) problems.add(holder.id() + " has an empty result");
                if (infuser.getIngredients().isEmpty()) problems.add(holder.id() + " has no ingredients");
            } else if (recipe instanceof velrondevs.botania.api.recipe.BotaniaRecipe botaniaRecipe) {
                if (botaniaRecipe.getResultItem(helper.getLevel().registryAccess()).isEmpty()) problems.add(holder.id() + " has an empty result");
            } else {
                if (recipe.display().isEmpty()) problems.add(holder.id() + " has no recipe display (missing ingredients/tags)");
                if (!recipe.isSpecial() && recipe.placementInfo().isImpossibleToPlace()) problems.add(holder.id() + " is incomplete (missing ingredients/tags)");
            }
        }
        TestUtil.check(helper, problems.isEmpty(), "broken recipes: " + problems);
        for (String type : List.of("minecraft:crafting_shaped", "minecraft:crafting_shapeless", "minecraft:smithing_transform",
                "botania:runic_altar", "botania:petal_apothecary", "masterfulbotany:rune_ritual", "masterfulbotany:infuser")) {
            TestUtil.check(helper, byType.containsKey(type), "no recipe of type " + type + ", found " + byType);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void infuserRecipesHaveManaAndColors(GameTestHelper helper) {
        List<RecipeHolder<InfuserRecipe>> recipes = new ArrayList<>(BotaniaRecipeTypes.getAllRecipesFor(helper.getLevel(), ModRecipes.infuser));
        TestUtil.check(helper, recipes.size() >= 2, "expected infuser recipes, found " + recipes.size());
        for (RecipeHolder<InfuserRecipe> holder : recipes) {
            InfuserRecipe recipe = holder.value();
            TestUtil.check(helper, recipe.getManaUsage() > 0, holder.id() + " has no mana cost");
            TestUtil.check(helper, !recipe.getIngredients().isEmpty(), holder.id() + " has no ingredients");
            List<ItemStack> stacks = recipe.getIngredients().stream().map(i -> TestUtil.firstStack(i)).toList();
            TestUtil.check(helper, !recipe.result(stacks).isEmpty(), holder.id() + " does not match its own inputs");
            TestUtil.check(helper, recipe.result(List.of(new ItemStack(Blocks.DIRT))).isEmpty(), holder.id() + " matched a dirt block");
        }
        helper.succeed();
    }

    public static RuneRitualRecipe ritualRecipe(GameTestHelper helper, Identifier recipeId) {
        return (RuneRitualRecipe) BotaniaRecipeTypes.recipeMap(helper.getLevel()).byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, recipeId)).value();
    }

    public static TileCentralRuneHolder buildRitual(GameTestHelper helper, RuneRitualRecipe recipe) {
        helper.setBlock(POS, ModBlocks.centralRuneHolder);
        TileCentralRuneHolder center = (TileCentralRuneHolder) helper.getBlockEntity(POS, BlockEntity.class);
        StackHandlers.set(center.getInventory(), 0, firstOf(recipe.getCenterRune()));
        for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
            BlockPos pos = POS.offset(rune.getX(), 0, rune.getZ());
            helper.setBlock(pos, ModBlocks.runeHolder);
            TileRuneHolder holder = (TileRuneHolder) helper.getBlockEntity(pos, BlockEntity.class);
            StackHandlers.set(holder.getInventory(), 0, firstOf(rune.getRune()));
        }
        for (Ingredient input : recipe.getInputs()) {
            ItemStack stack = firstOf(input);
            helper.spawnItem(stack.getItem(), POS.getX() + 0.5f, POS.getY() + 0.5f, POS.getZ() + 0.5f);
        }
        return center;
    }

    private static void runRitual(GameTestHelper helper, Identifier recipeId, Runnable extraSetup, Runnable check) {
        RuneRitualRecipe recipe = ritualRecipe(helper, recipeId);
        TileCentralRuneHolder center = buildRitual(helper, recipe);
        if (recipe.getSpecialInput() != null) extraSetup.run();
        List<Component> messages = new ArrayList<>();
        center.tryStartRitual(messages::add, mana -> true, mana -> {});
        TestUtil.check(helper, messages.isEmpty(), "ritual did not start: " + messages.stream().map(Component::getString).toList());
        helper.runAfterDelay(recipe.getTicks() + 10L, () -> {
            check.run();
            helper.succeed();
        });
    }

    private static ItemStack firstOf(Ingredient ingredient) {
        return TestUtil.firstStack(ingredient).copy();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void ritualAndwariRing(GameTestHelper helper) {
        runRitual(helper, Identifier.parse("masterfulbotany:masterfulbotany_rune_rituals/andwari_ring"), () -> {}, () -> {
            helper.assertItemEntityPresent(ModItems.andwariRing, POS, 3);
            helper.assertItemEntityNotPresent(ModItems.cursedAndwariRing, POS, 3);
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void ritualKvasirBlood(GameTestHelper helper) {
        runRitual(helper, Identifier.parse("masterfulbotany:masterfulbotany_rune_rituals/kvasir_blood"), () -> {
            helper.spawn(EntityType.WANDERING_TRADER, POS.getX() + 1, POS.getY(), POS.getZ() + 1);
        }, () -> {
            helper.assertItemEntityPresent(ModItems.kvasirBlood, POS, 3);
            helper.assertEntityNotPresent(EntityType.WANDERING_TRADER);
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void ritualMjoellnir(GameTestHelper helper) {
        runRitual(helper, Identifier.parse("masterfulbotany:masterfulbotany_rune_rituals/mjoellnir"), () -> {}, () -> {
            TestUtil.assertBlock(helper, POS, b -> b == ModBlocks.mjoellnir, () -> "the ritual did not place the hammer");
        });
    }

    @GameTest(template = "empty")
    public static void ritualFailsWithWrongPattern(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.centralRuneHolder);
        TileCentralRuneHolder center = (TileCentralRuneHolder) helper.getBlockEntity(POS, BlockEntity.class);
        List<Component> messages = new ArrayList<>();
        center.tryStartRitual(messages::add, mana -> true, mana -> {});
        TestUtil.check(helper, messages.size() == 1, "expected the wrong shape message, got " + messages);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recipeTypesRegistered(GameTestHelper helper) {
        for (RecipeType<?> type : List.of(ModRecipes.infuser, ModRecipes.runeRitual)) {
            TestUtil.check(helper, BuiltInRegistries.RECIPE_TYPE.getKey(type) != null, "recipe type not registered: " + type);
        }
        TestUtil.check(helper, BuiltInRegistries.RECIPE_SERIALIZER.containsKey(Identifier.parse("masterfulbotany:infuser")), "infuser serializer missing");
        TestUtil.check(helper, BuiltInRegistries.RECIPE_SERIALIZER.containsKey(Identifier.parse("masterfulbotany:rune_ritual")), "rune ritual serializer missing");
        helper.succeed();
    }
}
