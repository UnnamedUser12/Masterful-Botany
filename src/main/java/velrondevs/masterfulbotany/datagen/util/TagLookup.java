package velrondevs.masterfulbotany.datagen.util;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagLookup {

    public static final HolderGetter<Item> ITEMS = new HolderGetter<>() {

        @Override
        public Optional<Holder.Reference<Item>> get(ResourceKey<Item> key) {
            return BuiltInRegistries.ITEM.get(key);
        }

        @Override
        public Optional<HolderSet.Named<Item>> get(TagKey<Item> tag) {
            return Optional.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
        }
    };

    private TagLookup() {

    }
}
