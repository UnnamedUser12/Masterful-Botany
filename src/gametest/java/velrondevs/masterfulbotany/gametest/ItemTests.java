package velrondevs.masterfulbotany.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botania.api.mana.ManaItem;
import velrondevs.botania.common.helper.ItemNBTHelper;
import velrondevs.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;
import velrondevs.botania.common.lib.BotaniaTags;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.botania.xplat.XplatAbstractions;
import velrondevs.masterfulbotany.common.item.FullGjallarHorn;
import velrondevs.masterfulbotany.common.item.equipment.AlfsteelPick;
import velrondevs.masterfulbotany.common.item.equipment.CommonAlfsteelArmor;
import velrondevs.masterfulbotany.common.helper.MasterfulPlayerData;
import velrondevs.masterfulbotany.common.loot.AlfsteelDisposeModifier;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.registry.ModItems;

@GameTestHolder("masterfulbotany")
@PrefixGameTestTemplate(false)
public class ItemTests {

    private static final BlockPos POS = TestUtil.CENTER;

    private static double modifier(ItemStack stack, EquipmentSlot slot, net.minecraft.core.Holder<Attribute> attribute) {
        AtomicReference<Double> total = new AtomicReference<>(0.0);
        stack.forEachModifier(slot, (attr, mod) -> {
            if (attr.equals(attribute)) total.updateAndGet(v -> v + mod.amount());
        });
        return total.get();
    }

    private static ItemStack fullTablet() {
        return TestUtil.tablet(1000000);
    }

    // ---------------------------------------------------------------- alfsteel

