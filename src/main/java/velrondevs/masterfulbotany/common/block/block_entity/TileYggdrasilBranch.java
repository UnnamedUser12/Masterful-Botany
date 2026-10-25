package velrondevs.masterfulbotany.common.block.block_entity;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.moddingx.libx.base.tile.TickingBlock;
import org.moddingx.libx.inventory.BaseItemStackHandler;

public class TileYggdrasilBranch extends BlockEntityMana implements TickingBlock {

    private final BaseItemStackHandler inventory = BaseItemStackHandler.builder(1)
            .contentsChanged(() -> {
                this.setChanged();
                this.setDispatchable();
            })
            .validator(stack -> stack.getItem() == ModItems.gjallarHornEmpty, 0)
            .build();

    private int progress = 0;
    
    public TileYggdrasilBranch(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 10000, true, false);
    }

    @Override
    protected boolean canReceive() {
        return true;
    }

    @Override
    public void tick() {
        if (this.inventory.getStackInSlot(0).getItem() == ModItems.gjallarHornEmpty && this.inventory.getStackInSlot(0).getCount() == 1) {
            if (this.mana >= 20) {
                //noinspection ConstantConditions
                if (!this.level.isClientSide) {
                    this.mana -= 10;
                    this.progress += 1;
                    if (this.progress >= 600) {
                        this.inventory.setStackInSlot(0, new ItemStack(ModItems.gjallarHornFull));
                        this.progress = 0;
                    }
                    this.setChanged();
                    this.setDispatchable();
                } else if (this.level.getGameTime() % 4 == 0) {
                    double xf = 0.5;
                    double zf = 0.35;
                    Direction dir = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (dir.getAxis() == Direction.Axis.X) {
                        double tmp = xf;
                        xf = zf;
                        zf = tmp;
                    }
                    if (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                        xf = 1 - xf;
                        zf = 1 - zf;
                    }
                    this.level.addParticle(ParticleTypes.DRIPPING_WATER, this.worldPosition.getX() + xf, this.worldPosition.getY() + 0.76, this.worldPosition.getZ() + zf, 0, -0.2, 0);
                }
            }
        } else if (this.progress != 0) {
            this.progress = 0;
            this.setChanged();
            this.setDispatchable();
        }
    }

    @Override
    public void loadAdditional(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.inventory.deserializeNBT(registries, nbt.getCompound("Inventory"));
        this.progress = nbt.getInt("Progress");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("Inventory", this.inventory.serializeNBT(registries));
        nbt.putInt("Progress", this.progress);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        //noinspection ConstantConditions
        if (!this.level.isClientSide) {
            nbt.put("Inventory", this.inventory.serializeNBT(registries));
        }
        return nbt;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider registries) {
        super.handleUpdateTag(nbt, registries);
        //noinspection ConstantConditions
        if (this.level.isClientSide) {
            this.inventory.deserializeNBT(registries, nbt.getCompound("Inventory"));
        }
    }

    public IItemHandlerModifiable getInventory() {
        return this.inventory;
    }
}
