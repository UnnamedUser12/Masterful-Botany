package velrondevs.masterfulbotany.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.masterfulbotany.common.entity.AlfPixie;
import velrondevs.masterfulbotany.common.entity.Mjoellnir;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModEntities;
import velrondevs.masterfulbotany.registry.ModItems;

public class EntityTests {

    private static final BlockPos POS = TestUtil.CENTER;

    @GameTest(template = "empty")
    public static void alfPixieAttributesAndSpawnEgg(GameTestHelper helper) {
        AlfPixie pixie = helper.spawn(ModEntities.alfPixie, POS.above(2));
        TestUtil.check(helper, pixie.getMaxHealth() == 4, "unexpected pixie health " + pixie.getMaxHealth());
        TestUtil.check(helper, !pixie.onClimbable(), "pixies do not climb");
        TestUtil.check(helper, pixie.getMaxSpawnClusterSize() == 7, "unexpected cluster size");
        TestUtil.check(helper, !pixie.causeFallDamage(30, 1, pixie.damageSources().fall()), "pixies take no fall damage");

        ServerPlayer player = TestUtil.player(helper, true);
        var result = TestUtil.useOn(helper, player, new ItemStack(ModItems.alfPixieSpawnEgg), POS.above(4));
        TestUtil.check(helper, result.consumesAction(), "the spawn egg did not work: " + result);
        TestUtil.check(helper, helper.getLevel().getEntitiesOfClass(AlfPixie.class, new AABB(helper.absolutePos(POS)).inflate(8)).size() == 2, "the spawn egg should have spawned a second pixie");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void alfPixieFliesAndHeals(GameTestHelper helper) {
        AlfPixie pixie = helper.spawn(ModEntities.alfPixie, POS.above(3));
        pixie.hurt(pixie.damageSources().generic(), 2);
        float damaged = pixie.getHealth();
        Vec3 start = pixie.position();
        helper.succeedWhen(() -> {
            TestUtil.check(helper, pixie.isAlive(), "the pixie died");
            TestUtil.check(helper, pixie.getHealth() > damaged, "the pixie did not heal");
            TestUtil.check(helper, pixie.position().distanceTo(start) > 0.3, "the pixie did not move");
        });
    }

    @GameTest(template = "empty")
    public static void alfPixieDropsPixieDust(GameTestHelper helper) {
        int dust = 0;
        for (int i = 0; i < 40; i++) {
            AlfPixie pixie = helper.spawnWithNoFreeWill(ModEntities.alfPixie, POS.getX() + 0.5f, POS.getY() + 3, POS.getZ() + 0.5f);
            pixie.hurt(pixie.damageSources().generic(), 100);
        }
        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(POS)).inflate(8))) {
            TestUtil.check(helper, item.getItem().is(BotaniaItems.pixieDust), "unexpected drop " + item.getItem());
            dust += item.getItem().getCount();
        }
        TestUtil.check(helper, dust > 0, "40 pixies dropped no pixie dust at all");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mjoellnirEntityNbtRoundTrip(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, true);
        Mjoellnir projectile = new Mjoellnir(helper.getLevel());
        ItemStack hammer = new ItemStack(ModBlocks.mjoellnir.asItem());
        projectile.setStack(hammer);
        projectile.setThrower(player);
        projectile.setThrowPos(new Vec3(1, 2, 3));
        projectile.setHotBarSlot(4);
        projectile.setLifeLeft(17);
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        projectile.saveWithoutId(output);
        CompoundTag tag = output.buildResult();
        Mjoellnir copy = new Mjoellnir(helper.getLevel());
        copy.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
        TestUtil.check(helper, copy.getStack().is(hammer.getItem()), "the stack was lost");
        TestUtil.check(helper, player.getUUID().equals(copy.getThrowerId()), "the thrower was lost");
        TestUtil.check(helper, new Vec3(1, 2, 3).equals(copy.getThrowPos()), "the throw position was lost");
        TestUtil.check(helper, copy.getHotBarSlot() == 4 && copy.getLifeLeft() == 17, "slot / lifetime were lost");
        TestUtil.check(helper, !copy.isReturning(), "should not be returning");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void mjoellnirWithoutThrowerLandsAsBlock(GameTestHelper helper) {
        Mjoellnir projectile = new Mjoellnir(helper.getLevel());
        projectile.setStack(new ItemStack(ModBlocks.mjoellnir.asItem()));
        Vec3 pos = helper.absoluteVec(new Vec3(POS.getX() + 0.5, POS.getY() + 0.2, POS.getZ() + 0.5));
        projectile.setPos(pos);
        projectile.setReturning(true);
        helper.getLevel().addFreshEntity(projectile);
        helper.succeedWhen(() -> {
            TestUtil.check(helper, projectile.isRemoved(), "the hammer entity should have vanished");
            TestUtil.check(helper, helper.getLevel().getBlockState(BlockPos.containing(pos)).is(ModBlocks.mjoellnir), "the hammer should have been placed as a block");
        });
    }
}
