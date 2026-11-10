package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.render.block_entity.RenderRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import velrondevs.masterfulbotany.common.helper.StackHandlers;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.SetupContext;

public class BlockRuneHolder<T extends TileRuneHolder> extends BlockBE<T> {

    public static final VoxelShape SHAPE = Shapes.or(
            box(6.5, 1, 6.5, 9.5, 3, 9.5),
            box(5, 0, 5, 11, 1, 11)
    );
    
    public BlockRuneHolder(ModX mod, Class<T> tileClass, Properties properties) {
        this(mod, tileClass, properties, new Item.Properties());
    }

    public BlockRuneHolder(ModX mod, Class<T> tileClass, Properties properties, Item.Properties itemProperties) {
        super(mod, tileClass, properties, itemProperties);
    }

    @Override
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> BlockEntityRenderers.register(this.getBlockEntityType(), mgr -> new RenderRuneHolder()));
    }

    @Nonnull
    @Override
    protected InteractionResult useItemOn(@Nonnull ItemStack handStack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        TileRuneHolder tile = this.getBlockEntity(level, pos);
        if (!StackHandlers.get(tile.getInventory(), 0).isEmpty()) {
            if (!level.isClientSide()) {
                ItemStack held = player.getItemInHand(hand);
                ItemStack stack = StackHandlers.get(tile.getInventory(), 0);
                if (held.isEmpty()) {
                    player.setItemInHand(hand, stack.copy());
                } else if (ItemStack.isSameItemSameComponents(held, stack) && held.getCount() + stack.getCount() <= held.getMaxStackSize()) {
                    held.grow(stack.getCount());
                    player.setItemInHand(hand, held);
                } else {
                    player.getInventory().add(StackHandlers.get(tile.getInventory(), 0).copy());
                }
                StackHandlers.set(tile.getInventory(), 0, ItemStack.EMPTY);
            }
            return InteractionResult.SUCCESS;
        } else if (!player.getItemInHand(hand).isEmpty() && tile.getInventory().isValid(0, ItemResource.of(player.getItemInHand(hand))) && player.getItemInHand(hand).getCount() >= 1) {
            if (!level.isClientSide()) {
                ItemStack held = player.getItemInHand(hand);
                ItemStack stack = held.split(1);
                StackHandlers.set(tile.getInventory(), 0, stack);
                player.setItemInHand(hand, held);
            }
            return InteractionResult.SUCCESS;
        } else {
            return super.useItemOn(handStack, state, level, pos, player, hand, hit);
        }
    }
    
    @Nonnull
    @Override
    protected VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }
    
    @Nonnull
    @Override
    protected BlockState updateShape(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull ScheduledTickAccess ticks, @Nonnull BlockPos pos, @Nonnull Direction direction, @Nonnull BlockPos neighborPos, @Nonnull BlockState neighborState, @Nonnull RandomSource random) {
        return direction == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }
    
    @Nonnull
    @Override
    public PushReaction getPistonPushReaction(@Nonnull BlockState state) {
        return PushReaction.DESTROY;
    }
}
