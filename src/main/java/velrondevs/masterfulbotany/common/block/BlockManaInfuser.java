package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.block.block_entity.TileManaInfuser;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.crafting.RecipeHelper;
import org.moddingx.libx.mod.ModX;

public class BlockManaInfuser extends BlockBE<TileManaInfuser> {

    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);

    public BlockManaInfuser(ModX mod, Class<TileManaInfuser> teClass, Properties properties) {
        super(mod, teClass, properties);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @SuppressWarnings("deprecation")
    public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull PathComputationType type) {
        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos pos) {
        TileManaInfuser te = this.getBlockEntity(level, pos);
        double progress = te.getProgress();
        if (progress < 0) {
            return 0;
        } else {
            return 1 + (int) (Math.round(progress * 14));
        }
    }

    @Nonnull
    @Override
    protected ItemInteractionResult useItemOn(@Nonnull ItemStack stack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide) {
            if (!stack.isEmpty() && RecipeHelper.isItemValidInput(level.getRecipeManager(), ModRecipes.infuser, stack)) {
                ItemStack copy = stack.copy();
                stack.shrink(1);
                player.setItemInHand(hand, stack);
                copy.setCount(1);
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, copy);
                entity.setDeltaMovement(0, 0, 0);
                entity.setThrower(player);
                entity.setPickUpDelay(40);
                level.addFreshEntity(entity);
                MasterfulBotany.getNetwork().setItemMagnetImmune(entity);
                return ItemInteractionResult.SUCCESS;
            } else {
                return super.useItemOn(stack, state, level, pos, player, hand, hit);
            }
        } else {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
    }
}
