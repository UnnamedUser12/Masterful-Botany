package velrondevs.masterfulbotany.common.block.block_entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.registry.tags.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.moddingx.libx.base.tile.BlockEntityBase;
import org.moddingx.libx.inventory.BaseItemStackHandler;

public class TileRuneHolder extends BlockEntityBase {

    private final BaseItemStackHandler inventory = BaseItemStackHandler.builder(1)
            .contentsChanged(() -> {
                this.setChanged();
                this.setDispatchable();
            })
            .validator(stack -> stack.is(ModItemTags.RITUAL_RUNES), 0)
            .defaultSlotLimit(1)
            .build();

    @Nullable
    private BlockPos target;
    private double floatProgress;
    
    public TileRuneHolder(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public void loadAdditional(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.inventory.deserializeNBT(registries, nbt.getCompound("Inventory"));
        this.target = NbtUtils.readBlockPos(nbt, "TargetPos").orElse(null);
        this.floatProgress = nbt.getDouble("FloatProgress");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("Inventory", this.inventory.serializeNBT(registries));
        if (this.target != null) {
            nbt.put("TargetPos", NbtUtils.writeBlockPos(this.target));
            nbt.putDouble("FloatProgress", this.floatProgress);
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        //noinspection ConstantConditions
        if (!this.level.isClientSide) {
            nbt.put("Inventory", this.inventory.serializeNBT(registries));
            if (this.target != null) {
                nbt.put("TargetPos", NbtUtils.writeBlockPos(this.target));
                nbt.putDouble("FloatProgress", this.floatProgress);
            }
        }
        return nbt;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider registries) {
        super.handleUpdateTag(nbt, registries);
        //noinspection ConstantConditions
        if (this.level.isClientSide) {
            this.inventory.deserializeNBT(registries, nbt.getCompound("Inventory"));
            this.target = NbtUtils.readBlockPos(nbt, "TargetPos").orElse(null);
            this.floatProgress = nbt.getDouble("FloatProgress");
        }
    }

    @Nullable
    public BlockPos getTarget() {
        return this.target;
    }

    public double getFloatProgress() {
        return this.floatProgress;
    }

    public void setTarget(@Nullable BlockPos target, double floatProgress, boolean sync) {
        this.target = target;
        if (target != null) {
            this.floatProgress = Mth.clamp(floatProgress, 0, 1);
        } else {
            this.floatProgress = 0;
        }
        this.setChanged();
        if (sync) {
            this.setDispatchable();
        }
    }

}
