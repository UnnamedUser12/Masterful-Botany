package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.common.world.feature.AbandonedApothecaryConfiguration;
import velrondevs.masterfulbotany.common.world.feature.AbandonedApothecaryFeature;
import velrondevs.masterfulbotany.common.world.feature.ManaCrystalFeature;
import velrondevs.masterfulbotany.common.world.feature.MotifFlowerFeature;
import velrondevs.masterfulbotany.common.world.feature.WheatFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "FEATURE")
public class ModFeatures {
    
    public static final Feature<AbandonedApothecaryConfiguration> abandonedApothecaries = new AbandonedApothecaryFeature();
    public static final Feature<NoneFeatureConfiguration> manaCrystals = new ManaCrystalFeature();
    public static final Feature<NoneFeatureConfiguration> motifFlowers = new MotifFlowerFeature();
    public static final Feature<NoneFeatureConfiguration> wheatFields = new WheatFeature();
}
