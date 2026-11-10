package velrondevs.masterfulbotany.datagen.providers.loot;

import velrondevs.masterfulbotany.common.loot.AlfsteelDisposeModifier;
import velrondevs.masterfulbotany.common.loot.FimbultyrModifier;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.GlobalLootProviderBase;

public class GlobalLootProvider extends GlobalLootProviderBase {

    public GlobalLootProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    protected void setup() {
        this.add("dispose", new AlfsteelDisposeModifier(new LootItemCondition[0], IGlobalLootModifier.DEFAULT_PRIORITY));
        this.add("fimbultyr", new FimbultyrModifier(new LootItemCondition[0], IGlobalLootModifier.DEFAULT_PRIORITY));
        this.addition("alfsteel_template", MasterfulBotany.getInstance().id("entity_addition/alfsteel_template")).or(this.conditions()
                .forLootTable(EntityType.WITCH.getDefaultLootTable().orElseThrow().identifier())
                .forLootTable(EntityType.EVOKER.getDefaultLootTable().orElseThrow().identifier())
        ).build();
    }
}
