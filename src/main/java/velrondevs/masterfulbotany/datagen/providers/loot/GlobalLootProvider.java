package velrondevs.masterfulbotany.datagen.providers.loot;

import velrondevs.masterfulbotany.common.loot.AlfsteelDisposeModifier;
import velrondevs.masterfulbotany.common.loot.FimbultyrModifier;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.world.entity.EntityType;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.GlobalLootProviderBase;

public class GlobalLootProvider extends GlobalLootProviderBase {

    public GlobalLootProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    protected void setup() {
        this.add("dispose", new AlfsteelDisposeModifier());
        this.add("fimbultyr", new FimbultyrModifier());
        this.addition("alfsteel_template", MasterfulBotany.getInstance().resource("entity_addition/alfsteel_template")).or(this.conditions()
                .forLootTable(EntityType.WITCH.getDefaultLootTable().location())
                .forLootTable(EntityType.EVOKER.getDefaultLootTable().location())
        ).build();
    }
}
