package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.render.block_entity.RenderAlfsteelPylon;
import velrondevs.masterfulbotany.common.block.block_entity.TileAlfsteelPylon;
import velrondevs.masterfulbotany.common.pylon.PylonRepairable;
import velrondevs.masterfulbotany.common.pylon.PylonRepairables;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.SetupContext;
import org.moddingx.libx.registration.util.ClientExtensionInfo;

public class BlockAlfsteelPylon extends BlockBE<TileAlfsteelPylon> {

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 21.0D, 14.0D);

    public BlockAlfsteelPylon(ModX mod, Properties properties) {
        super(mod, TileAlfsteelPylon.class, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> BlockEntityRenderers.register(this.getBlockEntityType(), RenderAlfsteelPylon::new));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClientAdditional(RegistrationContext ctx, EntryCollector builder) {
        super.registerClientAdditional(ctx, builder);
        builder.register(null, new ClientExtensionInfo.Item(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RenderAlfsteelPylon.ItemRenderer();
            }
        }));
    }
    
    @Nonnull
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nonnull
    @Override
    protected ItemInteractionResult useItemOn(@Nonnull ItemStack stack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide) {
            PylonRepairable repairable = PylonRepairables.getRepairInfo(stack);
            if (stack.getCount() == 1 && repairable != null && repairable.canRepairPylon(stack)) {
                ItemStack copy = stack.copy();
                player.setItemInHand(hand, ItemStack.EMPTY);
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, copy);
                entity.setDeltaMovement(0, 0, 0);
                entity.setThrower(player);
                entity.setPickUpDelay(40);
                level.addFreshEntity(entity);
                return ItemInteractionResult.SUCCESS;
            } else {
                return super.useItemOn(stack, state, level, pos, player, hand, hit);
            }
        } else {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 15;
    }
}
