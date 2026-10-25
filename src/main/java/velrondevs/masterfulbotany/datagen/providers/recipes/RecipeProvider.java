package velrondevs.masterfulbotany.datagen.providers.recipes;

import java.util.Objects;
import velrondevs.masterfulbotany.common.block.flower.BlockFloatingFunctionalFlower;
import velrondevs.masterfulbotany.common.crafting.MjoellnirRuneOutput;
import velrondevs.masterfulbotany.common.crafting.WanderingTraderRuneInput;
import velrondevs.masterfulbotany.datagen.providers.recipes.extension.ElvenTradeExtension;
import velrondevs.masterfulbotany.datagen.providers.recipes.extension.InfuserExtension;
import velrondevs.masterfulbotany.datagen.providers.recipes.extension.ManaInfusionExtension;
import velrondevs.masterfulbotany.datagen.providers.recipes.extension.PetalExtension;
import velrondevs.masterfulbotany.datagen.providers.recipes.extension.RuneExtension;
import velrondevs.masterfulbotany.datagen.providers.recipes.extension.RuneRitualExtension;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.registries.BuiltInRegistries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.recipe.crafting.CompressionExtension;
import org.moddingx.libx.datagen.provider.recipe.crafting.CraftingExtension;
import org.moddingx.libx.datagen.provider.recipe.RecipeProviderBase;
import org.moddingx.libx.datagen.provider.recipe.SmeltingExtension;
import org.moddingx.libx.datagen.provider.recipe.SmithingExtension;
import velrondevs.botania.registry.BotaniaBlocks;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.botania.common.lib.BotaniaTags;

public class RecipeProvider extends RecipeProviderBase implements CraftingExtension, CompressionExtension, SmeltingExtension, SmithingExtension, PetalExtension, ManaInfusionExtension, RuneExtension, ElvenTradeExtension, InfuserExtension, RuneRitualExtension {

