package velrondevs.masterfulbotany.datagen.providers.loot;

import velrondevs.masterfulbotany.registry.ModEntities;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.EntityLootProviderBase;
import velrondevs.botania.registry.BotaniaItems;

public class EntityLootProvider extends EntityLootProviderBase {

    public EntityLootProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setup() {
        this.drops(ModEntities.alfPixie, this.stack(BotaniaItems.pixieDust)
                .with(this.count(0, 1))
                .with(this.looting(2))
        );
    }
}
