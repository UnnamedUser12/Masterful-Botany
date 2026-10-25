package velrondevs.masterfulbotany.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record AbandonedApothecaryConfiguration(List<BlockState> states, List<Item> petals) implements FeatureConfiguration {
    
    public static final Codec<AbandonedApothecaryConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.listOf().fieldOf("states").forGetter(AbandonedApothecaryConfiguration::states),
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("petals").forGetter(AbandonedApothecaryConfiguration::petals)
    ).apply(instance, AbandonedApothecaryConfiguration::new));
}
