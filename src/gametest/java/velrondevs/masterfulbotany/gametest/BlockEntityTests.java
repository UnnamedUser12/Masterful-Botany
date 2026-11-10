package velrondevs.masterfulbotany.gametest;

import velrondevs.botania.registry.BotaniaRecipeTypes;
import velrondevs.masterfulbotany.common.helper.StackHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.common.handler.ManaNetworkHandler;
import velrondevs.botania.registry.BotaniaBlocks;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.masterfulbotany.common.block.BlockMjoellnir;
import velrondevs.masterfulbotany.common.block.block_entity.TileAlfsteelPylon;
import velrondevs.masterfulbotany.common.block.block_entity.TileCentralRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileManaCollector;
import velrondevs.masterfulbotany.common.block.block_entity.TileManaInfuser;
import velrondevs.masterfulbotany.common.block.block_entity.TileMjoellnir;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileYggdrasilBranch;
import velrondevs.masterfulbotany.common.crafting.InfuserRecipe;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;
import velrondevs.masterfulbotany.registry.ModRecipes;

public class BlockEntityTests {

    private static final BlockPos POS = TestUtil.CENTER;

    private static <T extends BlockEntity> T place(GameTestHelper helper, Block block, Class<T> cls) {
        helper.setBlock(POS, block);
        BlockEntity be = helper.getBlockEntity(POS, BlockEntity.class);
        if (!cls.isInstance(be)) TestUtil.fail(helper, "Expected block entity " + cls.getSimpleName() + " but got " + be);
        return cls.cast(be);
    }

