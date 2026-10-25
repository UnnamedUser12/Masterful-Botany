package velrondevs.masterfulbotany.datagen.providers.tags;

import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimBiomeLayers;
import velrondevs.masterfulbotany.registry.tags.ModWorldGenTags;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.TagProviderBase;
import org.moddingx.libx.sandbox.generator.BiomeLayer;
import org.moddingx.libx.sandbox.SandBox;

public class BiomeLayerTagsProvider extends TagProviderBase<BiomeLayer> {
    
    private final AlfheimBiomeLayers biomeLayers;
    
    public BiomeLayerTagsProvider(DatagenContext ctx) {
        super(ctx, SandBox.BIOME_LAYER);
        this.biomeLayers = ctx.findRegistryProvider(AlfheimBiomeLayers.class);
    }

    @Override
    protected void setup() {
        this.tag(ModWorldGenTags.ALFHEIM_LAYERS).add(this.biomeLayers.alfheim.value());
    }
}
