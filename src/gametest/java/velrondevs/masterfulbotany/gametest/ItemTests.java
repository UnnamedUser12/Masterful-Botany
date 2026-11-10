package velrondevs.masterfulbotany.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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

    @GameTest(template = "empty")
    public static void alfsteelStats(GameTestHelper helper) {
        TestUtil.check(helper, new ItemStack(ModItems.alfsteelPick).getMaxDamage() == MasterfulConfig.alftools.durability.pickaxe.max_durability(), "wrong pickaxe durability: " + new ItemStack(ModItems.alfsteelPick).getMaxDamage());
        TestUtil.check(helper, new ItemStack(ModItems.alfsteelAxe).getMaxDamage() == MasterfulConfig.alftools.durability.axe.max_durability(), "wrong axe durability: " + new ItemStack(ModItems.alfsteelAxe).getMaxDamage());
        TestUtil.check(helper, new ItemStack(ModItems.alfsteelSword).getMaxDamage() == MasterfulConfig.alftools.durability.sword.max_durability(), "wrong sword durability: " + new ItemStack(ModItems.alfsteelSword).getMaxDamage());
        double swordDamage = modifier(new ItemStack(ModItems.alfsteelSword), EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE);
        TestUtil.check(helper, Math.abs(swordDamage - MasterfulConfig.alftools.tool_values.sword_damage) < 0.001, "wrong sword damage " + swordDamage);

        record Piece(net.minecraft.world.item.Item item, ArmorType type, EquipmentSlot slot) {}
        for (Piece piece : List.of(
                new Piece(ModItems.alfsteelHelmet, ArmorType.HELMET, EquipmentSlot.HEAD),
                new Piece(ModItems.alfsteelChestplate, ArmorType.CHESTPLATE, EquipmentSlot.CHEST),
                new Piece(ModItems.alfsteelLeggings, ArmorType.LEGGINGS, EquipmentSlot.LEGS),
                new Piece(ModItems.alfsteelBoots, ArmorType.BOOTS, EquipmentSlot.FEET))) {
            ItemStack stack = new ItemStack(piece.item());
            TestUtil.check(helper, stack.getMaxDamage() == MasterfulConfig.alftools.durability.armor.max_durability(), "wrong armor durability for " + piece.item());
            double armor = modifier(stack, piece.slot(), Attributes.ARMOR);
            TestUtil.check(helper, armor > 0 && Math.abs(armor - CommonAlfsteelArmor.getDefense(piece.type())) < 0.001, "wrong armor value for " + piece.item() + ": " + armor);
            double toughness = modifier(stack, piece.slot(), Attributes.ARMOR_TOUGHNESS);
            TestUtil.check(helper, Math.abs(toughness - CommonAlfsteelArmor.getToughness(piece.type())) < 0.001, "wrong toughness for " + piece.item() + ": " + toughness);
        }
        TestUtil.check(helper, modifier(new ItemStack(ModItems.alfsteelHelmet), EquipmentSlot.HEAD, Attributes.BLOCK_INTERACTION_RANGE) > 0, "the helmet should increase the reach");
        TestUtil.check(helper, modifier(new ItemStack(ModItems.alfsteelChestplate), EquipmentSlot.CHEST, Attributes.KNOCKBACK_RESISTANCE) > 0, "the chestplate should give knockback resistance");
        TestUtil.check(helper, modifier(new ItemStack(ModItems.alfsteelLeggings), EquipmentSlot.LEGS, Attributes.MOVEMENT_SPEED) > 0, "the leggings should be faster");

        TestUtil.check(helper, repairs(ModItems.alfsteelPick, new ItemStack(ModItems.alfsteelIngot)), "alfsteel ingots should repair the pickaxe");
        TestUtil.check(helper, !repairs(ModItems.alfsteelPick, new ItemStack(BotaniaItems.terrasteel)), "terrasteel ingots must not repair the pickaxe");
        TestUtil.check(helper, repairs(ModItems.alfsteelHelmet, new ItemStack(ModItems.alfsteelIngot)), "alfsteel ingots should repair the helmet");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfsteelArmorSetCountsAsTerrasteel(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.alfsteelHelmet));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.alfsteelChestplate));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.alfsteelLeggings));
        TestUtil.check(helper, !TerrasteelHelmItem.hasTerraArmorSet(player), "an incomplete set must not give the set bonus");
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.alfsteelBoots));
        TestUtil.check(helper, TerrasteelHelmItem.hasTerraArmorSet(player), "the full alfsteel set should count as a terrasteel set (mixin)");
        TestUtil.check(helper, velrondevs.masterfulbotany.common.helper.TerraArmorChecker.hasTerraHelmet(player), "TerraArmorChecker disagrees");
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        TestUtil.check(helper, !TerrasteelHelmItem.hasTerraArmorSet(player), "removing a piece must remove the set bonus");
        helper.succeed();
    }

    private static boolean repairs(net.minecraft.world.item.Item item, ItemStack repair) {
        var repairable = new ItemStack(item).get(net.minecraft.core.component.DataComponents.REPAIRABLE);
        return repairable != null && repairable.isValidRepairItem(repair);
    }

    @GameTest(template = "empty")
    public static void alfsteelArmorRepairsWithMana(GameTestHelper helper) {
        for (net.minecraft.world.item.Item item : List.of(ModItems.alfsteelHelmet, ModItems.alfsteelChestplate)) {
            ServerPlayer player = TestUtil.player(helper, false);
            ItemStack tablet = fullTablet();
            player.getInventory().setItem(0, tablet);
            ItemStack armor = new ItemStack(item);
            armor.setDamageValue(100);
            EquipmentSlot slot = ((velrondevs.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem) item).getEquipmentSlot();
            player.setItemSlot(slot, armor);
            int manaBefore = TestUtil.mana(tablet);
            item.inventoryTick(armor, helper.getLevel(), player, slot);
            TestUtil.check(helper, armor.getDamageValue() < 100, "the armor was not repaired: " + item);
            TestUtil.check(helper, TestUtil.mana(tablet) < manaBefore, "the repair did not use any mana: " + item);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void alfsteelPickTippedDisposesDrops(GameTestHelper helper) {
        List<net.minecraft.world.item.Item> disposable = BuiltInRegistries.ITEM.get(BotaniaTags.Items.DISPOSABLE).map(t -> t.stream().map(h -> h.value()).toList()).orElse(List.of());
        TestUtil.check(helper, !disposable.isEmpty(), "the disposable tag is empty");
        ServerPlayer player = TestUtil.player(helper, false);

        ItemStack plain = new ItemStack(ModItems.alfsteelPick);
        TestUtil.check(helper, !AlfsteelPick.isTipped(plain), "a new pickaxe must not be tipped");
        ItemStack tipped = new ItemStack(ModItems.alfsteelPick);
        AlfsteelPick.setTipped(tipped);
        TestUtil.check(helper, AlfsteelPick.isTipped(tipped), "setTipped had no effect");

        List<ItemStack> drops = new ArrayList<>(List.of(new ItemStack(disposable.get(0)), new ItemStack(Items.DIAMOND)));
        AlfsteelDisposeModifier.filterDisposable(drops, player, plain);
        TestUtil.check(helper, drops.size() == 2, "an untipped pickaxe must keep all drops");
        AlfsteelDisposeModifier.filterDisposable(drops, player, tipped);
        TestUtil.check(helper, drops.size() == 1 && drops.get(0).is(Items.DIAMOND), "a tipped pickaxe should only dispose junk: " + drops);

        BlockPos support = POS.above(2);
        helper.setBlock(support, Blocks.STONE);
        List<ItemStack> plainDrops = net.minecraft.world.level.block.Block.getDrops(Blocks.STONE.defaultBlockState(), helper.getLevel(), helper.absolutePos(support), null, player, withEnchantableTool(plain));
        TestUtil.check(helper, plainDrops.stream().anyMatch(s -> s.is(Items.COBBLESTONE)), "stone should drop cobblestone with an untipped pickaxe: " + plainDrops);
        List<ItemStack> tippedDrops = net.minecraft.world.level.block.Block.getDrops(Blocks.STONE.defaultBlockState(), helper.getLevel(), helper.absolutePos(support), null, player, withEnchantableTool(tipped));
        boolean cobbleDisposable = new ItemStack(Items.COBBLESTONE).is(BotaniaTags.Items.DISPOSABLE) || new ItemStack(Items.COBBLESTONE).is(BotaniaTags.Items.SEMI_DISPOSABLE);
        if (cobbleDisposable) {
            TestUtil.check(helper, tippedDrops.stream().noneMatch(s -> s.is(Items.COBBLESTONE)), "the tipped pickaxe should have disposed the cobblestone: " + tippedDrops);
        }
        helper.succeed();
    }

    private static ItemStack withEnchantableTool(ItemStack stack) {
        return stack;
    }

    @GameTest(template = "empty")
    public static void alfsteelPickMinesAnArea(GameTestHelper helper) {
        for (int x = 9; x <= 15; x++) {
            for (int y = 2; y <= 7; y++) {
                for (int z = 9; z <= 10; z++) helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
            }
        }
        ServerPlayer player = TestUtil.player(helper, false);
        player.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 3, 6.5)), 0, 0);
        ItemStack pick = new ItemStack(ModItems.alfsteelPick);
        ItemNBTHelper.setInt(pick, "mana", 5000000);
        ItemNBTHelper.setBoolean(pick, "enabled", true);
        TestUtil.check(helper, AlfsteelPick.getLevel(pick) >= 2, "the test pickaxe should be at least level 2");
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        BlockPos target = new BlockPos(12, 4, 9);
        player.gameMode.destroyBlock(helper.absolutePos(target));
        TestUtil.check(helper, helper.getBlockState(target).isAir(), "the block in the center was not mined");
        int removed = 0;
        for (int x = 9; x <= 15; x++) {
            for (int y = 2; y <= 7; y++) {
                if (helper.getBlockState(new BlockPos(x, y, 9)).isAir()) removed++;
            }
        }
        TestUtil.check(helper, removed >= 9, "the area pickaxe should mine at least 3x3 blocks, mined " + removed);
        TestUtil.check(helper, helper.getBlockState(new BlockPos(9, 2, 9)).is(Blocks.STONE), "far away blocks must stay");
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
        TestUtil.check(helper, far.distanceTo(player) < 2.5, "the axe should pull items to the player, distance: " + far.distanceTo(player));
        TestUtil.check(helper, player.getMainHandItem().getDamageValue() == 1, "using the axe should damage it");
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
        TestUtil.check(helper, player.getAttackStrengthScale(0) == 1, "attack strength should be full");
        ((velrondevs.masterfulbotany.common.item.equipment.AlfsteelSword) ModItems.alfsteelSword).trySpawnAlfBurst(player);
        var bursts = helper.getLevel().getEntitiesOfClass(velrondevs.botania.common.entity.ManaBurstEntity.class, player.getBoundingBox().inflate(5));
        TestUtil.check(helper, bursts.size() == 1, "expected one mana burst, found " + bursts.size());
        TestUtil.check(helper, bursts.get(0).getMana() > 0, "the burst carries no mana");
        TestUtil.check(helper, player.getMainHandItem().getDamageValue() == 1, "firing a burst should damage the sword");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fireRingImmunity(GameTestHelper helper) {
        ServerPlayer plain = TestUtil.player(helper, false);
        float before = plain.getHealth();
        plain.hurt(plain.damageSources().onFire(), 4);
        TestUtil.check(helper, plain.getHealth() < before, "fire should hurt a player without the ring");

        ServerPlayer wearer = TestUtil.player(helper, false);
        TestUtil.equipCurio(wearer, "ring", new ItemStack(ModItems.fireRing));
        float health = wearer.getHealth();
        wearer.hurt(wearer.damageSources().onFire(), 4);
        wearer.hurt(wearer.damageSources().lava(), 4);
        TestUtil.check(helper, wearer.getHealth() == health, "the fire ring should protect from fire damage, health " + wearer.getHealth());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fireRingIgnitesEnemies(GameTestHelper helper) {
        ServerPlayer attacker = TestUtil.player(helper, false);
        TestUtil.equipCurio(attacker, "ring", new ItemStack(ModItems.fireRing));
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, 12.5f, 1, 10.5f);
        cow.hurt(attacker.damageSources().playerAttack(attacker), 1);
        TestUtil.check(helper, cow.getRemainingFireTicks() > 0, "the target of a player wearing the fire ring should burn");

        ServerPlayer plainAttacker = TestUtil.player(helper, false);
        Cow other = helper.spawnWithNoFreeWill(EntityType.COW, 14.5f, 1, 10.5f);
        other.hurt(plainAttacker.damageSources().playerAttack(plainAttacker), 1);
        TestUtil.check(helper, other.getRemainingFireTicks() <= 0, "without the ring nothing should burn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void iceRingSlowsEnemiesAndProtects(GameTestHelper helper) {
        ServerPlayer attacker = TestUtil.player(helper, false);
        TestUtil.equipCurio(attacker, "ring", new ItemStack(ModItems.iceRing));
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, 12.5f, 1, 10.5f);
        cow.hurt(attacker.damageSources().playerAttack(attacker), 1);
        TestUtil.check(helper, cow.hasEffect(MobEffects.SLOWNESS), "the target of a player wearing the ice ring should be slowed down");

        ServerPlayer wearer = TestUtil.player(helper, false);
        TestUtil.equipCurio(wearer, "ring", new ItemStack(ModItems.iceRing));
        float health = wearer.getHealth();
        for (DamageSource source : List.of(wearer.damageSources().inWall(), wearer.damageSources().cramming())) {
            wearer.hurt(source, 3);
        }
        TestUtil.check(helper, wearer.getHealth() == health, "the ice ring should protect from in wall and cramming damage, health " + wearer.getHealth());

        ServerPlayer plain = TestUtil.player(helper, false);
        float plainHealth = plain.getHealth();
        plain.hurt(plain.damageSources().inWall(), 3);
        TestUtil.check(helper, plain.getHealth() < plainHealth, "in wall damage should hurt without the ring");
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
        TestUtil.assertBlock(helper, support.above(), b -> b == Blocks.GOLD_BLOCK, () -> "the gold block was not placed");
        TestUtil.check(helper, TestUtil.mana(player.getInventory().getItem(5)) == manaBefore - 2000, "placing gold should cost 2000 mana");
        TestUtil.check(helper, !helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(5)).isEmpty(), "the ring should have dropped loot");
        var curios = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).orElseThrow();
        ItemStack now = curios.getStacksHandler("ring").orElseThrow().getStacks().getStackInSlot(0);
        TestUtil.check(helper, now.is(ModItems.cursedAndwariRing), "a worn out andwari ring should turn into the cursed ring, found " + now);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void andwariRingNeedsMana(GameTestHelper helper) {
        BlockPos support = POS.above(2);
        helper.setBlock(support, Blocks.STONE);
        ServerPlayer player = TestUtil.player(helper, false);
        TestUtil.equipCurio(player, "ring", new ItemStack(ModItems.andwariRing));
        TestUtil.useOn(helper, player, new ItemStack(Items.GOLD_BLOCK), support);
        TestUtil.assertBlock(helper, support.above(), b -> b == Blocks.GOLD_BLOCK, () -> "the gold block was not placed");
        TestUtil.check(helper, helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(5)).isEmpty(), "without mana there must be no loot");
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
        TestUtil.check(helper, player.hasEffect(MobEffects.POISON), "the cursed ring should poison its wearer");
        TestUtil.check(helper, TestUtil.mana(tablet) < manaBefore, "the cursed ring should drain mana");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void greatestManaRingCapacity(GameTestHelper helper) {
        ItemStack ring = new ItemStack(ModItems.manaRingGreatest);
        ManaItem mana = XplatAbstractions.INSTANCE.findManaItem(ring);
        TestUtil.check(helper, mana != null, "the greatest mana ring has no mana capability");
        TestUtil.check(helper, mana.getMaxMana() == 4000000, "unexpected capacity " + mana.getMaxMana());
        mana.addMana(3000000);
        TestUtil.check(helper, mana.getMana() == 3000000, "the ring did not store the mana, has " + mana.getMana());
        mana.addMana(2000000);
        TestUtil.check(helper, mana.getMana() == 4000000, "the ring should be capped at 4000000, has " + mana.getMana());
        ItemStack band = new ItemStack(BotaniaItems.manaRing);
        TestUtil.check(helper, XplatAbstractions.INSTANCE.findManaItem(band).getMaxMana() < 4000000, "the normal band must keep its capacity");
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
        TestUtil.check(helper, TestUtil.mana(tablet) >= 50, "the aura ring should have charged the tablet, mana: " + TestUtil.mana(tablet));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gjallarhornGivesMimirKnowledge(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        TestUtil.check(helper, !MasterfulPlayerData.has(player, "MimirKnowledge"), "new players do not know mimir");
        ItemStack horn = new ItemStack(ModItems.gjallarHornFull);
        player.setItemInHand(InteractionHand.MAIN_HAND, horn);
        TestUtil.check(helper, horn.getItem() instanceof FullGjallarHorn && horn.getItem().getUseDuration(horn, player) == 48, "the horn must take 48 ticks to drink");
        ItemStack result = horn.getItem().finishUsingItem(horn, helper.getLevel(), player);
        TestUtil.check(helper, MasterfulPlayerData.has(player, "MimirKnowledge"), "drinking from the horn should give mimir knowledge");
        TestUtil.check(helper, result.is(ModItems.gjallarHornEmpty), "the horn should be empty afterwards, got " + result);
        ServerPlayer clone = TestUtil.player(helper, false);
        MasterfulPlayerData.copy(player, clone);
        TestUtil.check(helper, MasterfulPlayerData.has(clone, "MimirKnowledge"), "the knowledge was lost on copy");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kvasirMeadGivesKvasirKnowledge(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        ItemStack mead = new ItemStack(ModItems.kvasirMead, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, mead);
        ItemStack result = mead.getItem().finishUsingItem(mead, helper.getLevel(), player);
        TestUtil.check(helper, MasterfulPlayerData.has(player, "KvasirKnowledge"), "drinking the mead should give kvasir knowledge");
        TestUtil.check(helper, result.getCount() == 1 && result.is(ModItems.kvasirMead), "one mead should be left");
        TestUtil.check(helper, result.getItem().getUseDuration(result, player) == 48, "the mead must take 48 ticks to drink");
        TestUtil.check(helper, player.getInventory().countItem(BotaniaItems.vial) == 1, "the player should have got an empty vial");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fadedNetherStarIsAWitherAconiteBattery(GameTestHelper helper) {
        ItemStack star = new ItemStack(ModItems.fadedNetherStar);
        TestUtil.check(helper, star.getMaxDamage() == MasterfulConfig.flowers.witherAconiteMana, "the faded star durability must come from the config");
        helper.succeed();
    }
}
