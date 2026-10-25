package velrondevs.masterfulbotany;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.patchouli.PageRitualInfo;
import velrondevs.masterfulbotany.client.patchouli.PageRitualPattern;
import velrondevs.masterfulbotany.common.crafting.MjoellnirRuneOutput;
import velrondevs.masterfulbotany.common.crafting.RuneRitualRecipe;
import velrondevs.masterfulbotany.common.crafting.WanderingTraderRuneInput;
import velrondevs.masterfulbotany.common.pylon.PylonRepairables;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimPortalHandler;
import velrondevs.masterfulbotany.config.ClientConfig;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.datagen.MasterfulBotanyDatagen;
import velrondevs.masterfulbotany.event.EventListener;
import velrondevs.masterfulbotany.network.MasterfulNetwork;
import velrondevs.masterfulbotany.registry.ModCapabilities;
import velrondevs.masterfulbotany.registry.ModCreativeTabs;
import velrondevs.masterfulbotany.registry.ModCriteria;
import velrondevs.masterfulbotany.registry.ModDefaultComponents;
import velrondevs.masterfulbotany.registry.ModEntities;
import velrondevs.masterfulbotany.registry.ModLootModifiers;
import velrondevs.masterfulbotany.registry.ModWorldGen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.moddingx.libx.datapack.DynamicPacks;
import org.moddingx.libx.mod.ModXRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vazkii.patchouli.client.book.ClientBookRegistry;

@Mod("masterfulbotany")
public final class MasterfulBotany extends ModXRegistration {

    public static final Logger logger = LoggerFactory.getLogger("masterfulbotany");

    private static MasterfulBotany instance;
    private static MasterfulNetwork network;
    private static ModCreativeTabs creativeTab;

    public MasterfulBotany() {
        instance = this;
        network = new MasterfulNetwork(this);
        creativeTab = new ModCreativeTabs(this);

        IEventBus modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);

        modBus.addListener(ModLootModifiers::register);
        modBus.addListener(ModWorldGen::register);
        modBus.addListener(ModCriteria::register);
        modBus.addListener(ModEntities::createAttributes);
        modBus.addListener(ModEntities::createSpawnPlacement);
        modBus.addListener(ModDefaultComponents::modifyDefaultComponents);
        modBus.addListener(ModCapabilities::register);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(ModCapabilities::registerClient);
            modBus.addListener(ModEntities::registerRenderers);
            NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, AlfheimPortalHandler::renderGameOverlay);
        }

        NeoForge.EVENT_BUS.register(new EventListener());
        NeoForge.EVENT_BUS.addListener(AlfheimPortalHandler::serverStarted);
        NeoForge.EVENT_BUS.addListener(AlfheimPortalHandler::endTick);

        MasterfulBotanyDatagen.register(this);

        if (MasterfulConfig.addExtraRingSlot) {
            DynamicPacks.DATA_PACKS.enablePack(this.modid, "curios");
        }
    }

    @Nonnull
    public static MasterfulBotany getInstance() {
        return instance;
    }

    @Nonnull
    public static MasterfulNetwork getNetwork() {
        return network;
    }

    @Nonnull
    public static ModCreativeTabs getCreativeTab() {
        return creativeTab;
    }

    @Override
    protected void setup(final FMLCommonSetupEvent event) {
        logger.info("Loading Masterful Botany");

        event.enqueueWork(() -> {

            PylonRepairables.register(new PylonRepairables.ItemPylonRepairable(), PylonRepairables.PRIORITY_ITEM_WITH_INTERFACE);
            PylonRepairables.register(new PylonRepairables.MendingPylonRepairable(), PylonRepairables.PRIORITY_MENDING);

            RuneRitualRecipe.registerSpecialInput(WanderingTraderRuneInput.INSTANCE);
            RuneRitualRecipe.registerSpecialOutput(MjoellnirRuneOutput.INSTANCE);
        });
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientBookRegistry.INSTANCE.pageTypes.put(ResourceLocation.fromNamespaceAndPath(this.modid, "ritual_pattern"), PageRitualPattern.class);
            ClientBookRegistry.INSTANCE.pageTypes.put(ResourceLocation.fromNamespaceAndPath(this.modid, "ritual_info"), PageRitualInfo.class);
        });
    }
}
