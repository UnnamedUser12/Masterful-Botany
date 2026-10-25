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
import net.minecraft.world.item.ItemStack;

public class AlfRepairTrigger extends SimpleCriterionTrigger<AlfRepairTrigger.Instance> {

    @Nonnull
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack item) {
        this.trigger(player, instance -> instance.matches(item));
    }

    public record Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Instance::item)
        ).apply(inst, Instance::new));

        public boolean matches(ItemStack stack) {
            return this.item.isEmpty() || this.item.get().test(stack);
        }
    }
}
