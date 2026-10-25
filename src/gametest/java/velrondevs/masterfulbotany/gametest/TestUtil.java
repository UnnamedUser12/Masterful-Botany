package velrondevs.masterfulbotany.gametest;

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
import net.minecraft.resources.ResourceLocation;
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

/**
 * Shared helpers for the GameTests. Positions are relative to the test structure
 * (24x16x24 with a stone floor at y=0), the center of the floor is (12, 0, 12).
 */
public final class TestUtil {

    public static final BlockPos CENTER = new BlockPos(12, 1, 12);

    private TestUtil() {}

    public static ServerPlayer player(GameTestHelper helper) {
        return player(helper, true);
    }

    /**
     * Creates a mock player. Creative players ignore a lot of restrictions, survival ones behave like real players.
     */
    public static ServerPlayer player(GameTestHelper helper, boolean creative) {
        // Same as GameTestHelper#makeMockServerPlayerInLevel, but the mock connection accepts every payload channel.
        // Otherwise modded packets sent while the player joins (curios sync, ...) throw on the fake connection.
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
        channel.attr(ChannelAttributes.ADHOC_CHANNELS).set(new HashSet<ResourceLocation>() {
            @Override
            public boolean contains(Object o) {
                return true;
            }
        });
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        // Test servers put new players in creative mode, which disables damage and item wear
        try {
            java.lang.reflect.Field invulnerable = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            invulnerable.setAccessible(true);
            invulnerable.setInt(player, 0);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        player.setGameMode(creative ? net.minecraft.world.level.GameType.CREATIVE : net.minecraft.world.level.GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(12.5, 1, 6.5));
        player.moveTo(pos.x, pos.y, pos.z, 0, 0);
        return player;
    }

    /**
     * Right clicks a block with the given hand item of the player.
     */
    public static InteractionResult useOn(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos relPos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(relPos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
        return player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND, hit);
    }

    /**
     * Saves a block entity and loads it back into a new instance.
     */
    public static BlockEntity roundTrip(GameTestHelper helper, BlockEntity be) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag tag = be.saveWithFullMetadata(registries);
        BlockState state = be.getBlockState();
        BlockEntity copy = BlockEntity.loadStatic(be.getBlockPos(), state, tag, registries);
        if (copy == null) throw new IllegalStateException("Could not load block entity back from nbt: " + tag);
        return copy;
    }

    /**
     * Creates a mana tablet with the given amount of mana. Mana consumers in the player inventory use it.
     */
    public static ItemStack tablet(int mana) {
        ItemStack stack = new ItemStack(BotaniaItems.manaTablet);
        ManaItem item = XplatAbstractions.INSTANCE.findManaItem(stack);
        item.addMana(mana);
        return stack;
    }

    /**
     * Puts an item into the first slot of the given curios slot type (e.g. "ring") of the entity.
     */
    public static void equipCurio(net.minecraft.world.entity.LivingEntity entity, String slotType, ItemStack stack) {
        var inventory = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(entity).orElseThrow(() -> new IllegalStateException("entity has no curios inventory"));
        var handler = inventory.getStacksHandler(slotType).orElseThrow(() -> new IllegalStateException("entity has no curios slot of type " + slotType));
        handler.getStacks().setStackInSlot(0, stack);
    }

    /**
     * The mana stored in the given item (a mana tablet for example).
     */
    public static int mana(ItemStack stack) {
        return XplatAbstractions.INSTANCE.findManaItem(stack).getMana();
    }
}
