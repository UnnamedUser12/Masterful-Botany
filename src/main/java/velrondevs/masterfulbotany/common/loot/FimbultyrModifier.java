package velrondevs.masterfulbotany.common.loot;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.helper.MasterfulPlayerData;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import velrondevs.botania.common.entity.GaiaGuardianEntity;
import velrondevs.botania.registry.BotaniaEntities;

public class FimbultyrModifier extends LootModifier {
    
    public static final MapCodec<FimbultyrModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions)
    ).apply(instance, FimbultyrModifier::new));
    
    
    
    public FimbultyrModifier(LootItemCondition... conditions) {
        super(conditions);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity self = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        Entity dead = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (self instanceof Player && dead instanceof GaiaGuardianEntity && dead.getType() == BotaniaEntities.DOPPLEGANGER) {
            if (((GaiaGuardianEntity) dead).isHardMode() && MasterfulPlayerData.getData((Player) self).getBoolean("MimirKnowledge")) {
                generatedLoot.add(new ItemStack(ModItems.fimbultyrTablet));
            }
        }
        return generatedLoot;
    }
}
