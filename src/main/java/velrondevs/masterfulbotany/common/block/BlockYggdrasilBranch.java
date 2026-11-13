package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.client.render.MasterfulClientSetup;
import velrondevs.masterfulbotany.common.block.block_entity.TileYggdrasilBranch;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import velrondevs.masterfulbotany.common.helper.StackHandlers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.block.RotationShape;
import org.moddingx.libx.mod.ModX;

import org.moddingx.libx.registration.SetupContext;

import org.moddingx.libx.render.ItemStackRenderer;
import velrondevs.botania.common.item.WandOfTheForestItem;

public class BlockYggdrasilBranch extends BlockBE<TileYggdrasilBranch> {

    public static final RotationShape SHAPE = new RotationShape(box(5, 0, 8, 11, 12, 14));
    
    public BlockYggdrasilBranch(ModX mod, Properties properties) {
        this(mod, properties, new Item.Properties());
    }

    public BlockYggdrasilBranch(ModX mod, Properties properties, Item.Properties itemProperties) {
        super(mod, TileYggdrasilBranch.class, properties, itemProperties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
    }

    @Override
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> MasterfulClientSetup.yggdrasilBranch(this.getBlockEntityType()));
        ctx.enqueue(() -> ItemStackRenderer.addRenderBlock(this.getBlockEntityType(), false));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nonnull
    @Override
    protected InteractionResult useItemOn(@Nonnull ItemStack handStack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        TileYggdrasilBranch tile = this.getBlockEntity(level, pos);
        if (handStack.getItem() instanceof WandOfTheForestItem) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        } else if (!StackHandlers.get(tile.getInventory(), 0).isEmpty()) {
            if (!level.isClientSide()) {
                if (player.getItemInHand(hand).isEmpty()) {
                    player.setItemInHand(hand, StackHandlers.get(tile.getInventory(), 0).copy());
                } else {
                    player.getInventory().add(StackHandlers.get(tile.getInventory(), 0).copy());
                }
                StackHandlers.set(tile.getInventory(), 0, ItemStack.EMPTY);
            }
            return InteractionResult.CONSUME;
        } else if (player.getItemInHand(hand).getItem() == ModItems.gjallarHornEmpty && player.getItemInHand(hand).getCount() == 1) {
            if (!level.isClientSide()) {
                StackHandlers.set(tile.getInventory(), 0, player.getItemInHand(hand).copy());
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return InteractionResult.CONSUME;
        } else {
            return super.useItemOn(handStack, state, level, pos, player, hand, hit);
        }
    }
    
    @Nonnull
    @Override
    protected VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE.getShape(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