    @GameTest(template = "empty")
    public static void alfsteelStats(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModItems.alfsteelPick).getMaxDamage() == MasterfulConfig.alftools.durability.pickaxe.max_durability(), "wrong pickaxe durability: " + new ItemStack(ModItems.alfsteelPick).getMaxDamage());
        helper.assertTrue(new ItemStack(ModItems.alfsteelAxe).getMaxDamage() == MasterfulConfig.alftools.durability.axe.max_durability(), "wrong axe durability: " + new ItemStack(ModItems.alfsteelAxe).getMaxDamage());
        helper.assertTrue(new ItemStack(ModItems.alfsteelSword).getMaxDamage() == MasterfulConfig.alftools.durability.sword.max_durability(), "wrong sword durability: " + new ItemStack(ModItems.alfsteelSword).getMaxDamage());
        double swordDamage = modifier(new ItemStack(ModItems.alfsteelSword), EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE);
        helper.assertTrue(Math.abs(swordDamage - MasterfulConfig.alftools.tool_values.sword_damage) < 0.001, "wrong sword damage " + swordDamage);

        record Piece(net.minecraft.world.item.Item item, ArmorItem.Type type, EquipmentSlot slot) {}
        for (Piece piece : List.of(
                new Piece(ModItems.alfsteelHelmet, ArmorItem.Type.HELMET, EquipmentSlot.HEAD),
                new Piece(ModItems.alfsteelChestplate, ArmorItem.Type.CHESTPLATE, EquipmentSlot.CHEST),
                new Piece(ModItems.alfsteelLeggings, ArmorItem.Type.LEGGINGS, EquipmentSlot.LEGS),
                new Piece(ModItems.alfsteelBoots, ArmorItem.Type.BOOTS, EquipmentSlot.FEET))) {
            ItemStack stack = new ItemStack(piece.item());
            helper.assertTrue(stack.getMaxDamage() == MasterfulConfig.alftools.durability.armor.max_durability(), "wrong armor durability for " + piece.item());
            double armor = modifier(stack, piece.slot(), Attributes.ARMOR);
            helper.assertTrue(armor > 0 && Math.abs(armor - CommonAlfsteelArmor.getDefense(piece.type())) < 0.001, "wrong armor value for " + piece.item() + ": " + armor);
            double toughness = modifier(stack, piece.slot(), Attributes.ARMOR_TOUGHNESS);
            helper.assertTrue(Math.abs(toughness - CommonAlfsteelArmor.getToughness(piece.type())) < 0.001, "wrong toughness for " + piece.item() + ": " + toughness);
        }
        helper.assertTrue(modifier(new ItemStack(ModItems.alfsteelHelmet), EquipmentSlot.HEAD, Attributes.BLOCK_INTERACTION_RANGE) > 0, "the helmet should increase the reach");
        helper.assertTrue(modifier(new ItemStack(ModItems.alfsteelChestplate), EquipmentSlot.CHEST, Attributes.KNOCKBACK_RESISTANCE) > 0, "the chestplate should give knockback resistance");
        helper.assertTrue(modifier(new ItemStack(ModItems.alfsteelLeggings), EquipmentSlot.LEGS, Attributes.MOVEMENT_SPEED) > 0, "the leggings should be faster");

        // Repair items: alfsteel ingots repair, terrasteel ingots do not
        helper.assertTrue(ModItems.alfsteelPick.isValidRepairItem(new ItemStack(ModItems.alfsteelPick), new ItemStack(ModItems.alfsteelIngot)), "alfsteel ingots should repair the pickaxe");
        helper.assertTrue(!ModItems.alfsteelPick.isValidRepairItem(new ItemStack(ModItems.alfsteelPick), new ItemStack(BotaniaItems.terrasteel)), "terrasteel ingots must not repair the pickaxe");
        helper.assertTrue(ModItems.alfsteelHelmet.isValidRepairItem(new ItemStack(ModItems.alfsteelHelmet), new ItemStack(ModItems.alfsteelIngot)), "alfsteel ingots should repair the helmet");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfsteelArmorSetCountsAsTerrasteel(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.alfsteelHelmet));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.alfsteelChestplate));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.alfsteelLeggings));
        helper.assertTrue(!TerrasteelHelmItem.hasTerraArmorSet(player), "an incomplete set must not give the set bonus");
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.alfsteelBoots));
        helper.assertTrue(TerrasteelHelmItem.hasTerraArmorSet(player), "the full alfsteel set should count as a terrasteel set (mixin)");
        helper.assertTrue(velrondevs.masterfulbotany.common.helper.TerraArmorChecker.hasTerraHelmet(player), "TerraArmorChecker disagrees");
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        helper.assertTrue(!TerrasteelHelmItem.hasTerraArmorSet(player), "removing a piece must remove the set bonus");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfsteelArmorRepairsWithMana(GameTestHelper helper) {
        for (net.minecraft.world.item.Item item : List.of(ModItems.alfsteelHelmet, ModItems.alfsteelChestplate)) {
            ServerPlayer player = TestUtil.player(helper, false);
            ItemStack tablet = fullTablet();
            player.getInventory().setItem(0, tablet);
            ItemStack armor = new ItemStack(item);
            armor.setDamageValue(100);
            EquipmentSlot slot = ((ArmorItem) item).getEquipmentSlot();
            player.setItemSlot(slot, armor);
            int manaBefore = TestUtil.mana(tablet);
            item.inventoryTick(armor, helper.getLevel(), player, 0, false);
            helper.assertTrue(armor.getDamageValue() < 100, "the armor was not repaired: " + item);
            helper.assertTrue(TestUtil.mana(tablet) < manaBefore, "the repair did not use any mana: " + item);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfsteelPickTippedDisposesDrops(GameTestHelper helper) {
        List<net.minecraft.world.item.Item> disposable = BuiltInRegistries.ITEM.getTag(BotaniaTags.Items.DISPOSABLE).map(t -> t.stream().map(h -> h.value()).toList()).orElse(List.of());
        helper.assertTrue(!disposable.isEmpty(), "the disposable tag is empty");
        ServerPlayer player = TestUtil.player(helper, false);

        ItemStack plain = new ItemStack(ModItems.alfsteelPick);
        helper.assertTrue(!AlfsteelPick.isTipped(plain), "a new pickaxe must not be tipped");
        ItemStack tipped = new ItemStack(ModItems.alfsteelPick);
        AlfsteelPick.setTipped(tipped);
        helper.assertTrue(AlfsteelPick.isTipped(tipped), "setTipped had no effect");

        List<ItemStack> drops = new ArrayList<>(List.of(new ItemStack(disposable.get(0)), new ItemStack(Items.DIAMOND)));
        AlfsteelDisposeModifier.filterDisposable(drops, player, plain);
        helper.assertTrue(drops.size() == 2, "an untipped pickaxe must keep all drops");
        AlfsteelDisposeModifier.filterDisposable(drops, player, tipped);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(Items.DIAMOND), "a tipped pickaxe should only dispose junk: " + drops);

        // The same through the real loot tables (the global loot modifier must be registered and loaded)
        BlockPos support = POS.above(2);
        helper.setBlock(support, Blocks.STONE);
        List<ItemStack> plainDrops = net.minecraft.world.level.block.Block.getDrops(Blocks.STONE.defaultBlockState(), helper.getLevel(), helper.absolutePos(support), null, player, withEnchantableTool(plain));
        helper.assertTrue(plainDrops.stream().anyMatch(s -> s.is(Items.COBBLESTONE)), "stone should drop cobblestone with an untipped pickaxe: " + plainDrops);
        List<ItemStack> tippedDrops = net.minecraft.world.level.block.Block.getDrops(Blocks.STONE.defaultBlockState(), helper.getLevel(), helper.absolutePos(support), null, player, withEnchantableTool(tipped));
        boolean cobbleDisposable = new ItemStack(Items.COBBLESTONE).is(BotaniaTags.Items.DISPOSABLE) || new ItemStack(Items.COBBLESTONE).is(BotaniaTags.Items.SEMI_DISPOSABLE);
        if (cobbleDisposable) {
            helper.assertTrue(tippedDrops.stream().noneMatch(s -> s.is(Items.COBBLESTONE)), "the tipped pickaxe should have disposed the cobblestone: " + tippedDrops);
        }
        helper.succeed();
    }

    private static ItemStack withEnchantableTool(ItemStack stack) {
        return stack;
    }

    @GameTest(template = "empty")
    public static void alfsteelPickMinesAnArea(GameTestHelper helper) {
        // A wall of stone in front of the player, the pickaxe has enough mana for level 2 (3x3)
        for (int x = 9; x <= 15; x++) {
            for (int y = 2; y <= 7; y++) {
                for (int z = 9; z <= 10; z++) helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
            }
        }
        ServerPlayer player = TestUtil.player(helper, false);
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 3, 6.5)), 0, 0);
        ItemStack pick = new ItemStack(ModItems.alfsteelPick);
        ItemNBTHelper.setInt(pick, "mana", 5000000);
        ItemNBTHelper.setBoolean(pick, "enabled", true);
        helper.assertTrue(AlfsteelPick.getLevel(pick) >= 2, "the test pickaxe should be at least level 2");
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        BlockPos target = new BlockPos(12, 4, 9);
        player.gameMode.destroyBlock(helper.absolutePos(target));
        helper.assertTrue(helper.getBlockState(target).isAir(), "the block in the center was not mined");
        int removed = 0;
        for (int x = 9; x <= 15; x++) {
            for (int y = 2; y <= 7; y++) {
                if (helper.getBlockState(new BlockPos(x, y, 9)).isAir()) removed++;
            }
        }
        helper.assertTrue(removed >= 9, "the area pickaxe should mine at least 3x3 blocks, mined " + removed);
        helper.assertTrue(helper.getBlockState(new BlockPos(9, 2, 9)).is(Blocks.STONE), "far away blocks must stay");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfsteelAxeCollectsItems(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        ItemEntity far = new ItemEntity(helper.getLevel(), player.getX() + 6, player.getY(), player.getZ(), new ItemStack(Items.DIAMOND));
        helper.getLevel().addFreshEntity(far);
        ItemStack axe = new ItemStack(ModItems.alfsteelAxe);
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);
        axe.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(far.distanceTo(player) < 2.5, "the axe should pull items to the player, distance: " + far.distanceTo(player));
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "using the axe should damage it");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfsteelSwordFiresBursts(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        ItemStack sword = new ItemStack(ModItems.alfsteelSword);
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        try {
            java.lang.reflect.Field ticker = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("attackStrengthTicker");
            ticker.setAccessible(true);
            ticker.setInt(player, 1000);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        helper.assertTrue(player.getAttackStrengthScale(0) == 1, "attack strength should be full");
        ((velrondevs.masterfulbotany.common.item.equipment.AlfsteelSword) ModItems.alfsteelSword).trySpawnAlfBurst(player);
        var bursts = helper.getLevel().getEntitiesOfClass(velrondevs.botania.common.entity.ManaBurstEntity.class, player.getBoundingBox().inflate(5));
        helper.assertTrue(bursts.size() == 1, "expected one mana burst, found " + bursts.size());
        helper.assertTrue(bursts.get(0).getMana() > 0, "the burst carries no mana");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "firing a burst should damage the sword");
        helper.succeed();
    }

    // ---------------------------------------------------------------- rings

    @GameTest(template = "empty")
    public static void fireRingImmunity(GameTestHelper helper) {
        ServerPlayer plain = TestUtil.player(helper, false);
        float before = plain.getHealth();
        plain.hurt(plain.damageSources().onFire(), 4);
        helper.assertTrue(plain.getHealth() < before, "fire should hurt a player without the ring");

        ServerPlayer wearer = TestUtil.player(helper, false);
        TestUtil.equipCurio(wearer, "ring", new ItemStack(ModItems.fireRing));
        float health = wearer.getHealth();
        wearer.hurt(wearer.damageSources().onFire(), 4);
        wearer.hurt(wearer.damageSources().lava(), 4);
        helper.assertTrue(wearer.getHealth() == health, "the fire ring should protect from fire damage, health " + wearer.getHealth());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fireRingIgnitesEnemies(GameTestHelper helper) {
        ServerPlayer attacker = TestUtil.player(helper, false);
        TestUtil.equipCurio(attacker, "ring", new ItemStack(ModItems.fireRing));
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, 12.5f, 1, 10.5f);
        cow.hurt(attacker.damageSources().playerAttack(attacker), 1);
        helper.assertTrue(cow.getRemainingFireTicks() > 0, "the target of a player wearing the fire ring should burn");

        ServerPlayer plainAttacker = TestUtil.player(helper, false);
        Cow other = helper.spawnWithNoFreeWill(EntityType.COW, 14.5f, 1, 10.5f);
        other.hurt(plainAttacker.damageSources().playerAttack(plainAttacker), 1);
        helper.assertTrue(other.getRemainingFireTicks() <= 0, "without the ring nothing should burn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void iceRingSlowsEnemiesAndProtects(GameTestHelper helper) {
        ServerPlayer attacker = TestUtil.player(helper, false);
        TestUtil.equipCurio(attacker, "ring", new ItemStack(ModItems.iceRing));
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, 12.5f, 1, 10.5f);
        cow.hurt(attacker.damageSources().playerAttack(attacker), 1);
        helper.assertTrue(cow.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the target of a player wearing the ice ring should be slowed down");

        ServerPlayer wearer = TestUtil.player(helper, false);
        TestUtil.equipCurio(wearer, "ring", new ItemStack(ModItems.iceRing));
        float health = wearer.getHealth();
        for (DamageSource source : List.of(wearer.damageSources().inWall(), wearer.damageSources().cramming())) {
            wearer.hurt(source, 3);
        }
        helper.assertTrue(wearer.getHealth() == health, "the ice ring should protect from in wall and cramming damage, health " + wearer.getHealth());

        ServerPlayer plain = TestUtil.player(helper, false);
        float plainHealth = plain.getHealth();
        plain.hurt(plain.damageSources().inWall(), 3);
        helper.assertTrue(plain.getHealth() < plainHealth, "in wall damage should hurt without the ring");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void andwariRingGivesGoldDrops(GameTestHelper helper) {
        BlockPos support = POS.above(2);
        helper.setBlock(support, Blocks.STONE);
        ServerPlayer player = TestUtil.player(helper, false);
        player.getInventory().setItem(5, fullTablet());
        ItemStack ring = new ItemStack(ModItems.andwariRing);
        ring.setDamageValue(ring.getMaxDamage() - 1);
        TestUtil.equipCurio(player, "ring", ring);
        int manaBefore = TestUtil.mana(player.getInventory().getItem(5));
        TestUtil.useOn(helper, player, new ItemStack(Items.GOLD_BLOCK), support);
        helper.assertBlock(support.above(), b -> b == Blocks.GOLD_BLOCK, () -> "the gold block was not placed");
        helper.assertTrue(TestUtil.mana(player.getInventory().getItem(5)) == manaBefore - 2000, "placing gold should cost 2000 mana");
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(5)).isEmpty(), "the ring should have dropped loot");
        var curios = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).orElseThrow();
        ItemStack now = curios.getStacksHandler("ring").orElseThrow().getStacks().getStackInSlot(0);
        helper.assertTrue(now.is(ModItems.cursedAndwariRing), "a worn out andwari ring should turn into the cursed ring, found " + now);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void andwariRingNeedsMana(GameTestHelper helper) {
        BlockPos support = POS.above(2);
        helper.setBlock(support, Blocks.STONE);
        ServerPlayer player = TestUtil.player(helper, false);
        TestUtil.equipCurio(player, "ring", new ItemStack(ModItems.andwariRing));
        TestUtil.useOn(helper, player, new ItemStack(Items.GOLD_BLOCK), support);
        helper.assertBlock(support.above(), b -> b == Blocks.GOLD_BLOCK, () -> "the gold block was not placed");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(5)).isEmpty(), "without mana there must be no loot");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cursedAndwariRingPoisonsAndDrainsMana(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        ItemStack tablet = fullTablet();
        player.getInventory().setItem(3, tablet);
        ItemStack ring = new ItemStack(ModItems.cursedAndwariRing);
        TestUtil.equipCurio(player, "ring", ring);
        int manaBefore = TestUtil.mana(tablet);
        player.tickCount = 20;
        ((velrondevs.botania.common.item.equipment.bauble.BaubleItem) ring.getItem()).onWornTick(ring, player);
        helper.assertTrue(player.hasEffect(MobEffects.POISON), "the cursed ring should poison its wearer");
        helper.assertTrue(TestUtil.mana(tablet) < manaBefore, "the cursed ring should drain mana");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void greatestManaRingCapacity(GameTestHelper helper) {
        ItemStack ring = new ItemStack(ModItems.manaRingGreatest);
        ManaItem mana = XplatAbstractions.INSTANCE.findManaItem(ring);
        helper.assertTrue(mana != null, "the greatest mana ring has no mana capability");
        helper.assertTrue(mana.getMaxMana() == 4000000, "unexpected capacity " + mana.getMaxMana());
        mana.addMana(3000000);
        helper.assertTrue(mana.getMana() == 3000000, "the ring did not store the mana, has " + mana.getMana());
        mana.addMana(2000000);
        helper.assertTrue(mana.getMana() == 4000000, "the ring should be capped at 4000000, has " + mana.getMana());
        // The vanilla band keeps its own smaller capacity
        ItemStack band = new ItemStack(BotaniaItems.manaRing);
        helper.assertTrue(XplatAbstractions.INSTANCE.findManaItem(band).getMaxMana() < 4000000, "the normal band must keep its capacity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void greatestAuraRingChargesItems(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        ItemStack tablet = TestUtil.tablet(0);
        player.getInventory().setItem(2, tablet);
        ItemStack ring = new ItemStack(ModItems.auraRingGreatest);
        TestUtil.equipCurio(player, "ring", ring);
        player.tickCount = 2;
        for (int i = 0; i < 10; i++) {
            ((velrondevs.botania.common.item.equipment.bauble.BaubleItem) ring.getItem()).onWornTick(ring, player);
        }
        helper.assertTrue(TestUtil.mana(tablet) >= 50, "the aura ring should have charged the tablet, mana: " + TestUtil.mana(tablet));
        helper.succeed();
    }

    // ---------------------------------------------------------------- consumables

    @GameTest(template = "empty")
    public static void gjallarhornGivesMimirKnowledge(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        helper.assertTrue(!MasterfulPlayerData.getData(player).getBoolean("MimirKnowledge"), "new players do not know mimir");
        ItemStack horn = new ItemStack(ModItems.gjallarHornFull);
        player.setItemInHand(InteractionHand.MAIN_HAND, horn);
        helper.assertTrue(horn.getItem() instanceof FullGjallarHorn && horn.getItem().getUseDuration(horn, player) == 48, "the horn must take 48 ticks to drink");
        ItemStack result = horn.getItem().finishUsingItem(horn, helper.getLevel(), player);
        helper.assertTrue(MasterfulPlayerData.getData(player).getBoolean("MimirKnowledge"), "drinking from the horn should give mimir knowledge");
        helper.assertTrue(result.is(ModItems.gjallarHornEmpty), "the horn should be empty afterwards, got " + result);
        // The knowledge survives death (clone)
        ServerPlayer clone = TestUtil.player(helper, false);
        MasterfulPlayerData.copy(player, clone);
        helper.assertTrue(MasterfulPlayerData.getData(clone).getBoolean("MimirKnowledge"), "the knowledge was lost on copy");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kvasirMeadGivesKvasirKnowledge(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        ItemStack mead = new ItemStack(ModItems.kvasirMead, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, mead);
        ItemStack result = mead.getItem().finishUsingItem(mead, helper.getLevel(), player);
        helper.assertTrue(MasterfulPlayerData.getData(player).getBoolean("KvasirKnowledge"), "drinking the mead should give kvasir knowledge");
        helper.assertTrue(result.getCount() == 1 && result.is(ModItems.kvasirMead), "one mead should be left");
        helper.assertTrue(result.getItem().getUseDuration(result, player) == 48, "the mead must take 48 ticks to drink");
        helper.assertTrue(player.getInventory().countItem(BotaniaItems.vial) == 1, "the player should have got an empty vial");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fadedNetherStarIsAWitherAconiteBattery(GameTestHelper helper) {
        ItemStack star = new ItemStack(ModItems.fadedNetherStar);
        helper.assertTrue(star.getMaxDamage() == MasterfulConfig.flowers.witherAconiteMana, "the faded star durability must come from the config");
        helper.succeed();
    }
}