    @GameTest(template = "empty")
    public static void manaCollector(GameTestHelper helper) {
        TileManaCollector collector = place(helper, ModBlocks.manaCollector, TileManaCollector.class);
        BlockPos abs = helper.absolutePos(POS);
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null) != null, "no MANA_RECEIVER capability");
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.WANDABLE, abs, null) != null, "no WANDABLE capability");
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.SPARK_ATTACHABLE, abs, null) != null, "no SPARK_ATTACHABLE capability");
        collector.receiveMana(1234);
        TestUtil.check(helper, collector.getCurrentMana() == 1234, "mana not stored: " + collector.getCurrentMana());
        TileManaCollector copy = (TileManaCollector) TestUtil.roundTrip(helper, collector);
        TestUtil.check(helper, copy.getCurrentMana() == 1234, "mana lost in the nbt round trip: " + copy.getCurrentMana());
        helper.runAfterDelay(5, () -> {
            TestUtil.check(helper, ManaNetworkHandler.instance.isCollectorIn(helper.getLevel(), collector), "collector did not join the mana network");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void alfsteelPylonCapabilitiesAndNbt(GameTestHelper helper) {
        TileAlfsteelPylon pylon = place(helper, ModBlocks.alfsteelPylon, TileAlfsteelPylon.class);
        BlockPos abs = helper.absolutePos(POS);
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null) != null, "no MANA_RECEIVER capability");
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.WANDABLE, abs, null) != null, "no WANDABLE capability");
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.SPARK_ATTACHABLE, abs, null) == null, "pylon must not be spark attachable");
        pylon.receiveMana(777);
        TileAlfsteelPylon copy = (TileAlfsteelPylon) TestUtil.roundTrip(helper, pylon);
        TestUtil.check(helper, copy.getCurrentMana() == 777, "mana lost in the nbt round trip");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void alfsteelPylonRepairsItems(GameTestHelper helper) {
        TileAlfsteelPylon pylon = place(helper, ModBlocks.alfsteelPylon, TileAlfsteelPylon.class);
        pylon.receiveMana(TileAlfsteelPylon.MAX_MANA);
        ItemStack pick = new ItemStack(ModItems.alfsteelPick);
        pick.setDamageValue(500);
        ItemEntity item = helper.spawnItem(ModItems.alfsteelPick, POS.getX() + 0.5f, POS.getY() + 1, POS.getZ() + 0.5f);
        item.setItem(pick);
        helper.runAfterDelay(40, () -> {
            ItemStack now = item.getItem();
            TestUtil.check(helper, now.getDamageValue() < 500, "item was not repaired, damage: " + now.getDamageValue());
            TestUtil.check(helper, pylon.getCurrentMana() < TileAlfsteelPylon.MAX_MANA, "pylon did not use mana");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void runeHolderInventory(GameTestHelper helper) {
        TileRuneHolder holder = place(helper, ModBlocks.runeHolder, TileRuneHolder.class);
        BlockPos abs = helper.absolutePos(POS);
        ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, null);
        TestUtil.check(helper, handler != null, "no item handler capability");
        TestUtil.check(helper, handler.isValid(0, ItemResource.of(new ItemStack(BotaniaItems.runeFire))), "rune not accepted");
        TestUtil.checkNot(helper, handler.isValid(0, ItemResource.of(new ItemStack(Items.STICK))), "stick accepted as rune");
        ServerPlayer player = TestUtil.player(helper);
        ItemStack runes = new ItemStack(BotaniaItems.runeFire, 3);
        TestUtil.useOn(helper, player, runes, POS);
        TestUtil.check(helper, StackHandlers.get(holder.getInventory(), 0).is(BotaniaItems.runeFire), "rune was not inserted by right click");
        TestUtil.check(helper, runes.getCount() == 2, "rune not consumed from hand, count: " + runes.getCount());
        TileRuneHolder copy = (TileRuneHolder) TestUtil.roundTrip(helper, holder);
        TestUtil.check(helper, StackHandlers.get(copy.getInventory(), 0).is(BotaniaItems.runeFire), "rune lost in nbt round trip");
        TestUtil.useOn(helper, player, ItemStack.EMPTY, POS);
        TestUtil.check(helper, StackHandlers.get(holder.getInventory(), 0).isEmpty(), "rune not removed by right click");
        TestUtil.check(helper, player.getMainHandItem().is(BotaniaItems.runeFire), "rune not given to the player");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void centralRuneHolderWithoutKnowledge(GameTestHelper helper) {
        TileCentralRuneHolder holder = place(helper, ModBlocks.centralRuneHolder, TileCentralRuneHolder.class);
        ServerPlayer player = TestUtil.player(helper);
        TestUtil.useOn(helper, player, new ItemStack(BotaniaItems.twigWand), POS);
        TileCentralRuneHolder copy = (TileCentralRuneHolder) TestUtil.roundTrip(helper, holder);
        TestUtil.check(helper, StackHandlers.get(copy.getInventory(), 0).isEmpty(), "unexpected inventory content");
        helper.runAfterDelay(5, helper::succeed);
    }

    @GameTest(template = "empty")
    public static void manaInfuserBasics(GameTestHelper helper) {
        TileManaInfuser infuser = place(helper, ModBlocks.manaInfuser, TileManaInfuser.class);
        BlockPos abs = helper.absolutePos(POS);
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null) != null, "no MANA_RECEIVER capability");
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.SPARK_ATTACHABLE, abs, null) != null, "no SPARK_ATTACHABLE capability");
        TestUtil.check(helper, infuser.getCurrentMana() == 0, "infuser without recipe must report no mana");
        TestUtil.check(helper, !infuser.canReceiveManaFromBursts(), "infuser without recipe must not accept bursts");
        helper.runAfterDelay(5, helper::succeed);
    }

    private static void buildPlatform(GameTestHelper helper) {
        BlockPos center = POS.below();
        helper.setBlock(center, BotaniaBlocks.shimmerrock);
        helper.setBlock(center.north().west(), BotaniaBlocks.shimmerrock);
        helper.setBlock(center.north().east(), BotaniaBlocks.shimmerrock);
        helper.setBlock(center.south().west(), BotaniaBlocks.shimmerrock);
        helper.setBlock(center.south().east(), BotaniaBlocks.shimmerrock);
        helper.setBlock(center.north(), Blocks.GOLD_BLOCK);
        helper.setBlock(center.east(), Blocks.GOLD_BLOCK);
        helper.setBlock(center.south(), Blocks.GOLD_BLOCK);
        helper.setBlock(center.west(), Blocks.GOLD_BLOCK);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void manaInfuserCrafts(GameTestHelper helper) {
        buildPlatform(helper);
        TileManaInfuser infuser = place(helper, ModBlocks.manaInfuser, TileManaInfuser.class);
        InfuserRecipe recipe = BotaniaRecipeTypes.getAllRecipesFor(helper.getLevel(), ModRecipes.infuser).stream()
                .map(h -> h.value()).filter(r -> r.getResultItem().is(ModItems.alfsteelIngot)).findFirst().orElseThrow();
        for (var ingredient : recipe.getIngredients()) {
            helper.spawnItem(TestUtil.firstStack(ingredient).getItem(), POS.getX() + 0.5f, POS.getY() + 0.3f, POS.getZ() + 0.5f);
        }
        helper.runAfterDelay(5, () -> {
            TestUtil.check(helper, infuser.canReceiveManaFromBursts(), "infuser did not pick up the recipe");
            infuser.receiveMana(recipe.getManaUsage() - 1);
            TestUtil.check(helper, infuser.getCurrentMana() == recipe.getManaUsage() - 1, "mana not stored in infuser");
        });
        helper.runAfterDelay(10, () -> {
            helper.assertItemEntityNotPresent(ModItems.alfsteelIngot, POS, 3);
            infuser.receiveMana(1);
        });
        helper.runAfterDelay(20, () -> {
            helper.assertItemEntityPresent(ModItems.alfsteelIngot, POS, 3);
            TestUtil.check(helper, infuser.getCurrentMana() == 0, "mana was not consumed, left: " + infuser.getCurrentMana());
            for (var ingredient : recipe.getIngredients()) {
                helper.assertItemEntityNotPresent(TestUtil.firstStack(ingredient).getItem(), POS, 3);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 800)
    public static void yggdrasilBranchFillsHorn(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.yggdrasilBranch.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        TileYggdrasilBranch branch = (TileYggdrasilBranch) helper.getBlockEntity(POS, BlockEntity.class);
        BlockPos abs = helper.absolutePos(POS);
        TestUtil.check(helper, helper.getLevel().getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null) != null, "no MANA_RECEIVER capability");
        ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, null);
        TestUtil.check(helper, handler != null, "no item handler");
        ServerPlayer player = TestUtil.player(helper);
        TestUtil.useOn(helper, player, new ItemStack(ModItems.gjallarHornEmpty), POS);
        TestUtil.check(helper, StackHandlers.get(branch.getInventory(), 0).is(ModItems.gjallarHornEmpty), "horn not inserted by right click");
        branch.receiveMana(10000);
        helper.succeedWhen(() -> {
            if (!(branch.getCurrentMana() < 10000 && StackHandlers.get(branch.getInventory(), 0).is(ModItems.gjallarHornFull))) {
                TestUtil.fail(helper, "horn not filled yet (" + StackHandlers.get(branch.getInventory(), 0) + ", mana " + branch.getCurrentMana() + ")");
            }
        });
    }

    @GameTest(template = "empty")
    public static void returnPortalOutsideAlfheimRemovesItself(GameTestHelper helper) {
        place(helper, ModBlocks.returnPortal, TileReturnPortal.class);
        helper.runAfterDelay(3, () -> {
            TestUtil.assertBlock(helper, POS, b -> b == Blocks.AIR, () -> "return portal must disappear outside of alfheim");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void mjoellnirBlock(GameTestHelper helper) {
        ItemStack hammer = new ItemStack(ModBlocks.mjoellnir);
        TestUtil.check(helper, BlockMjoellnir.placeInWorld(hammer, helper.getLevel(), helper.absolutePos(POS)), "hammer could not be placed");
        TileMjoellnir tile = (TileMjoellnir) helper.getBlockEntity(POS, BlockEntity.class);
        TestUtil.check(helper, tile.getStack().is(ModBlocks.mjoellnir.asItem()), "hammer stack missing in block entity");
        TileMjoellnir copy = (TileMjoellnir) TestUtil.roundTrip(helper, tile);
        TestUtil.check(helper, copy.getStack().is(ModBlocks.mjoellnir.asItem()), "hammer stack lost in nbt round trip");
        ServerPlayer player = TestUtil.player(helper);
        player.getInventory().setSelectedSlot(0);
        TestUtil.useOn(helper, player, ItemStack.EMPTY, POS);
        TestUtil.assertBlock(helper, POS, b -> b == Blocks.AIR, () -> "hammer block not removed after pickup");
        TestUtil.check(helper, player.getInventory().contains(new ItemStack(ModBlocks.mjoellnir)), "player did not receive the hammer");
        helper.succeed();
    }
}
