package velrondevs.masterfulbotany.common.block.block_entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.registry.tags.ModItemTags;
import net.minecraft.core.BlockPos;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        this.inventory.deserialize(input.childOrEmpty("Inventory"));
        this.target = input.read("TargetPos", BlockPos.CODEC).orElse(null);
        this.floatProgress = input.getDoubleOr("FloatProgress", 0);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        this.inventory.serialize(output.child("Inventory"));
        if (this.target != null) {
            output.store("TargetPos", BlockPos.CODEC, this.target);
            output.putDouble("FloatProgress", this.floatProgress);
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