    public RecipeProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    protected void setup() {
        this.makeFloatingFlowerRecipes();

        this.doubleCompress(ModItems.alfsteelNugget, ModItems.alfsteelIngot, ModBlocks.alfsteelBlock, true);
        this.shaped(ModItems.fireRing, "re ", "e e", " e ", 'r', ModItems.muspelheimRune, 'e', BotaniaTags.Items.INGOTS_ELEMENTIUM);
        this.shaped(ModItems.iceRing, "re ", "e e", " e ", 'r', ModItems.niflheimRune, 'e', BotaniaTags.Items.INGOTS_ELEMENTIUM);

        this.shaped(ModBlocks.manaInfuser, "eee", "wdz", "xay",
                'e', BotaniaTags.Items.INGOTS_ELEMENTIUM,
                'd', BotaniaBlocks.dreamwoodGlimmering,
                'a', ModItems.asgardRune,
                'w', BotaniaItems.runeSpring,
                'x', BotaniaItems.runeSummer,
                'y', BotaniaItems.runeAutumn,
                'z',BotaniaItems.runeWinter
        );

        this.shaped(ModBlocks.alfsteelPylon, " g ", "npn", " g ", 'p', BotaniaBlocks.naturaPylon, 'n', ModItems.alfsteelNugget, 'g', Items.GHAST_TEAR);
        this.shaped(BotaniaBlocks.gaiaPylon, " d ", "epe", " d ", 'p', ModBlocks.alfsteelPylon, 'e', BotaniaTags.Items.INGOTS_ELEMENTIUM, 'd', BotaniaItems.pixieDust);
        this.shaped(ModBlocks.manaCollector, "dgd", "dpd", "dmd", 'd', BotaniaBlocks.dreamwoodGlimmering, 'g', BotaniaItems.gaiaIngot, 'p', BotaniaItems.pixieDust, 'm', ModItems.vanaheimRune);
        this.shaped(ModBlocks.yggdrasilBranch, "lll", "ttt", "lll", 'l', BotaniaTags.Items.LIVINGWOOD_LOGS, 't', BotaniaTags.Items.NUGGETS_TERRASTEEL);
        this.shaped(ModBlocks.runeHolder, " w ", "wdw", 'w', Tags.Items.INGOTS_IRON, 'd', BotaniaTags.Items.DUSTS_MANA);
        this.shaped(ModBlocks.centralRuneHolder, " w ", "wdw", 'w', Tags.Items.GEMS_EMERALD, 'd', BotaniaTags.Items.DUSTS_MANA);
        this.shapeless(ModItems.kvasirMead, ModItems.kvasirBlood, Items.HONEY_BOTTLE);

        this.compress(ModItems.rawElementium, ModBlocks.rawElementiumBlock, true);
        this.blasting(ModBlocks.elementiumOre, BotaniaItems.elementium, 0.7f, 200);
        this.blasting(ModBlocks.dragonstoneOre, BotaniaItems.dragonstone, 0.7f, 200);
        this.blasting(this.loc(BotaniaItems.elementium, "from_raw_ore"), ModItems.rawElementium, BotaniaItems.elementium, 0.7f, 200);

        this.shaped(ModItems.alfsteelTemplate, 3, "gtg", "ele", "geg", 'g', Tags.Items.INGOTS_GOLD, 'e', BotaniaTags.Items.INGOTS_ELEMENTIUM, 'l', BotaniaItems.lensExplosive, 't', ModItems.alfsteelTemplate);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.terraSword, ModItems.alfsteelIngot, ModItems.alfsteelSword);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.terraPick, ModItems.alfsteelIngot, ModItems.alfsteelPick);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.terraAxe, ModItems.alfsteelIngot, ModItems.alfsteelAxe);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.terrasteelHelm, ModItems.alfsteelIngot, ModItems.alfsteelHelmet);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.terrasteelChest, ModItems.alfsteelIngot, ModItems.alfsteelChestplate);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.terrasteelLegs, ModItems.alfsteelIngot, ModItems.alfsteelLeggings);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.terrasteelBoots, ModItems.alfsteelIngot, ModItems.alfsteelBoots);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.manaRingGreater, ModItems.alfsteelIngot, ModItems.manaRingGreatest);
        this.smithing(ModItems.alfsteelTemplate, BotaniaItems.auraRingGreater, ModItems.alfsteelIngot, ModItems.auraRingGreatest);

        this.petalApothecary(ModBlocks.exoblaze, this.petal(DyeColor.YELLOW), this.petal(DyeColor.YELLOW), this.petal(DyeColor.GRAY), this.petal(DyeColor.LIGHT_GRAY), Ingredient.of(BotaniaItems.runeFire), Ingredient.of(Items.BLAZE_POWDER));
        this.petalApothecary(ModBlocks.witherAconite, this.petal(DyeColor.BLACK), this.petal(DyeColor.BLACK), Ingredient.of(BotaniaItems.runePride), Ingredient.of(Blocks.WITHER_ROSE));
        this.petalApothecary(ModBlocks.aquapanthus, this.petal(DyeColor.BLUE), this.petal(DyeColor.BLUE), this.petal(DyeColor.LIGHT_BLUE), this.petal(DyeColor.GREEN), this.petal(DyeColor.CYAN));
        this.petalApothecary(ModBlocks.hellebore, this.petal(DyeColor.RED), this.petal(DyeColor.RED), this.petal(DyeColor.PURPLE), this.petal(DyeColor.CYAN), Ingredient.of(BotaniaItems.runeFire));
        this.petalApothecary(ModBlocks.raindeletia, this.petal(DyeColor.LIGHT_BLUE), this.petal(DyeColor.BLUE), this.petal(DyeColor.MAGENTA), this.petal(DyeColor.WHITE), Ingredient.of(BotaniaItems.runeWater));
        this.petalApothecary(ModBlocks.petrunia, this.petal(DyeColor.RED), this.petal(DyeColor.RED), this.petal(DyeColor.ORANGE), this.petal(DyeColor.BROWN), Ingredient.of(ModItems.gjallarHornFull), Ingredient.of(BotaniaItems.phantomInk));

        this.manaInfusion(BotaniaItems.grassHorn, ModItems.gjallarHornEmpty, 20000);

        this.runeAltar(ModItems.midgardRune, 16000, Ingredient.of(BotaniaTags.Items.INGOTS_MANASTEEL), Ingredient.of(BotaniaItems.runeEarth), Ingredient.of(BotaniaItems.runeSpring), Ingredient.of(BotaniaItems.runeGreed), Ingredient.of(Blocks.GRASS_BLOCK));
        this.runeAltar(ModItems.alfheimRune, 16000, Ingredient.of(BotaniaTags.Items.INGOTS_ELEMENTIUM), Ingredient.of(BotaniaItems.runeAir), Ingredient.of(BotaniaItems.runeSummer), Ingredient.of(BotaniaItems.runeLust), Ingredient.of(Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES));
        this.runeAltar(ModItems.muspelheimRune, 16000, Ingredient.of(Tags.Items.BRICKS_NETHER), Ingredient.of(BotaniaItems.runeFire), Ingredient.of(BotaniaItems.runeSummer), Ingredient.of(BotaniaItems.runeWrath), Ingredient.of(Blocks.MAGMA_BLOCK));
        this.runeAltar(ModItems.niflheimRune, 16000, Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(BotaniaItems.runeWater), Ingredient.of(BotaniaItems.runeWinter), Ingredient.of(BotaniaItems.runeWrath), Ingredient.of(Blocks.BLUE_ICE));
        this.runeAltar(ModItems.asgardRune, 16000, Ingredient.of(Tags.Items.INGOTS_NETHERITE), Ingredient.of(BotaniaItems.runeAir), Ingredient.of(BotaniaItems.runeAutumn), Ingredient.of(BotaniaItems.runePride), Ingredient.of(BotaniaItems.rainbowRod));
        this.runeAltar(ModItems.vanaheimRune, 16000, Ingredient.of(BotaniaTags.Items.INGOTS_TERRASTEEL), Ingredient.of(BotaniaItems.runeEarth), Ingredient.of(BotaniaItems.runeSpring), Ingredient.of(BotaniaItems.runePride), Ingredient.of(BotaniaBlocks.alfPortal));
        this.runeAltar(ModItems.helheimRune, 16000, Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(BotaniaItems.runeFire), Ingredient.of(BotaniaItems.runeAutumn), Ingredient.of(BotaniaItems.runeEnvy), Ingredient.of(Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.ZOMBIE_HEAD));
        this.runeAltar(ModItems.nidavellirRune, 16000, Ingredient.of(Tags.Items.INGOTS_COPPER), Ingredient.of(BotaniaItems.runeEarth), Ingredient.of(BotaniaItems.runeWinter), Ingredient.of(BotaniaItems.runeSloth), Ingredient.of(Blocks.IRON_BLOCK));
        this.runeAltar(ModItems.joetunheimRune, 16000, Ingredient.of(Tags.Items.BRICKS_NORMAL), Ingredient.of(BotaniaItems.runeEarth), Ingredient.of(BotaniaItems.runeAutumn), Ingredient.of(BotaniaItems.runeGluttony), Ingredient.of(Blocks.BLACKSTONE));

        this.elvenTrade(ModBlocks.dreamwoodLeaves, Ingredient.of(ItemTags.LEAVES));

        this.infuser(BotaniaItems.terrasteel)
                .addIngredient(BotaniaTags.Items.INGOTS_MANASTEEL)
                .addIngredient(BotaniaItems.manaPearl)
                .addIngredient(BotaniaTags.Items.GEMS_MANA_DIAMOND)
                .setManaCost(500000)
                .setColors(0x0000FF, 0x00FF00)
                .build();

        this.infuser(ModItems.alfsteelIngot)
                .addIngredient(BotaniaTags.Items.INGOTS_ELEMENTIUM)
                .addIngredient(BotaniaTags.Items.GEMS_DRAGONSTONE)
                .addIngredient(BotaniaItems.pixieDust)
                .setManaCost(1500000)
                .setColors(0xFF008D, 0xFF9600)
                .build();

        this.runeRitual(BotaniaItems.runeMana)
                .rune4(BotaniaItems.runeGreed, 2, 2)
                .rune2(ModItems.alfheimRune, 3, 0)
                .rune2(BotaniaItems.runeGreed, 0, 3)
                .input(Ingredient.of(new ItemStack(ModItems.cursedAndwariRing), new ItemStack(ModItems.andwariRing)))
                .input(BotaniaItems.manaweaveCloth)
                .output(new ItemStack(ModItems.andwariRing))
                .build();

        this.runeRitual(ModItems.fimbultyrTablet)
                .rune4(BotaniaItems.runeWrath, 5, 0)
                .rune4(BotaniaItems.runePride, 4, 4)
                .rune(BotaniaItems.runeAir, -3, 2)
                .rune(BotaniaItems.runeAir, 3, 2)
                .rune(BotaniaItems.runeAir, -2, 3)
                .rune(BotaniaItems.runeAir, 2, 3)
                .rune(BotaniaItems.runeEarth, -3, -2)
                .rune(BotaniaItems.runeEarth, 3, -2)
                .rune(BotaniaItems.runeEarth, -2, -3)
                .rune(BotaniaItems.runeEarth, 2, -3)
                .rune2(ModItems.nidavellirRune, 2, 0)
                .rune(ModItems.asgardRune, 0, 2)
                .rune(ModItems.joetunheimRune, 0, -2)
                .input(Items.POLISHED_ANDESITE)
                .input(Tags.Items.INGOTS_GOLD)
                .input(BotaniaItems.goldenSeeds)
                .input(BotaniaItems.redString)
                .input(BotaniaItems.tinyPlanet)
                .special(MjoellnirRuneOutput.INSTANCE)
                .mana(500000)
                .build();

        this.runeRitual(ModItems.fimbultyrTablet)
                .rune2(ModItems.midgardRune, 2, 2)
                .rune2(ModItems.helheimRune, -2, 2)
                .rune2(BotaniaItems.runeSummer, 1, 3)
                .rune2(BotaniaItems.runeSummer, 3, 1)
                .rune2(BotaniaItems.runeFire, -1, 3)
                .rune2(BotaniaItems.runeFire, -3, 1)
                .input(BotaniaItems.enderDagger)
                .input(ModItems.alfsteelNugget)
                .input(BotaniaItems.vial)
                .special(WanderingTraderRuneInput.INSTANCE)
                .output(ModItems.kvasirBlood)
                .mana(20000)
                .build();
    }

    private void makeFloatingFlowerRecipes() {
        BuiltInRegistries.ITEM.stream()
                .filter(item -> MasterfulBotany.getInstance().modid.equals(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).getNamespace()))
                .filter(item -> item instanceof BlockItem)
                .filter(item -> ((BlockItem) item).getBlock() instanceof BlockFloatingFunctionalFlower<?>)
                .forEach(item -> this.shapeless(item, BotaniaTags.Items.FLOATING_FLOWERS, ((BlockFloatingFunctionalFlower<?>) ((BlockItem) item).getBlock()).getNonFloatingBlock()));
    }
}
