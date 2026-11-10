package velrondevs.masterfulbotany.common.block.block_entity.flower;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.TileCentralRuneHolder;
import velrondevs.masterfulbotany.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class Petrunia extends FunctionalFlowerBase {
    
    @Nullable
    private BlockPos currentPos = null;
    private int storedMana = 0;
    
    public Petrunia(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0xB71A1A, false);
    }
    
    @Override
    protected void tickFlower() {
        if (!this.level.isClientSide()) {
            if (this.currentPos == null) {
                find: for (int x = -3; x <= 3; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -3; z <= 3; z++) {
                            BlockPos pos = this.worldPosition.offset(x, y, z);
                            if (this.level.getBlockState(pos).getBlock() == ModBlocks.centralRuneHolder) {
                                this.currentPos = pos.immutable();
                                break find;
                            }
                        }
                    }
                }
            }
            if (this.currentPos != null) {
                BlockState state = this.level.getBlockState(this.currentPos);
                BlockEntity te = this.level.getBlockEntity(this.currentPos);
                if (state.getBlock() != ModBlocks.centralRuneHolder || !(te instanceof TileCentralRuneHolder tile)) {
                    this.currentPos = null;
                } else {
                    tile.tryStartRitual(
                            msg -> {},
                            m -> {
                                if (this.storedMana >= m) {
                                    return true;
                                } else {
                                    int transfer = Math.min(Math.max(0, m - this.storedMana), this.mana);
                                    this.storedMana += transfer;
                                    this.mana -= transfer;
                                    return this.storedMana >= m;
                                }
                            },
                            m -> {
                                this.storedMana = Math.max(0, this.storedMana - m);
                                this.setChanged();
                            }
                    );
                }
            }
        }
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        this.storedMana = input.getIntOr("StoredMana", 0);
        this.currentPos = input.read("CurrentRuneTargetPos", BlockPos.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("StoredMana", this.storedMana);
        if (this.currentPos != null) output.store("CurrentRuneTargetPos", BlockPos.CODEC, this.currentPos);
    }
}
