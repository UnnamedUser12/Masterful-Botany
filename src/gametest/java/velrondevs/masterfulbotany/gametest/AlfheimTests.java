package velrondevs.masterfulbotany.gametest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botania.api.recipe.ElvenPortalUpdateEvent;
import velrondevs.botania.registry.BotaniaBlocks;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import velrondevs.masterfulbotany.common.helper.MasterfulPlayerData;
import velrondevs.masterfulbotany.common.world.alfheim.Alfheim;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimPortalHandler;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimTeleporter;
import velrondevs.masterfulbotany.registry.ModBlocks;

@GameTestHolder("masterfulbotany")
@PrefixGameTestTemplate(false)
public class AlfheimTests {

    private static ServerLevel alfheim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(Alfheim.DIMENSION);
        helper.assertTrue(level != null || AlfheimLevelInstaller.problem == null, "the alfheim level could not be created: " + AlfheimLevelInstaller.problem);
        helper.assertTrue(level != null, "the alfheim level is not loaded, loaded levels: " + helper.getLevel().getServer().levelKeys());
        return level;
    }

    private static ResourceKey<Biome> biome(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.parse("masterfulbotany:" + path));
    }

    @GameTest(template = "empty")
    public static void alfheimLevelExists(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        helper.assertTrue(level.dimension().equals(Alfheim.DIMENSION), "wrong dimension key");
        helper.assertTrue(level.dimensionType().hasSkyLight(), "alfheim should have a sky");
        helper.assertTrue(!level.dimensionType().hasCeiling(), "alfheim has no ceiling");
        helper.assertTrue(level.getChunkSource().getGenerator() != null, "no chunk generator");
        helper.assertTrue(level.getSeaLevel() > 0, "unexpected sea level " + level.getSeaLevel());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void chunksGenerateWithTerrainAndBiomes(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        Set<ResourceLocation> biomes = new HashSet<>();
        int solidColumns = 0;
        int columns = 0;
        for (int cx = -6; cx <= 6; cx += 3) {
            for (int cz = -6; cz <= 6; cz += 3) {
                ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.FULL, true);
                helper.assertTrue(chunk != null, "chunk " + cx + "," + cz + " was not generated");
                BlockPos pos = new ChunkPos(cx, cz).getMiddleBlockPosition(0);
                int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() & 15, pos.getZ() & 15);
                columns++;
                if (height > level.getMinBuildHeight() + 5) solidColumns++;
                BlockPos top = new BlockPos(pos.getX(), height, pos.getZ());
                level.getBiome(top).unwrapKey().ifPresent(k -> biomes.add(k.location()));
            }
        }
        helper.assertTrue(solidColumns == columns, "some chunks have no terrain: " + solidColumns + "/" + columns);
        helper.assertTrue(!biomes.isEmpty(), "no biomes found");
        helper.assertTrue(biomes.stream().allMatch(b -> b.getNamespace().equals("masterfulbotany")), "alfheim must only contain its own biomes, found " + biomes);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void biomesAreRegistered(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        for (String name : List.of("alfheim_hills", "alfheim_lakes", "alfheim_plains", "dreamwood_forest", "golden_fields")) {
            helper.assertTrue(registry.containsKey(biome(name).location()), "missing biome " + name);
        }
        helper.succeed();
    }

    /**
     * Requests the structure start of every candidate chunk of the structure placement around the origin, one chunk
     * per tick (generation of many chunks in one tick would trigger the server watchdog), and checks that at least one
     * of the candidates in a fitting biome really got a structure start.
     */
    private static void structureStartsGenerate(GameTestHelper helper, String name) {
        ServerLevel level = alfheim(helper);
        var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Holder.Reference<Structure> holder = structures.getHolder(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("masterfulbotany:" + name))).orElse(null);
        helper.assertTrue(holder != null, "structure " + name + " is not registered");
        var placements = level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder);
        helper.assertTrue(placements.size() == 1 && placements.get(0) instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement, "structure " + name + " has no random spread placement in alfheim");
        var placement = (net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement) placements.get(0);
        List<ChunkPos> candidates = new java.util.ArrayList<>();
        for (int rx = -5; rx <= 5; rx++) for (int rz = -5; rz <= 5; rz++) candidates.add(placement.getPotentialStructureChunk(level.getSeed(), rx * placement.spacing(), rz * placement.spacing()));
        int[] index = {0};
        int[] checked = {0};
        int[] found = {0};
        List<String> report = new java.util.ArrayList<>();
        helper.onEachTick(() -> {
            if (index[0] >= candidates.size()) return;
            ChunkPos pos = candidates.get(index[0]++);
            ChunkAccess chunk = level.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS, true);
            BlockPos mid = pos.getMiddleBlockPosition(0);
            int surface = level.getChunkSource().getGenerator().getFirstOccupiedHeight(mid.getX(), mid.getZ(), Heightmap.Types.WORLD_SURFACE_WG, level, level.getChunkSource().randomState());
            Holder<Biome> biomeHere = level.getBiome(new BlockPos(mid.getX(), surface, mid.getZ()));
            boolean fits = holder.value().biomes().contains(biomeHere);
            var start = chunk.getStartForStructure(holder.value());
            if (fits) checked[0]++;
            if (start != null && start.isValid()) found[0]++;
            report.add(pos.x + "," + pos.z + ":" + biomeHere.unwrapKey().map(k -> k.location().getPath()).orElse("?") + (fits ? "(fits)" : "") + (start != null && start.isValid() ? "+" : "-"));
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(index[0] >= candidates.size(), "still checking chunks");
            helper.assertTrue(checked[0] > 0, "no candidate chunk of " + name + " is in a fitting biome");
            helper.assertTrue(found[0] > 0, name + " never generated (candidates in a fitting biome: " + checked[0] + "/" + candidates.size() + ") " + report);
        });
    }

    @GameTest(template = "empty", timeoutTicks = 4000)
    public static void elvenHousesGenerate(GameTestHelper helper) {
        structureStartsGenerate(helper, "elven_house");
    }

    @GameTest(template = "empty", timeoutTicks = 4000)
    public static void andwariCavesGenerate(GameTestHelper helper) {
        structureStartsGenerate(helper, "andwari_cave");
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void featuresPlace(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        ChunkAccess chunk = level.getChunk(0, 0, ChunkStatus.FULL, true);
        BlockPos surface = new BlockPos(8, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8), 8);
        var configured = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        var placed = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        List<String> names = List.of("abandoned_apothecaries", "alfheim_grass", "dragonstone_ore", "dreamwood_trees", "elementium_ore", "gold_ore", "mana_crystals", "motif_flowers", "wheat_fields");
        for (String name : names) {
            ResourceLocation id = ResourceLocation.parse("masterfulbotany:" + name);
            ConfiguredFeature<?, ?> feature = configured.get(id);
            helper.assertTrue(feature != null, "configured feature " + name + " is missing");
        }
        // Every configured feature must be able to generate without throwing (the placement modifiers are checked by the biome tests)
        int placedCount = 0;
        for (String name : names) {
            ConfiguredFeature<?, ?> feature = configured.get(ResourceLocation.parse("masterfulbotany:" + name));
            for (int i = 0; i < 6; i++) {
                if (feature.place(level, level.getChunkSource().getGenerator(), level.random, surface.offset(i * 7, 0, i * 5))) placedCount++;
            }
        }
        for (String name : List.of("abandoned_apothecaries", "alfheim_grass", "dragonstone_ore", "dense_dreamwood_trees", "loose_dreamwood_trees", "elementium_ore", "gold_ore", "extra_gold_ore", "mana_crystals", "motif_flowers", "wheat_fields", "metamorphic_desert_stone", "metamorphic_forest_stone", "metamorphic_fungal_stone", "metamorphic_mesa_stone", "metamorphic_mountain_stone", "metamorphic_swamp_stone", "metamorphic_taiga_stone")) {
            helper.assertTrue(placed.containsKey(ResourceLocation.parse("masterfulbotany:" + name)), "placed feature " + name + " is missing");
        }
        helper.assertTrue(placedCount > 0, "no feature was placed at all");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void worldFeaturesAreInBiomes(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        var biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Set<ResourceLocation> used = new HashSet<>();
        for (String name : List.of("alfheim_hills", "alfheim_lakes", "alfheim_plains", "dreamwood_forest", "golden_fields")) {
            Biome biome = biomes.get(biome(name));
            for (var step : biome.getGenerationSettings().features()) {
                for (Holder<PlacedFeature> feature : step) feature.unwrapKey().ifPresent(k -> used.add(k.location()));
            }
        }
        for (String expected : List.of("dreamwood_trees", "alfheim_grass", "mana_crystals", "gold_ore")) {
            boolean ok = used.stream().anyMatch(id -> id.getPath().contains(expected.replace("dreamwood_trees", "dreamwood")));
            helper.assertTrue(ok, "no biome uses a '" + expected + "' feature, used: " + used);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void returnPortalValidityInsideAlfheim(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        ChunkAccess chunk = level.getChunk(40, 40, ChunkStatus.FULL, true);
        BlockPos center = new BlockPos(40 * 16 + 8, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8) + 10, 40 * 16 + 8);
        helper.assertTrue(!TileReturnPortal.validPortal(level, center), "air is not a valid portal");
        buildPortalFrame(level, center);
        helper.assertTrue(TileReturnPortal.validPortal(level, center), "the frame should be a valid portal in alfheim");
        helper.assertTrue(!TileReturnPortal.validPortal(helper.getLevel(), helper.absolutePos(TestUtil.CENTER)), "the portal is only valid inside alfheim");
        level.setBlock(center, ModBlocks.returnPortal.defaultBlockState(), 3);
        helper.assertTrue(level.getBlockEntity(center) instanceof TileReturnPortal, "no block entity for the portal");
        level.setBlock(center.north(), Blocks.AIR.defaultBlockState(), 3);
        helper.assertTrue(!TileReturnPortal.validPortal(level, center), "a broken frame is not valid");
        helper.succeed();
    }

    private static void buildPortalFrame(ServerLevel level, BlockPos center) {
        level.setBlock(center.north(), BotaniaBlocks.livingwoodGlimmering.defaultBlockState(), 3);
        level.setBlock(center.south(), BotaniaBlocks.livingwoodGlimmering.defaultBlockState(), 3);
        level.setBlock(center.east(), BotaniaBlocks.livingwoodGlimmering.defaultBlockState(), 3);
        level.setBlock(center.west(), BotaniaBlocks.livingwoodGlimmering.defaultBlockState(), 3);
        level.setBlock(center.offset(-1, 0, -1), BotaniaBlocks.livingwood.defaultBlockState(), 3);
        level.setBlock(center.offset(-1, 0, 1), BotaniaBlocks.livingwood.defaultBlockState(), 3);
        level.setBlock(center.offset(1, 0, -1), BotaniaBlocks.livingwood.defaultBlockState(), 3);
        level.setBlock(center.offset(1, 0, 1), BotaniaBlocks.livingwood.defaultBlockState(), 3);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void pixieDustCreatesReturnPortal(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        ChunkAccess chunk = level.getChunk(44, 44, ChunkStatus.FULL, true);
        BlockPos center = new BlockPos(44 * 16 + 8, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8) + 10, 44 * 16 + 8);
        buildPortalFrame(level, center);
        ItemEntity dust = new ItemEntity(level, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, new ItemStack(BotaniaItems.pixieDust));
        level.addFreshEntity(dust);
        NeoForge.EVENT_BUS.post(new ItemExpireEvent(dust));
        helper.assertTrue(level.getBlockState(center).is(ModBlocks.returnPortal), "an expiring pixie dust on the frame should create the return portal");
        helper.assertTrue(dust.isRemoved(), "the pixie dust should have been consumed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void teleportToAlfheimAndBack(GameTestHelper helper) {
        ServerLevel alfheim = alfheim(helper);
        ServerPlayer player = TestUtil.player(helper, false);
        BlockPos source = new BlockPos(20 * 16, 70, 20 * 16);
        alfheim.getChunk(20, 20, ChunkStatus.FULL, true);
        boolean ok = AlfheimTeleporter.teleportToAlfheim(player, source);
        helper.assertTrue(ok, "teleportToAlfheim reported a failure");
        helper.assertTrue(player.level().dimension().equals(Alfheim.DIMENSION), "the player is not in alfheim, but in " + player.level().dimension().location());
        BlockPos arrival = player.blockPosition();
        // The teleporter builds a return portal below the arrival position
        boolean portalFound = false;
        for (int y = -3; y <= 1 && !portalFound; y++) portalFound = player.level().getBlockState(arrival.offset(0, y, 0)).is(ModBlocks.returnPortal);
        helper.assertTrue(portalFound, "no return portal was created at " + arrival);
        AlfheimTeleporter.teleportToOverworld(player, arrival);
        helper.assertTrue(player.level().dimension().equals(Level.OVERWORLD), "the player did not return to the overworld, but is in " + player.level().dimension().location());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void portalTimerTeleportsAfterSixSeconds(GameTestHelper helper) {
        ServerPlayer player = TestUtil.player(helper, false);
        boolean teleport = false;
        int ticks = 0;
        while (!teleport && ticks < 200) {
            teleport = AlfheimPortalHandler.setInPortal(helper.getLevel(), player);
            ticks++;
        }
        helper.assertTrue(teleport, "standing in a portal never triggered the teleport");
        helper.assertTrue(ticks == 120, "the teleport should happen after 120 ticks, but took " + ticks);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void alfPortalTeleportsPlayersWithKvasirKnowledge(GameTestHelper helper) {
        alfheim(helper);
        BlockPos portalPos = TestUtil.CENTER.above(2);
        helper.setBlock(portalPos, BotaniaBlocks.alfPortal);
        var portal = helper.getBlockEntity(portalPos);
        ServerPlayer knows = TestUtil.player(helper, false);
        MasterfulPlayerData.getData(knows).putBoolean("KvasirKnowledge", true);
        ServerPlayer ignorant = TestUtil.player(helper, false);
        AABB box = new AABB(helper.absolutePos(portalPos)).inflate(10);
        knows.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 3, 12.5)));
        ignorant.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 3, 12.5)));
        // The portal block entity fires the update event every tick
        helper.onEachTick(() -> NeoForge.EVENT_BUS.post(new ElvenPortalUpdateEvent(portal, box, true, List.of())));
        helper.succeedWhen(() -> {
            helper.assertTrue(knows.level().dimension().equals(Alfheim.DIMENSION), "the player with kvasir knowledge should have been sent to alfheim");
            helper.assertTrue(ignorant.level().dimension().equals(Level.OVERWORLD), "a player without kvasir knowledge must stay in the overworld");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void playersWithoutKnowledgeAreBlindedInAlfheim(GameTestHelper helper) {
        ServerLevel alfheim = alfheim(helper);
        ServerPlayer player = TestUtil.player(helper, false);
        alfheim.getChunk(10, 10, ChunkStatus.FULL, true);
        player.teleportTo(alfheim, 168, 120, 168, 0, 0);
        helper.assertTrue(player.level().dimension().equals(Alfheim.DIMENSION), "the player is not in alfheim");
        player.tickCount = 1;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.hasEffect(MobEffects.BLINDNESS), "players without knowledge should be blinded in alfheim");
        player.removeAllEffects();
        MasterfulPlayerData.getData(player).putBoolean("KvasirKnowledge", true);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertTrue(!player.hasEffect(MobEffects.BLINDNESS), "players with knowledge must not be blinded");
        helper.succeed();
    }
}
