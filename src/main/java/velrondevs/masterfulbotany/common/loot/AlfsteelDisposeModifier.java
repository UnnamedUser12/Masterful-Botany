package velrondevs.masterfulbotany.common.loot;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.item.equipment.AlfsteelPick;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import velrondevs.botania.common.lib.BotaniaTags;

public class AlfsteelDisposeModifier extends LootModifier {
    
    public static final MapCodec<AlfsteelDisposeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions)
    ).apply(instance, AlfsteelDisposeModifier::new));

    public AlfsteelDisposeModifier(LootItemCondition... conditions) {
        super(conditions);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (entity != null && tool != null && !tool.isEmpty()) {
            filterDisposable(generatedLoot, entity, tool);
        }
        return generatedLoot;
    }

    public static void filterDisposable(List<ItemStack> drops, Entity entity, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == ModItems.alfsteelPick && AlfsteelPick.isTipped(stack)) {
            drops.removeIf((loot) -> !loot.isEmpty() && (loot.is(BotaniaTags.Items.DISPOSABLE) || (loot.is(BotaniaTags.Items.SEMI_DISPOSABLE) && !entity.isShiftKeyDown())));
        }
    }
}
