package velrondevs.masterfulbotany.common.helper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.moddingx.libx.inventory.IAdvancedItemHandlerModifiable;

public class StackHandlers {

    public static ItemStack get(ResourceHandler<ItemResource> handler, int slot) {
        return ItemUtil.getStack(handler, slot);
    }

    public static void set(IAdvancedItemHandlerModifiable handler, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            handler.set(slot, ItemResource.EMPTY, 0);
        } else {
            handler.set(slot, ItemResource.of(stack), stack.getCount());
        }
    }
}
