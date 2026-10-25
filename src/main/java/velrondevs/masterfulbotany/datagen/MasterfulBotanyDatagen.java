package velrondevs.masterfulbotany.datagen;

import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.datagen.providers.advancements.AdvancementProvider;
import velrondevs.masterfulbotany.datagen.providers.curios.CuriosSlotProvider;
import velrondevs.masterfulbotany.datagen.providers.lexicon.LexiconProvider;
import velrondevs.masterfulbotany.datagen.providers.loot.BlockLootProvider;
import velrondevs.masterfulbotany.datagen.providers.loot.ChestLootProvider;
import velrondevs.masterfulbotany.datagen.providers.loot.EntityAdditionLootProvider;
import velrondevs.masterfulbotany.datagen.providers.loot.EntityLootProvider;
import velrondevs.masterfulbotany.datagen.providers.loot.GlobalLootProvider;
import velrondevs.masterfulbotany.datagen.providers.models.BlockStateProvider;
import velrondevs.masterfulbotany.datagen.providers.models.ItemModelProvider;
import velrondevs.masterfulbotany.datagen.providers.recipes.RecipeProvider;
import velrondevs.masterfulbotany.datagen.providers.tags.BiomeLayerTagsProvider;
import velrondevs.masterfulbotany.datagen.providers.tags.BiomeTagsProvider;
import velrondevs.masterfulbotany.datagen.providers.tags.CommonTagsProvider;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimBiomeLayers;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimBiomes;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimDimension;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimDimensionTypes;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimFeatures;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimNoise;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimPlacements;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimStructureSets;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimStructures;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimSurface;
import velrondevs.masterfulbotany.datagen.providers.worldgen.AlfheimTemplates;
import net.minecraft.server.packs.PackType;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.datagen.PackTarget;

/**
 * Entry point of the data generation. The datagen runs through the LibX {@link DatagenSystem}, which hooks into
 * the {@code GatherDataEvent} of the mod event bus. Run it with {@code ./gradlew runData}, the output is written
 * to {@code src/generated/resources}.
 */
public class MasterfulBotanyDatagen {

    /**
     * Registers the datagen configuration. Called once from the mod constructor, nothing is generated
     * unless the game is started in data generation mode.
     */
    public static void register(MasterfulBotany mod) {
        DatagenSystem.create(mod, MasterfulBotanyDatagen::configure);
    }

    private static void configure(DatagenSystem system) {
        // Worldgen / Alfheim
        system.addRegistryProvider(AlfheimFeatures::new);
        system.addRegistryProvider(AlfheimPlacements::new);
        system.addRegistryProvider(AlfheimTemplates::new);
        system.addRegistryProvider(AlfheimStructures::new);
        system.addRegistryProvider(AlfheimStructureSets::new);
        system.addRegistryProvider(AlfheimBiomes::new);
        system.addRegistryProvider(AlfheimNoise::new);
        system.addRegistryProvider(AlfheimDimensionTypes::new);
        system.addRegistryProvider(AlfheimBiomeLayers::new);
        system.addExtensionProvider(AlfheimSurface::new);
        system.addExtensionProvider(AlfheimDimension::new);

        // Lexicon, advancements and recipes
        system.addDataProvider(LexiconProvider::new);
        system.addDataProvider(AdvancementProvider::new);
        system.addDataProvider(RecipeProvider::new);

        // Loot
        system.addRegistryProvider(BlockLootProvider::new);
        system.addRegistryProvider(EntityLootProvider::new);
        system.addRegistryProvider(ChestLootProvider::new);
        system.addRegistryProvider(EntityAdditionLootProvider::new);
        system.addDataProvider(GlobalLootProvider::new);

        // Tags
        system.addDataProvider(CommonTagsProvider::new);
        system.addDataProvider(BiomeTagsProvider::new);
        system.addDataProvider(BiomeLayerTagsProvider::new);

        // Models
        system.addDataProvider(BlockStateProvider::new);
        system.addDataProvider(ItemModelProvider::new);

        // Dynamic data pack that adds the extra ring slot
        PackTarget curiosTarget = system.dynamic("curios", PackType.SERVER_DATA);
        system.addDataProvider(curiosTarget, CuriosSlotProvider::new);
    }
}
