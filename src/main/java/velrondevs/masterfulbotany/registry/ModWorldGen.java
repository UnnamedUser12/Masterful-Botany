package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.common.world.placement.AlfheimGroundModifier;
import velrondevs.masterfulbotany.common.world.tree.RandomFoliagePlacer;
import velrondevs.masterfulbotany.common.world.tree.ShatteredTrunkPlacer;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Registers the worldgen types that are not registered through the LibX registration system
 * (placement modifiers, trunk placers and foliage placers). Features are in {@link ModFeatures}.
 */
public class ModWorldGen {

    public static void register(RegisterEvent event) {
        MasterfulBotany mod = MasterfulBotany.getInstance();
        event.register(Registries.PLACEMENT_MODIFIER_TYPE, mod.resource("alfheim_ground"), () -> AlfheimGroundModifier.TYPE);
        event.register(Registries.TRUNK_PLACER_TYPE, mod.resource("shattered_trunk"), () -> ShatteredTrunkPlacer.TYPE);
        event.register(Registries.FOLIAGE_PLACER_TYPE, mod.resource("random_foliage"), () -> RandomFoliagePlacer.TYPE);
    }
}
