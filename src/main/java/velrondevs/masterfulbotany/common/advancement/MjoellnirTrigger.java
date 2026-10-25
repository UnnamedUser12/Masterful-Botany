package velrondevs.masterfulbotany.common.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class MjoellnirTrigger extends SimpleCriterionTrigger<MjoellnirTrigger.Instance> {

    @Nonnull
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack item, Entity entity) {
        LootContext ctx = EntityPredicate.createContext(player, entity);
        this.trigger(player, instance -> instance.matches(item, ctx));
    }

    public record Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Instance::item),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(Instance::entity)
        ).apply(inst, Instance::new));

        public boolean matches(ItemStack item, LootContext entity) {
            return (this.item.isEmpty() || this.item.get().test(item)) && (this.entity.isEmpty() || this.entity.get().matches(entity));
        }
    }
}
