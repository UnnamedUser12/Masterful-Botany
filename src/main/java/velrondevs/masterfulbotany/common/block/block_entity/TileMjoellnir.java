package velrondevs.masterfulbotany.common.block.block_entity;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.moddingx.libx.base.tile.BlockEntityBase;

public class TileMjoellnir extends BlockEntityBase {

    private ItemStack stack;
    
    public TileMjoellnir(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.stack = new ItemStack(ModBlocks.mjoellnir);
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        this.setChanged();
        this.setDispatchable();
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        this.stack = input.read("hammer", ItemStack.CODEC).orElseGet(() -> new ItemStack(ModBlocks.mjoellnir));
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.store("hammer", ItemStack.OPTIONAL_CODEC, this.stack);
    }
}
