package velrondevs.masterfulbotany.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import velrondevs.botania.registry.BotaniaBlocks;
import velrondevs.masterfulbotany.common.block.block_entity.TileManaCollector;
import velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase;
import velrondevs.masterfulbotany.common.block.flower.BlockFunctionalFlower;
import net.minecraft.resources.ResourceLocation;
import velrondevs.masterfulbotany.common.crafting.RuneRitualRecipe;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;

@GameTestHolder("masterfulbotany")
@PrefixGameTestTemplate(false)
public class FlowerTests {

    private static final BlockPos POS = TestUtil.CENTER;
    private static final BlockPos POOL_POS = POS.east(3);

    private static List<BlockFunctionalFlower<? extends FunctionalFlowerBase>> flowers() {
        return ModBlocks.functionalFlowers();
    }

    private static void plant(GameTestHelper helper, BlockFunctionalFlower<?> flower) {
        helper.setBlock(POS.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(POS, flower);
    }

    private static ManaPoolBlockEntity pool(GameTestHelper helper, int mana) {
        helper.setBlock(POOL_POS, BotaniaBlocks.manaPool);
        ManaPoolBlockEntity pool = (ManaPoolBlockEntity) helper.getBlockEntity(POOL_POS);
        // The pool only learns its capacity on its first tick, until then it can not store any mana
        try {
            java.lang.reflect.Field cap = ManaPoolBlockEntity.class.getDeclaredField("manaCap");
            cap.setAccessible(true);
            cap.setInt(pool, 1000000);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        pool.receiveMana(mana);
        return pool;
    }

    private static String diag(GameTestHelper helper, ManaPoolBlockEntity pool) {
        FunctionalFlowerBase flower = (FunctionalFlowerBase) helper.getBlockEntity(POS);
        return " [flower mana=" + flower.getCurrentMana() + ", binding=" + flower.getBinding() + ", valid=" + flower.isValidBinding()
                + ", pool mana=" + pool.getCurrentMana() + ", poolsInWorld=" + velrondevs.botania.api.BotaniaAPI.instance().getManaNetworkInstance().getAllPoolsInWorld(helper.getLevel()).size() + "]";
    }

    private static String diagGen(GameTestHelper helper) {
        FunctionalFlowerBase flower = (FunctionalFlowerBase) helper.getBlockEntity(POS);
        return " [flower mana=" + flower.getCurrentMana() + ", binding=" + flower.getBinding() + ", valid=" + flower.isValidBinding() + "]";
    }

    @GameTest(template = "empty")
    public static void allFlowersPlaceTickAndSave(GameTestHelper helper) {
        for (BlockFunctionalFlower<? extends FunctionalFlowerBase> flower : flowers()) {
            plant(helper, flower);
            BlockEntity be = helper.getBlockEntity(POS);
            helper.assertTrue(be instanceof FunctionalFlowerBase, "no flower block entity for " + flower);
            helper.assertTrue(flower.getBlockEntityType() == be.getType(), "wrong block entity type for " + flower);
            helper.assertTrue(helper.getLevel().getCapability(BotaniaCapabilities.WANDABLE, helper.absolutePos(POS), null) != null, "flower is not wandable: " + flower);
            BlockEntity copy = TestUtil.roundTrip(helper, be);
            helper.assertTrue(copy instanceof FunctionalFlowerBase, "flower lost in nbt round trip");
            helper.setBlock(POS, Blocks.AIR);
            // Floating variant: no ground needed
            Block floating = flower.getFloatingBlock();
            helper.setBlock(POS, floating);
            BlockEntity floatingBe = helper.getBlockEntity(POS);
            helper.assertTrue(floatingBe instanceof FunctionalFlowerBase fl && flower.getFloatingBlock().getBlockEntityType() == floatingBe.getType(), "no block entity for floating " + floating);
            helper.setBlock(POS, Blocks.AIR);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void allFlowersTickWithoutErrors(GameTestHelper helper) {
        // Normal flowers stay in the world for several ticks next to a collector and a pool
        pool(helper, 100000);
        int x = 2;
        for (BlockFunctionalFlower<? extends FunctionalFlowerBase> flower : flowers()) {
            BlockPos pos = new BlockPos(x, 1, 3);
            helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
            helper.setBlock(pos, flower);
            x += 3;
        }
        helper.runAfterDelay(30, () -> {
            int x2 = 2;
            for (BlockFunctionalFlower<? extends FunctionalFlowerBase> flower : flowers()) {
                helper.assertBlock(new BlockPos(x2, 1, 3), b -> b == flower, () -> "flower disappeared: " + flower);
                x2 += 3;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void witherAconiteGeneratesManaFromNetherStar(GameTestHelper helper) {
        plant(helper, ModBlocks.witherAconite);
        helper.setBlock(POOL_POS, ModBlocks.manaCollector);
        TileManaCollector collector = (TileManaCollector) helper.getBlockEntity(POOL_POS);
        helper.spawnItem(Items.NETHER_STAR, POS.getX() + 0.5f, POS.getY() + 0.2f, POS.getZ() + 0.5f);
        helper.succeedWhen(() -> {
            helper.assertTrue(collector.getCurrentMana() > 0, "no mana was generated into the collector");
            helper.assertItemEntityNotPresent(Items.NETHER_STAR, POS, 2);
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void raindeletiaGeneratesManaInThunder(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 6000, true, true);
        // The test box is closed on top, open the sky above the flower
        for (int y = 2; y < 40; y++) helper.getLevel().setBlock(helper.absolutePos(POS.atY(y)), Blocks.AIR.defaultBlockState(), 2);
        plant(helper, ModBlocks.raindeletia);
        helper.setBlock(POOL_POS, ModBlocks.manaCollector);
        TileManaCollector collector = (TileManaCollector) helper.getBlockEntity(POOL_POS);
        helper.succeedWhen(() -> {
            helper.assertTrue(collector.getCurrentMana() > 0, "no mana was generated in a thunderstorm; raining=" + helper.getLevel().isRainingAt(helper.absolutePos(POS)) + " thundering=" + helper.getLevel().isThundering() + " canSeeSky=" + helper.getLevel().canSeeSky(helper.absolutePos(POS)) + " heightmap=" + helper.getLevel().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, helper.absolutePos(POS)) + " pos=" + helper.absolutePos(POS) + " biome=" + helper.getLevel().getBiome(helper.absolutePos(POS)).unwrapKey().map(k -> k.location().toString()).orElse("?") + " rainLevel=" + helper.getLevel().getRainLevel(1) + diagGen(helper));
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void exoblazeFuelsBrewingStands(GameTestHelper helper) {
        ManaPoolBlockEntity pool = pool(helper, 100000);
        plant(helper, ModBlocks.exoblaze);
        helper.setBlock(POS.west(), Blocks.BREWING_STAND);
        BrewingStandBlockEntity stand = (BrewingStandBlockEntity) helper.getBlockEntity(POS.west());
        helper.succeedWhen(() -> helper.assertTrue(stand.fuel > 0, "brewing stand was not fueled" + diag(helper, pool)));
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void aquapanthusFillsCauldrons(GameTestHelper helper) {
        ManaPoolBlockEntity pool = pool(helper, 100000);
        plant(helper, ModBlocks.aquapanthus);
        helper.setBlock(POS.west(), Blocks.CAULDRON);
        helper.succeedWhen(() -> helper.assertBlock(POS.west(), b -> b == Blocks.WATER_CAULDRON, () -> "cauldron was not filled" + diag(helper, pool)));
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void hellebaoreResetsPiglinZombification(GameTestHelper helper) {
        ManaPoolBlockEntity pool = pool(helper, 100000);
        plant(helper, ModBlocks.hellebore);
        Piglin piglin = helper.spawn(EntityType.PIGLIN, POS.getX() + 1, POS.getY(), POS.getZ());
        piglin.timeInOverworld = 30;
        helper.succeedWhen(() -> helper.assertTrue(piglin.timeInOverworld == 0, "piglin zombification timer was not reset: " + piglin.timeInOverworld + diag(helper, pool)));
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void petruniaStartsRituals(GameTestHelper helper) {
        // The andwari ring ritual needs no mana, so the petrunia can start it on its own
        RuneRitualRecipe recipe = RecipeTests.ritualRecipe(helper, ResourceLocation.parse("masterfulbotany:masterfulbotany_rune_rituals/andwari_ring"));
        RecipeTests.buildRitual(helper, recipe);
        BlockPos flowerPos = POS.offset(1, 0, 1);
        helper.setBlock(flowerPos.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(flowerPos, ModBlocks.petrunia);
        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(ModItems.andwariRing, POS, 3);
        });
    }
}
