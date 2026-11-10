package velrondevs.masterfulbotany.gametest;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executor;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import velrondevs.masterfulbotany.common.world.alfheim.Alfheim;

@EventBusSubscriber(modid = "masterfulbotany")
public final class AlfheimLevelInstaller {

    public static final long TEST_SEED = 2645106112262271830L;

    public static String problem = null;

    private AlfheimLevelInstaller() {}

    @SubscribeEvent
    public static void serverAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        if (!(server instanceof GameTestServer)) return;
        try {
            Field settingsField = MinecraftServer.class.getDeclaredField("worldGenSettings");
            settingsField.setAccessible(true);
            WorldGenSettings old = (WorldGenSettings) settingsField.get(server);
            settingsField.set(server, new WorldGenSettings(new WorldOptions(TEST_SEED, true, false), old.dimensions()));
        } catch (ReflectiveOperationException e) {
            problem = e.toString();
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!(server instanceof GameTestServer)) return;
        if (server.getLevel(Alfheim.DIMENSION) != null) return;
        try {
            install(server);
        } catch (Throwable t) {
            problem = t.toString();
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void serverStopping(ServerStoppingEvent event) {
        if (!(event.getServer() instanceof GameTestServer)) return;
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
        Identifier file = Identifier.parse("masterfulbotany:dimension/alfheim.json");
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

        long seed = BiomeManager.obfuscateSeed(TEST_SEED);
        ServerLevel alfheim = new ServerLevel(server, (Executor) executorField.get(server), (LevelStorageSource.LevelStorageAccess) storageField.get(server),
                new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData()), Alfheim.DIMENSION, stem, false, seed,
                ImmutableList.of(), false);
        alfheim.getWorldBorder().setAbsoluteMaxSize(server.getAbsoluteMaxWorldSize());
        levels.put(Alfheim.DIMENSION, alfheim);
        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(alfheim));
    }
}
