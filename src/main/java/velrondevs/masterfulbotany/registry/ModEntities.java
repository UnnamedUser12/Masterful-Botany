package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.client.render.entity.AlfPixieRender;
import velrondevs.masterfulbotany.client.render.entity.RenderEntityMjoellnir;
import velrondevs.masterfulbotany.common.entity.AlfPixie;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ENTITY_TYPE")
public class ModEntities {

    public static final EntityType<AlfPixie> alfPixie = EntityType.Builder.<AlfPixie>of(AlfPixie::new, MobCategory.CREATURE).sized(1, 1).setUpdateInterval(3).setTrackingRange(16).setShouldReceiveVelocityUpdates(true).build(ResourceKey.create(Registries.ENTITY_TYPE, MasterfulBotany.getInstance().id("alf_pixie")));
    
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(alfPixie, AlfPixieRender::new);
        event.registerEntityRenderer(ModBlocks.mjoellnir.getEntityType(), velrondevs.masterfulbotany.client.render.entity.RenderEntityMjoellnir::new);
    }
    
    public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(alfPixie, AlfPixie.entityAttributes());
    }
    
    public static void createSpawnPlacement(RegisterSpawnPlacementsEvent event) {
        event.register(alfPixie, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AlfPixie::canSpawnAt, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
