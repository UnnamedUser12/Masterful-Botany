package velrondevs.masterfulbotany.common.world.feature;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import velrondevs.botania.registry.BotaniaBlocks;

public class MotifFlowerFeature extends Feature<NoneFeatureConfiguration> {

    private static final List<BlockState> FLOWERS = ImmutableList.of(
            BotaniaBlocks.motifDaybloom.defaultBlockState(),
            BotaniaBlocks.motifNightshade.defaultBlockState()
    );

    public MotifFlowerFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockState state = FLOWERS.get(context.random().nextInt(FLOWERS.size()));
        if (state.canSurvive(context.level(), context.origin())) {
            return context.level().setBlock(context.origin(), state, 2);
        } else {
            return false;
        }
    }
}
