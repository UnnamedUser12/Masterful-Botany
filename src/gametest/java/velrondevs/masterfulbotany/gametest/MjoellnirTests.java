package velrondevs.masterfulbotany.gametest;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.masterfulbotany.common.block.BlockMjoellnir;
import velrondevs.masterfulbotany.common.block.block_entity.TileMjoellnir;
import velrondevs.masterfulbotany.common.entity.Mjoellnir;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.config.MjoellnirHoldRequirement;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModEnchantments;

@GameTestHolder("masterfulbotany")
@PrefixGameTestTemplate(false)
public class MjoellnirTests {

    private static final BlockPos POS = TestUtil.CENTER;

    private static ItemStack hammer() {
        return new ItemStack(ModBlocks.mjoellnir.asItem());
    }

    private static double attackDamage(ItemStack stack) {
        AtomicReference<Double> total = new AtomicReference<>(0.0);
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) total.updateAndGet(v -> v + modifier.amount());
        });
        return total.get();
    }

    private static double attackSpeed(ItemStack stack) {
        AtomicReference<Double> total = new AtomicReference<>(0.0);
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_SPEED)) total.updateAndGet(v -> v + modifier.amount());
        });
        return total.get();
    }

    private static Holder<Enchantment> enchantment(GameTestHelper helper, net.minecraft.resources.ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }

    @GameTest(template = "empty")
    public static void attributesAndEnchantments(GameTestHelper helper) {
        ItemStack plain = hammer();
        double base = attackDamage(plain);
        helper.assertTrue(Math.abs(base - (MasterfulConfig.mjoellnir.base_damage_melee - 1)) < 0.001, "unexpected base damage modifier " + base);
        double plainSpeed = attackSpeed(plain);
        helper.assertTrue(Math.abs(plainSpeed - MasterfulConfig.mjoellnir.base_attack_speed) < 0.001, "unexpected base attack speed " + plainSpeed);

        ItemStack sharp = hammer();
        sharp.enchant(enchantment(helper, Enchantments.SHARPNESS), 3);
        double sharpDamage = attackDamage(sharp);
        // sharpness 3: (multiplier - 1) * (1 + 2 * 0.5)
        double expected = base + (MasterfulConfig.mjoellnir.enchantment_multiplier - 1) * 2;
        helper.assertTrue(Math.abs(sharpDamage - expected) < 0.001, "sharpness bonus is wrong: " + sharpDamage + " instead of " + expected);

        ItemStack mobile = hammer();
        mobile.enchant(enchantment(helper, ModEnchantments.HAMMER_MOBILITY), 2);
        double mobileSpeed = attackSpeed(mobile);
        helper.assertTrue(Math.abs(mobileSpeed - (plainSpeed + MasterfulConfig.mjoellnir.attack_speed_multiplier * 2)) < 0.001, "hammer mobility speed is wrong: " + mobileSpeed);
        helper.assertTrue(hammer().getItem().isEnchantable(hammer()), "the hammer must be enchantable");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void holdRequirement(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        MjoellnirHoldRequirement old = MasterfulConfig.mjoellnir.requirement;
        try {
            MasterfulConfig.mjoellnir.requirement = MjoellnirHoldRequirement.EFFECT;
            helper.assertTrue(!BlockMjoellnir.canHold(player), "a player without absorption should not be able to hold the hammer");
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1000, 0));
            helper.assertTrue(BlockMjoellnir.canHold(player), "a player with absorption should be able to hold the hammer");
            player.removeAllEffects();
            MasterfulConfig.mjoellnir.requirement = MjoellnirHoldRequirement.HEARTS;
            helper.assertTrue(!BlockMjoellnir.canHold(player), "a player without golden hearts should not be able to hold the hammer");
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1000, 0));
            helper.assertTrue(player.getAbsorptionAmount() > 0, "absorption effect gave no hearts");
            helper.assertTrue(BlockMjoellnir.canHold(player), "a player with golden hearts should be able to hold the hammer");
            player.removeAllEffects();
            MasterfulConfig.mjoellnir.requirement = MjoellnirHoldRequirement.NOTHING;
            helper.assertTrue(BlockMjoellnir.canHold(player), "with no requirement everyone can hold the hammer");
        } finally {
            MasterfulConfig.mjoellnir.requirement = old;
        }
        helper.assertTrue(BlockMjoellnir.canHold(TestUtil.player(helper, true)), "creative players can always hold the hammer");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dropsWhenTooHeavy(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        BlockPos playerPos = player.blockPosition();
        ItemStack stack = hammer();
        player.getInventory().setItem(0, stack);
        // Without absorption: the hammer drops out of the inventory and becomes a block
        stack.getItem().inventoryTick(stack, helper.getLevel(), player, 0, true);
        helper.assertTrue(stack.isEmpty(), "the hammer should have been removed from the inventory");
        helper.assertTrue(helper.getLevel().getBlockState(playerPos).is(ModBlocks.mjoellnir), "the hammer should have been placed in the world at " + playerPos);

        // With absorption it stays in the inventory
        ServerPlayer strong = TestUtil.player(helper, false);
        strong.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1000, 0));
        ItemStack kept = hammer();
        kept.getItem().inventoryTick(kept, helper.getLevel(), strong, 0, true);
        helper.assertTrue(!kept.isEmpty(), "a strong player should keep the hammer");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pickupRestrictedWhenTooHeavy(GameTestHelper helper) {
        ServerPlayer weak = TestUtil.player(helper, false);
        ItemEntity weakItem = new ItemEntity(helper.getLevel(), weak.getX(), weak.getY(), weak.getZ(), hammer());
        weakItem.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(weakItem);
        weakItem.playerTouch(weak);
        helper.assertTrue(!weakItem.getItem().isEmpty() && !weakItem.isRemoved(), "a weak player must not pick up the hammer");
        helper.assertTrue(weak.getInventory().countItem(hammer().getItem()) == 0, "the hammer is in the inventory of a weak player");

        ServerPlayer strong = TestUtil.player(helper, false);
        strong.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1000, 0));
        ItemEntity strongItem = new ItemEntity(helper.getLevel(), strong.getX(), strong.getY(), strong.getZ(), hammer());
        strongItem.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(strongItem);
        strongItem.playerTouch(strong);
        helper.assertTrue(strong.getInventory().countItem(hammer().getItem()) == 1, "a strong player should pick up the hammer");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void placeWithShiftAndPickUp(GameTestHelper helper) {
        // Use our own support block so the test does not depend on the height of the template floor
        BlockPos support = POS.above(2);
        BlockPos placed = support.above();
        helper.setBlock(support, Blocks.STONE);
        ServerPlayer player = TestUtil.player(helper, true);
        player.setShiftKeyDown(true);
        ItemStack stack = hammer();
        stack.enchant(enchantment(helper, Enchantments.SHARPNESS), 2);
        // shift clicking a block places the hammer on top of it
        var result = TestUtil.useOn(helper, player, stack, support);
        helper.assertBlock(placed, b -> b == ModBlocks.mjoellnir, () -> "the hammer was not placed, result: " + result + ", block there: " + helper.getBlockState(placed));
        helper.assertBlock(support, b -> b == Blocks.STONE, () -> "the support block must not be replaced");
        TileMjoellnir tile = (TileMjoellnir) helper.getBlockEntity(placed);
        helper.assertTrue(tile.getStack().getItem() == hammer().getItem() && !tile.getStack().getEnchantments().isEmpty(), "the placed hammer lost its stack data");

        // Right click without shift does not place the block (it throws the hammer instead)
        BlockPos support2 = POS.above(2).east(3);
        helper.setBlock(support2, Blocks.STONE);
        ServerPlayer other = TestUtil.player(helper, true);
        other.setShiftKeyDown(false);
        TestUtil.useOn(helper, other, hammer(), support2);
        helper.assertBlock(support2.above(), b -> b != ModBlocks.mjoellnir, () -> "the hammer must only be placed with shift");

        // Pick up again with an empty hand
        ServerPlayer picker = TestUtil.player(helper, true);
        TestUtil.useOn(helper, picker, ItemStack.EMPTY, placed);
        helper.assertBlock(placed, b -> b != ModBlocks.mjoellnir, () -> "the hammer block should have been removed after picking it up");
        helper.assertTrue(picker.getInventory().countItem(hammer().getItem()) == 1, "the hammer should be in the inventory");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void throwHitsAndReturns(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100000, 0));
        // stand still and look along +z
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 1, 6.5)), 0, 0);
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, 12.5f, 2, 10.5f);
        cow.setNoGravity(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        cow.setHealth(200);
        ItemStack stack = hammer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.gameMode.useItem(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the hammer should have left the hand");
        helper.assertTrue(player.getCooldowns().isOnCooldown(hammer().getItem()) == (MasterfulConfig.mjoellnir.ranged_cooldown > 0), "wrong cooldown state after the throw");
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Mjoellnir.class, player.getBoundingBox().inflate(50)).isEmpty(), "no hammer projectile was spawned");
        helper.succeedWhen(() -> {
            helper.assertTrue(cow.getHealth() < 200, "the cow was not damaged");
            float damage = 200 - cow.getHealth();
            helper.assertTrue(damage >= MasterfulConfig.mjoellnir.base_damage_ranged * 0.9f, "the hit did too little damage: " + damage);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(Mjoellnir.class, player.getBoundingBox().inflate(50)).isEmpty(), "the hammer is still flying");
            helper.assertTrue(player.getInventory().countItem(hammer().getItem()) == 1, "the hammer did not return to the thrower");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void throwGrantsKillPixieAdvancement(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, true);
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 1, 6.5)), 0, 0);
        var pixie = helper.spawnWithNoFreeWill(velrondevs.masterfulbotany.registry.ModEntities.alfPixie, 12.5f, 2, 10.5f);
        pixie.setNoGravity(true);
        ItemStack stack = hammer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.gameMode.useItem(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND);
        AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements().get(ResourceLocation.parse("masterfulbotany:masterfulbotany/kill_pixie"));
        helper.assertTrue(advancement != null, "kill_pixie advancement is not loaded");
        helper.succeedWhen(() -> helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(), "the kill_pixie advancement was not granted"));
    }
}
