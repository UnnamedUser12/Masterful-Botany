package velrondevs.masterfulbotany.gametest;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import velrondevs.botania.api.mana.ManaItem;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.botania.xplat.XplatAbstractions;

public final class TestUtil {

    public static final BlockPos CENTER = new BlockPos(12, 1, 12);

    private TestUtil() {}

    public static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    public static void checkNot(GameTestHelper helper, boolean condition, String message) {
        helper.assertFalse(condition, Component.literal(message));
    }

    public static void assertBlock(GameTestHelper helper, BlockPos pos, Predicate<Block> predicate, Supplier<String> message) {
        helper.assertBlock(pos, predicate, block -> Component.literal(message.get()));
    }

    public static void fail(GameTestHelper helper, String message) {
        helper.fail(Component.literal(message));
    }

    public static ItemStack firstStack(Ingredient ingredient) {
        return new ItemStack(ingredient.items().findFirst().orElseThrow());
    }

    public static ServerPlayer player(GameTestHelper helper) {
        return player(helper, true);
    }

    public static ServerPlayer player(GameTestHelper helper, boolean creative) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return creative;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        channel.attr(ChannelAttributes.ADHOC_CHANNELS).set(new HashSet<Identifier>() {
            @Override
            public boolean contains(Object o) {
                return true;
            }
        });
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.connection.markClientLoaded();
        player.setGameMode(creative ? net.minecraft.world.level.GameType.CREATIVE : net.minecraft.world.level.GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(12.5, 1, 6.5));
        player.snapTo(pos.x, pos.y, pos.z, 0, 0);
        return player;
    }

    public static InteractionResult useOn(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos relPos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(relPos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
        return player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND, hit);
    }

    public static BlockEntity roundTrip(GameTestHelper helper, BlockEntity be) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag tag = be.saveWithFullMetadata(registries);
        BlockState state = be.getBlockState();
        BlockEntity copy = BlockEntity.loadStatic(be.getBlockPos(), state, tag, registries);
        if (copy == null) throw new IllegalStateException("Could not load block entity back from nbt: " + tag);
        return copy;
    }

    public static ItemStack tablet(int mana) {
        ItemStack stack = new ItemStack(BotaniaItems.manaTablet);
        ManaItem item = XplatAbstractions.INSTANCE.findManaItem(stack);
        item.addMana(mana);
        return stack;
    }

    public static void equipCurio(net.minecraft.world.entity.LivingEntity entity, String slotType, ItemStack stack) {
        var inventory = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(entity).orElseThrow(() -> new IllegalStateException("entity has no curios inventory"));
        var handler = inventory.getStacksHandler(slotType).orElseThrow(() -> new IllegalStateException("entity has no curios slot of type " + slotType));
        handler.getStacks().setStackInSlot(0, stack);
    }

    public static int mana(ItemStack stack) {
        return XplatAbstractions.INSTANCE.findManaItem(stack).getMana();
    }
}
