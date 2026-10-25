package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.client.render.block_entity.RenderYggdrasilBranch;
import velrondevs.masterfulbotany.common.block.block_entity.TileYggdrasilBranch;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.block.RotationShape;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.SetupContext;
import org.moddingx.libx.registration.util.ClientExtensionInfo;
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
    @OnlyIn(Dist.CLIENT)
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> BlockEntityRenderers.register(this.getBlockEntityType(), mgr -> new RenderYggdrasilBranch()));
        ctx.enqueue(() -> ItemStackRenderer.addRenderBlock(this.getBlockEntityType(), false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClientAdditional(RegistrationContext ctx, EntryCollector builder) {
        super.registerClientAdditional(ctx, builder);
        builder.register(null, new ClientExtensionInfo.Item(ItemStackRenderer.createProperties()));
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
    @SuppressWarnings("deprecation")
    protected ItemInteractionResult useItemOn(@Nonnull ItemStack handStack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        TileYggdrasilBranch tile = this.getBlockEntity(level, pos);
        if (handStack.getItem() instanceof WandOfTheForestItem) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else if (!tile.getInventory().getStackInSlot(0).isEmpty()) {
            if (!level.isClientSide) {
                if (player.getItemInHand(hand).isEmpty()) {
                    player.setItemInHand(hand, tile.getInventory().getStackInSlot(0).copy());
                } else {
                    player.getInventory().add(tile.getInventory().getStackInSlot(0).copy());
                }
                tile.getInventory().setStackInSlot(0, ItemStack.EMPTY);
            }
            return ItemInteractionResult.CONSUME;
        } else if (player.getItemInHand(hand).getItem() == ModItems.gjallarHornEmpty && player.getItemInHand(hand).getCount() == 1) {
            if (!level.isClientSide) {
                tile.getInventory().setStackInSlot(0, player.getItemInHand(hand).copy());
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return ItemInteractionResult.CONSUME;
        } else {
            return super.useItemOn(handStack, state, level, pos, player, hand, hit);
        }
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE.getShape(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
