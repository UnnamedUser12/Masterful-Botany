package velrondevs.masterfulbotany.common.item.equipment.bauble;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.moddingx.libx.creativetab.CreativeTabItemProvider;
import org.moddingx.libx.creativetab.CreativeTabX;
import velrondevs.botania.common.item.equipment.bauble.BandOfManaItem;

public class GreatestManaRing extends BandOfManaItem implements CreativeTabItemProvider {

    public static final int GREATEST_MAX_MANA = 4000000;

    public GreatestManaRing(Properties props) {
        super(props);
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return Integer.MAX_VALUE;
    }

    /**
     * Mana item implementation with a bigger capacity. Registered in the capability registration event.
     */
    public static class GreatestManaItemImpl extends ManaItemImpl {

        public GreatestManaItemImpl(ItemStack stack) {
            super(stack);
        }

        @Override
        public int getMaxMana() {
            return GREATEST_MAX_MANA * this.stack.getCount();
        }
    }

    @Override
    public Stream<ItemStack> makeCreativeTabStacks(CreativeTabX.TabContext ctx) {
        ItemStack full = new ItemStack(this);
        setMana(full, GREATEST_MAX_MANA);
        return Stream.of(new ItemStack(this), full);
    }

    @Override
    public void addToCreativeTab(net.minecraft.world.item.Item me, net.minecraft.world.item.CreativeModeTab.Output output) {
        // handled by the LibX creative tab
    }
}
