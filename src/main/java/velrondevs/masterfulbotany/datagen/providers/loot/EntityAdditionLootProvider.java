package velrondevs.masterfulbotany.datagen.providers.loot;

import javax.annotation.Nullable;
import velrondevs.masterfulbotany.registry.ModItems;
import velrondevs.masterfulbotany.registry.tags.ModItemTags;
import velrondevs.masterfulbotany.datagen.util.TagLookup;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.LootProviderBase;

public class EntityAdditionLootProvider extends LootProviderBase<String> {
    
    public EntityAdditionLootProvider(DatagenContext ctx) {
        super(ctx, "entity_addition", LootContextParamSets.ENTITY, ctx.mod()::id);
    }
    
    @Override
    protected void setup() {
        this.drops("alfsteel_template", this.stack(ModItems.alfsteelTemplate)
                .with(LootItemKilledByPlayerCondition.killedByPlayer())
                .with(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, new EntityPredicate.Builder()
                        .equipment(EntityEquipmentPredicate.Builder.equipment()
                                .mainhand(ItemPredicate.Builder.item()
                                        .of(TagLookup.ITEMS, ModItemTags.ELEMENTIUM_WEAPONS)
                                ).build()
                        )
                ))
                .with(this.random(0.15f))
        );
    }

    @Nullable
    @Override
    protected LootTable.Builder defaultBehavior(String item) {
        return null;
    }
}
