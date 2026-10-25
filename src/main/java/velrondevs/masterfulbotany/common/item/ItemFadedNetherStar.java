package velrondevs.masterfulbotany.common.item;

import java.util.stream.Stream;
import velrondevs.masterfulbotany.common.block.block_entity.flower.WitherAconite;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.base.ItemBase;
import org.moddingx.libx.creativetab.CreativeTabX;

public class ItemFadedNetherStar extends ItemBase {

    public ItemFadedNetherStar() {
        // The max damage is replaced with the config value in ModDefaultComponents
        super(MasterfulBotany.getInstance(), new Properties().stacksTo(1).durability(WitherAconite.DEFAULT_MANA_PER_STAR));
    }

    @Override
    public Stream<ItemStack> makeCreativeTabStacks(CreativeTabX.TabContext ctx) {
        return Stream.empty();
    }
}
