package velrondevs.masterfulbotany.common.crafting;

import java.util.List;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.ItemStack;

public record StackListInput(List<ItemStack> stacks) implements RecipeInput {

    @Override
    public ItemStack getItem(int slot) {
        return this.stacks.get(slot);
    }

    @Override
    public int size() {
        return this.stacks.size();
    }
}
