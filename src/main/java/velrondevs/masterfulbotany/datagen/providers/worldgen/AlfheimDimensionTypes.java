package velrondevs.masterfulbotany.datagen.providers.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.dimension.DimensionType;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.sandbox.DimensionTypeProviderBase;

public class AlfheimDimensionTypes extends DimensionTypeProviderBase {

    public final Holder<DimensionType> alfheim = this.dimension()
            .attributes(this.attributes().putAll(DimensionTypeProviderBase.OVERWORLD_ATTRIBUTES).set(EnvironmentAttributes.CAN_START_RAID, false))
            .build();
            
    public AlfheimDimensionTypes(DatagenContext ctx) {
        super(ctx);
    }
}
