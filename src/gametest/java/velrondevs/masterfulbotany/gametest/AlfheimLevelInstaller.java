package velrondevs.masterfulbotany.gametest;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executor;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import velrondevs.masterfulbotany.common.world.alfheim.Alfheim;

/**
 * The game test server only creates the dimensions of the world preset it uses (the three vanilla ones), a normal
 * world also creates the dimensions that datapacks add. The alfheim dimension json is part of this mod's datapack,
 * so this installer parses the real {@code dimension/alfheim.json} and creates the level like
 * {@code MinecraftServer#createLevels} does. It runs before the first tick, so the level is there for every test.
 */
@EventBusSubscriber(modid = "masterfulbotany")
public final class AlfheimLevelInstaller {

    /** Seed of the dev world used for manual testing (run/saves/masterful_test), alfheim is generated with it. */
    public static final long TEST_SEED = 2645106112262271830L;

    public static String problem = null;

    private AlfheimLevelInstaller() {}

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (server.getLevel(Alfheim.DIMENSION) != null) return;
        try {
            install(server);
        } catch (Throwable t) {
            problem = t.toString();
            t.printStackTrace();
        }
    }

    /**
     * The alfheim level only exists for the tests. Taking it out of the level map before the server stops avoids
     * waiting for (and saving) the chunks the tests left half generated.
     */
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void serverStopping(ServerStoppingEvent event) {
        try {
            Field levelsField = MinecraftServer.class.getDeclaredField("levels");
            levelsField.setAccessible(true);
            ((Map<ResourceKey<Level>, ServerLevel>) levelsField.get(event.getServer())).remove(Alfheim.DIMENSION);
            event.getServer().markWorldsDirty();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void install(MinecraftServer server) throws Exception {
        ResourceLocation file = ResourceLocation.parse("masterfulbotany:dimension/alfheim.json");
        JsonElement json;
        try (Reader reader = server.getResourceManager().getResourceOrThrow(file).openAsReader()) {
            json = JsonParser.parseReader(reader);
        }
        LevelStem stem = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, server.registryAccess()), json)
                .getOrThrow(msg -> new IllegalStateException("alfheim.json is not a valid dimension: " + msg));

        Field executorField = MinecraftServer.class.getDeclaredField("executor");
        executorField.setAccessible(true);
        Field storageField = MinecraftServer.class.getDeclaredField("storageSource");
        storageField.setAccessible(true);
        Field levelsField = MinecraftServer.class.getDeclaredField("levels");
        levelsField.setAccessible(true);
        Map<ResourceKey<Level>, ServerLevel> levels = (Map<ResourceKey<Level>, ServerLevel>) levelsField.get(server);

        // The game test world is created with structure generation turned off, a normal world has it on
        var worldData = server.getWorldData();
        Field optionsField = worldData.getClass().getDeclaredField("worldOptions");
        optionsField.setAccessible(true);
        WorldOptions old = (WorldOptions) optionsField.get(worldData);
        optionsField.set(worldData, new WorldOptions(TEST_SEED, true, old.generateBonusChest()));

        ServerLevel overworld = server.overworld();
        long seed = BiomeManager.obfuscateSeed(TEST_SEED);
        ChunkProgressListener quiet = new ChunkProgressListener() {
            @Override
            public void updateSpawnPos(ChunkPos center) {}

            @Override
            public void onStatusChange(ChunkPos pos, ChunkStatus status) {}

            @Override
            public void start() {}

            @Override
            public void stop() {}
        };
        ServerLevel alfheim = new ServerLevel(server, (Executor) executorField.get(server), (LevelStorageSource.LevelStorageAccess) storageField.get(server),
                new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData()), Alfheim.DIMENSION, stem, quiet, false, seed,
                ImmutableList.of(), false, overworld.getRandomSequences());
        overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(alfheim.getWorldBorder()));
        levels.put(Alfheim.DIMENSION, alfheim);
        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(alfheim));
    }
}
