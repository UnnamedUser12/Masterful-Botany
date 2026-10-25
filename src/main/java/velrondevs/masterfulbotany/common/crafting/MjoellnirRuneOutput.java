package velrondevs.masterfulbotany.common.crafting;

import com.google.common.collect.ImmutableList;
import java.util.List;
import velrondevs.masterfulbotany.common.block.BlockMjoellnir;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MjoellnirRuneOutput extends SpecialRuneOutput {

    public static final MjoellnirRuneOutput INSTANCE = new MjoellnirRuneOutput();
    
    private MjoellnirRuneOutput() {
        super(MasterfulBotany.getInstance().resource("mjoellnir"));
    }

    @Override
    public void apply(Level level, BlockPos center, List<ItemStack> consumedStacks) {
        BlockMjoellnir.putInWorld(new ItemStack(ModBlocks.mjoellnir), level, center, false);
    }

    @Override
    public List<ItemStack> getJeiOutputItems() {
        return ImmutableList.of(new ItemStack(ModBlocks.mjoellnir));
    }
}
