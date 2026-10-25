package velrondevs.masterfulbotany.registry.tags;

import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {

    public static final TagKey<Block> ALFHEIM_LOGS = BlockTags.create(MasterfulBotany.getInstance().resource("alfheim_logs"));
    public static final TagKey<Block> ALFHEIM_LEAVES = BlockTags.create(MasterfulBotany.getInstance().resource("alfheim_leaves"));
    public static final TagKey<Block> BASE_STONE_ALFHEIM = BlockTags.create(MasterfulBotany.getInstance().resource("base_stone_alfheim"));
    public static final TagKey<Block> ALFHEIM_ORES = BlockTags.create(MasterfulBotany.getInstance().resource("alfheim_ores"));
}
