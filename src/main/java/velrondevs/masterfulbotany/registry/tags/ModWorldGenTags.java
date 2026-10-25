package velrondevs.masterfulbotany.registry.tags;

import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.moddingx.libx.sandbox.generator.BiomeLayer;
import org.moddingx.libx.sandbox.SandBox;

public class ModWorldGenTags {

    public static final TagKey<BiomeLayer> ALFHEIM_LAYERS = TagKey.create(SandBox.BIOME_LAYER, MasterfulBotany.getInstance().resource("alfheim"));
    
    public static final TagKey<Biome> ALFHEIM = TagKey.create(Registries.BIOME, MasterfulBotany.getInstance().resource("alfheim"));
    public static final TagKey<Biome> ANDWARI_CAVE = TagKey.create(Registries.BIOME, MasterfulBotany.getInstance().resource("andwari_cave"));
    public static final TagKey<Biome> ELVEN_HOUSES = TagKey.create(Registries.BIOME, MasterfulBotany.getInstance().resource("elven_houses"));
}
