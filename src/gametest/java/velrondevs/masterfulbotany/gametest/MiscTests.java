package velrondevs.masterfulbotany.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botania.common.entity.GaiaGuardianEntity;
import velrondevs.botania.common.lib.BotaniaTags;
import velrondevs.botania.registry.BotaniaEntities;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.masterfulbotany.common.helper.MasterfulPlayerData;
import velrondevs.masterfulbotany.common.item.equipment.AlfsteelPick;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModCriteria;
import velrondevs.masterfulbotany.registry.ModEntities;
import velrondevs.masterfulbotany.registry.ModItems;

@GameTestHolder("masterfulbotany")
@PrefixGameTestTemplate(false)
public class MiscTests {

    private static AdvancementHolder advancement(GameTestHelper helper, String path) {
        AdvancementHolder holder = helper.getLevel().getServer().getAdvancements().get(ResourceLocation.parse("masterfulbotany:masterfulbotany/" + path));
        helper.assertTrue(holder != null, "advancement " + path + " is not loaded");
        return holder;
    }

    @GameTest(template = "empty")
    public static void advancementsLoad(GameTestHelper helper) {
        for (String name : List.of("root", "alfheim", "alfsteel", "all_runes", "andwari", "kill_pixie", "mending_repair", "mimir", "mjoellnir")) {
            AdvancementHolder holder = advancement(helper, name);
            helper.assertTrue(!holder.value().criteria().isEmpty(), name + " has no criteria");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfRepairTrigger(GameTestHelper helper) {
        AdvancementHolder advancement = advancement(helper, "mending_repair");
        ServerPlayer player = TestUtil.player(helper, false);
        ModCriteria.ALF_REPAIR.trigger(player, new ItemStack(ModItems.alfsteelPick));
        helper.assertTrue(!player.getAdvancements().getOrStartProgress(advancement).isDone(), "an item without mending must not complete the advancement");
        ItemStack mending = new ItemStack(ModItems.alfsteelPick);
        mending.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.MENDING), 1);
        ModCriteria.ALF_REPAIR.trigger(player, mending);
        helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(), "repairing an item with mending should complete the advancement");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mjoellnirTrigger(GameTestHelper helper) {
        AdvancementHolder advancement = advancement(helper, "kill_pixie");
        ServerPlayer player = TestUtil.player(helper, false);
        ItemStack hammer = new ItemStack(ModBlocks.mjoellnir.asItem());
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, 12.5f, 1, 10.5f);
        ModCriteria.MJOELLNIR.trigger(player, hammer, cow);
        helper.assertTrue(!player.getAdvancements().getOrStartProgress(advancement).isDone(), "hitting a cow must not complete the advancement");
        var pixie = helper.spawnWithNoFreeWill(ModEntities.alfPixie, 14.5f, 1, 10.5f);
        ModCriteria.MJOELLNIR.trigger(player, hammer, pixie);
        helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(), "hitting a pixie should complete the advancement");
        helper.succeed();
    }

    private static LootParams entityLoot(GameTestHelper helper, Entity victim, ServerPlayer killer) {
        LootParams.Builder builder = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.THIS_ENTITY, victim)
                .withParameter(LootContextParams.ORIGIN, victim.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, helper.getLevel().damageSources().playerAttack(killer))
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, killer)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, killer)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer);
        return builder.create(LootContextParamSets.ENTITY);
    }

    @GameTest(template = "empty")
    public static void alfsteelTemplateDropsFromWitchesKilledWithElementium(GameTestHelper helper) {
        ServerPlayer withSword = TestUtil.player(helper, false);
        withSword.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BotaniaItems.elementiumSword));
        ServerPlayer withoutSword = TestUtil.player(helper, false);
        withoutSword.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        var witch = helper.spawnWithNoFreeWill(EntityType.WITCH, 12.5f, 1, 10.5f);
        var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(EntityType.WITCH.getDefaultLootTable());
        int with = 0;
        int without = 0;
        for (int i = 0; i < 300; i++) {
            for (ItemStack stack : table.getRandomItems(entityLoot(helper, witch, withSword))) if (stack.is(ModItems.alfsteelTemplate)) with++;
            for (ItemStack stack : table.getRandomItems(entityLoot(helper, witch, withoutSword))) if (stack.is(ModItems.alfsteelTemplate)) without++;
        }
        helper.assertTrue(with > 10, "witches killed with an elementium sword should drop the template (15%), got " + with + "/300");
        helper.assertTrue(without == 0, "other weapons must not give the template, got " + without);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fimbultyrTabletDropsFromHardModeGaia(GameTestHelper helper) throws ReflectiveOperationException {
        GaiaGuardianEntity gaia = BotaniaEntities.DOPPLEGANGER.create(helper.getLevel());
        gaia.setPos(helper.absoluteVec(new Vec3(12.5, 1, 12.5)));
        java.lang.reflect.Field hard = GaiaGuardianEntity.class.getDeclaredField("hardMode");
        hard.setAccessible(true);
        hard.setBoolean(gaia, true);
        helper.assertTrue(gaia.isHardMode(), "hard mode could not be set");
        var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(BotaniaEntities.DOPPLEGANGER.getDefaultLootTable());

        ServerPlayer knows = TestUtil.player(helper, false);
        MasterfulPlayerData.getData(knows).putBoolean("MimirKnowledge", true);
        boolean dropped = false;
        for (ItemStack stack : table.getRandomItems(entityLoot(helper, gaia, knows))) dropped |= stack.is(ModItems.fimbultyrTablet);
        helper.assertTrue(dropped, "a player with mimir knowledge should get the fimbultyr tablet from hard mode gaia");

        ServerPlayer ignorant = TestUtil.player(helper, false);
        for (ItemStack stack : table.getRandomItems(entityLoot(helper, gaia, ignorant))) helper.assertTrue(!stack.is(ModItems.fimbultyrTablet), "players without mimir knowledge must not get the tablet");

        hard.setBoolean(gaia, false);
        for (ItemStack stack : table.getRandomItems(entityLoot(helper, gaia, knows))) helper.assertTrue(!stack.is(ModItems.fimbultyrTablet), "the normal mode must not drop the tablet");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tippedAlfsteelPickDisposesBlockDrops(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        ItemStack tipped = new ItemStack(ModItems.alfsteelPick);
        AlfsteelPick.setTipped(tipped);
        player.setItemSlot(EquipmentSlot.MAINHAND, tipped);
        BlockPos pos = TestUtil.CENTER.above(2);
        helper.setBlock(pos, Blocks.NETHERRACK);
        ItemStack netherrack = new ItemStack(Items.NETHERRACK);
        helper.assertTrue(netherrack.is(BotaniaTags.Items.DISPOSABLE) || netherrack.is(BotaniaTags.Items.SEMI_DISPOSABLE), "netherrack should be in a botania disposable tag");
        var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null, player, tipped);
        helper.assertTrue(drops.isEmpty(), "the tipped pickaxe should have deleted the netherrack drop, got " + drops);
        var plain = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null, player, new ItemStack(ModItems.alfsteelPick));
        helper.assertTrue(!plain.isEmpty(), "an untipped pickaxe keeps the drop");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void runeHoldersAcceptTheFimbultyrTablet(GameTestHelper helper) {
        BlockPos pos = TestUtil.CENTER.above(2);
        helper.setBlock(pos, ModBlocks.runeHolder);
        var holder = (velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder) helper.getBlockEntity(pos);
        helper.assertTrue(holder.getInventory().isItemValid(0, new ItemStack(ModItems.fimbultyrTablet)), "the rune holder should accept the fimbultyr tablet");
        helper.assertTrue(holder.getInventory().isItemValid(0, new ItemStack(BotaniaItems.runeFire)), "the rune holder should accept runes");
        helper.assertTrue(!holder.getInventory().isItemValid(0, new ItemStack(Items.STICK)), "the rune holder must reject other items");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeTabHasNoDuplicates(GameTestHelper helper) {
        CreativeModeTabs.tryRebuildTabContents(helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
        CreativeModeTab tab = null;
        for (CreativeModeTab candidate : BuiltInRegistries.CREATIVE_MODE_TAB) {
            ResourceLocation key = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(candidate);
            if (key != null && key.getNamespace().equals("masterfulbotany")) tab = candidate;
        }
        helper.assertTrue(tab != null, "the mod has no creative tab");
        List<ItemStack> stacks = new ArrayList<>(tab.getDisplayItems());
        helper.assertTrue(stacks.size() > 40, "the creative tab looks empty: " + stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            for (int j = i + 1; j < stacks.size(); j++) {
                helper.assertTrue(!ItemStack.isSameItemSameComponents(stacks.get(i), stacks.get(j)), "duplicate stack in the creative tab: " + stacks.get(i));
            }
        }
        helper.succeed();
    }
}
