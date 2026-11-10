package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.Item;

public class ModEnchantments {

    public static final ResourceKey<Enchantment> HAMMER_MOBILITY = ResourceKey.create(Registries.ENCHANTMENT, MasterfulBotany.getInstance().id("hammer_mobility"));
    public static final TagKey<Item> MJOELLNIR_ENCHANTABLE = ItemTags.create(MasterfulBotany.getInstance().id("enchantable/mjoellnir"));

    public static int level(net.minecraft.world.item.ItemStack stack, ResourceKey<Enchantment> key) {
        for (var entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().is(key)) return entry.getIntValue();
        }
        return 0;
    }
}
