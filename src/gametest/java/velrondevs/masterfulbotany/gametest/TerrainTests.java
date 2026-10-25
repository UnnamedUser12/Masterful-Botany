package velrondevs.masterfulbotany.gametest;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.masterfulbotany.common.world.alfheim.Alfheim;

/**
 * Checks what the generated alfheim terrain really looks like: the blocks on the surface and the biomes.
 */
@GameTestHolder("masterfulbotany")
@PrefixGameTestTemplate(false)
public class TerrainTests {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private static void survey(GameTestHelper helper, int centerX, int centerZ, String name) {
        ServerLevel level = helper.getLevel().getServer().getLevel(Alfheim.DIMENSION);
        helper.assertTrue(level != null, "alfheim is not loaded");
        List<ChunkPos> chunks = new ArrayList<>();
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) chunks.add(new ChunkPos(centerX + x, centerZ + z));
        Map<String, Integer> surface = new TreeMap<>();
        Map<String, Integer> top = new TreeMap<>();
        Map<String, Integer> biomes = new TreeMap<>();
        int[] index = {0};
        int[] columns = {0};
        int[] natural = {0};
        int[] minY = {Integer.MAX_VALUE};
        int[] maxY = {Integer.MIN_VALUE};
        helper.onEachTick(() -> {
            if (index[0] >= chunks.size()) return;
            ChunkPos pos = chunks.get(index[0]++);
            ChunkAccess chunk = level.getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // The first non air block under the world surface heightmap (trees and flowers included) ...
                    int worldSurface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    BlockPos topPos = new BlockPos(pos.getMinBlockX() + x, worldSurface, pos.getMinBlockZ() + z);
                    top.merge(BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(topPos).getBlock()).getPath(), 1, Integer::sum);
                    // ... and the first block that stops motion (terrain, trees, no flowers and no water)
                    int motion = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                    BlockPos groundPos = new BlockPos(pos.getMinBlockX() + x, motion, pos.getMinBlockZ() + z);
                    BlockState ground = chunk.getBlockState(groundPos);
                    surface.merge(BuiltInRegistries.BLOCK.getKey(ground.getBlock()).getPath(), 1, Integer::sum);
                    if (ground.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || ground.is(net.minecraft.world.level.block.Blocks.DIRT)
                            || ground.is(net.minecraft.tags.BlockTags.LEAVES) || ground.is(net.minecraft.tags.BlockTags.LOGS)) natural[0]++;
                    columns[0]++;
                    minY[0] = Math.min(minY[0], motion);
                    maxY[0] = Math.max(maxY[0], motion);
                    biomes.merge(level.getBiome(groundPos).unwrapKey().map(k -> k.location().getPath()).orElse("?"), 1, Integer::sum);
                }
            }
        });
        boolean[] logged = {false};
        helper.succeedWhen(() -> {
            helper.assertTrue(index[0] >= chunks.size(), "still generating");
            helper.assertTrue(surface.getOrDefault("livingrock", 0) < columns[0] / 2, "more than half of the columns at " + name + " are bare livingrock: " + surface);
            if (!logged[0]) {
                logged[0] = true;
            LOGGER.info("ALFHEIM TERRAIN [{}]: {} columns, ground height {}..{}", name, columns[0], minY[0], maxY[0]);
            LOGGER.info("ALFHEIM TERRAIN [{}]: ground blocks (ocean floor heightmap) {}", name, surface);
            LOGGER.info("ALFHEIM TERRAIN [{}]: topmost blocks (world surface heightmap) {}", name, top);
            LOGGER.info("ALFHEIM TERRAIN [{}]: biomes {}", name, biomes);
            }
        });
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void terrainHeightsAreReasonable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(Alfheim.DIMENSION);
        helper.assertTrue(level != null, "alfheim is not loaded");
        var generator = level.getChunkSource().getGenerator();
        var random = level.getChunkSource().randomState();
        int[] histogram = new int[8];
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int count = 0;
        Map<String, Integer> biomes = new TreeMap<>();
        for (int x = -1500; x <= 1500; x += 24) {
            for (int z = -1500; z <= 1500; z += 24) {
                int h = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                max = Math.max(max, h);
                min = Math.min(min, h);
                histogram[Math.max(0, Math.min(7, (h - 30) / 20))]++;
                count++;
                biomes.merge(generator.getBiomeSource().getNoiseBiome(x >> 2, h >> 2, z >> 2, random.sampler()).unwrapKey().map(k -> k.location().getPath()).orElse("?"), 1, Integer::sum);
            }
        }
        LOGGER.info("ALFHEIM HEIGHTS: {} samples, min {} max {}, histogram by 20 blocks from 30: {}, biomes {}", count, min, max, java.util.Arrays.toString(histogram), biomes);
        helper.assertTrue(max < 320, "alfheim has unexpectedly high terrain: " + max);
        helper.assertTrue(biomes.size() >= 4, "expected at least 4 different biomes, found " + biomes);
        helper.succeed();
    }


    /**
     * The land of alfheim (everything above the sea) must be covered with grass, dirt, trees and the like. Only steep
     * slopes may show the bare default block (livingrock). This guards the surface rules (libx surface rule set).
     */
    @GameTest(template = "empty", timeoutTicks = 3000)
    public static void landIsCoveredWithGrassAndDirt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(Alfheim.DIMENSION);
        helper.assertTrue(level != null, "alfheim is not loaded");
        List<ChunkPos> chunks = new ArrayList<>();
        int[][] centers = {{0, 0}, {96, 96}, {-96, 96}, {96, -96}, {-96, -96}, {200, 0}, {0, 200}, {-200, 0}, {0, -200}};
        for (int[] c : centers) for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) chunks.add(new ChunkPos(c[0] + x, c[1] + z));
        Map<String, Integer> land = new TreeMap<>();
        int[] index = {0};
        int[] landColumns = {0};
        int[] covered = {0};
        int[] bare = {0};
        helper.onEachTick(() -> {
            if (index[0] >= chunks.size()) return;
            ChunkPos pos = chunks.get(index[0]++);
            ChunkAccess chunk = level.getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z); // ChunkAccess#getHeight is already the y of the top block
                    BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos(pos.getMinBlockX() + x, y, pos.getMinBlockZ() + z);
                    BlockState state = chunk.getBlockState(scan);
                    // walk down through flowers, grass, water and the like to the ground
                    while (scan.getY() > level.getMinBuildHeight() && !state.blocksMotion()) {
                        scan.move(0, -1, 0);
                        state = chunk.getBlockState(scan);
                    }
                    if (chunk.getBlockState(new BlockPos(pos.getMinBlockX() + x, y, pos.getMinBlockZ() + z)).getFluidState().isEmpty() == false) continue; // under water
                    if (scan.getY() < level.getSeaLevel()) continue;
                    landColumns[0]++;
                    land.merge(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath(), 1, Integer::sum);
                    if (state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || state.is(net.minecraft.world.level.block.Blocks.DIRT)
                            || state.is(net.minecraft.world.level.block.Blocks.SAND) || state.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                            || state.is(velrondevs.botania.registry.BotaniaBlocks.goldenGrass)
                            || state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.LOGS)) covered[0]++;
                    if (state.is(velrondevs.botania.registry.BotaniaBlocks.livingrock)) bare[0]++;
                }
            }
        });
        boolean[] logged = {false};
        helper.succeedWhen(() -> {
            helper.assertTrue(index[0] >= chunks.size(), "still generating");
            if (!logged[0]) {
                logged[0] = true;
                LOGGER.info("ALFHEIM LAND: {} land columns, covered {} bare livingrock {}, blocks {}", landColumns[0], covered[0], bare[0], land);
            }
            helper.assertTrue(landColumns[0] > 2000, "too little land to judge: " + landColumns[0]);
            double share = covered[0] / (double) landColumns[0];
            helper.assertTrue(share >= 0.6, "only " + Math.round(share * 100) + "% of the land has grass, dirt, sand, gravel or trees on top: " + land);
            helper.assertTrue(land.getOrDefault("grass_block", 0) > 0, "no grass block on the land at all: " + land);
        });
    }


    /**
     * Mountains above y=150 are rare, steep slopes keep the bare default block by design (the surface rule does not
     * cover steep terrain). This only logs what they look like.
     */
    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void mountainSurfaceIsLogged(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(Alfheim.DIMENSION);
        helper.assertTrue(level != null, "alfheim is not loaded");
        var generator = level.getChunkSource().getGenerator();
        var random = level.getChunkSource().randomState();
        List<ChunkPos> chunks = new ArrayList<>();
        for (int x = -1500; x <= 1500 && chunks.size() < 12; x += 48) {
            for (int z = -1500; z <= 1500 && chunks.size() < 12; z += 48) {
                if (generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, random) >= 150) chunks.add(new ChunkPos(x >> 4, z >> 4));
            }
        }
        helper.assertTrue(!chunks.isEmpty(), "no mountain found");
        Map<String, Integer> top = new TreeMap<>();
        int[] index = {0};
        int[] max = {0};
        helper.onEachTick(() -> {
            if (index[0] >= chunks.size()) return;
            ChunkPos pos = chunks.get(index[0]++);
            ChunkAccess chunk = level.getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
                int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                if (y < 150) continue;
                max[0] = Math.max(max[0], y);
                top.merge(BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(new BlockPos(pos.getMinBlockX() + x, y, pos.getMinBlockZ() + z)).getBlock()).getPath(), 1, Integer::sum);
            }
        });
        boolean[] logged = {false};
        helper.succeedWhen(() -> {
            helper.assertTrue(index[0] >= chunks.size(), "still generating");
            if (!logged[0]) {
                logged[0] = true;
                LOGGER.info("ALFHEIM MOUNTAINS: {} chunks, highest {}, top blocks above y=150: {}", chunks.size(), max[0], top);
            }
        });
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void surfaceAroundOrigin(GameTestHelper helper) {
        survey(helper, 0, 0, "0,0");
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void surfaceAround96(GameTestHelper helper) {
        survey(helper, 96, 96, "96,96");
    }
}
