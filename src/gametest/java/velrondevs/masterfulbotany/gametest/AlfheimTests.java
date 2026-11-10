package velrondevs.masterfulbotany.gametest;

import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
import velrondevs.botania.api.recipe.ElvenPortalUpdateEvent;
import velrondevs.botania.registry.BotaniaBlocks;
import velrondevs.botania.registry.BotaniaItems;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import velrondevs.masterfulbotany.common.helper.MasterfulPlayerData;
import velrondevs.masterfulbotany.common.world.alfheim.Alfheim;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimPortalHandler;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimTeleporter;
import velrondevs.masterfulbotany.registry.ModBlocks;

public class AlfheimTests {

    private static ServerLevel alfheim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(Alfheim.DIMENSION);
        TestUtil.check(helper, level != null || AlfheimLevelInstaller.problem == null, "the alfheim level could not be created: " + AlfheimLevelInstaller.problem);
        TestUtil.check(helper, level != null, "the alfheim level is not loaded, loaded levels: " + helper.getLevel().getServer().levelKeys());
        return level;
    }

    private static ResourceKey<Biome> biome(String path) {
        return ResourceKey.create(Registries.BIOME, Identifier.parse("masterfulbotany:" + path));
    }

    @GameTest(template = "empty")
    public static void alfheimLevelExists(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        TestUtil.check(helper, level.dimension().equals(Alfheim.DIMENSION), "wrong dimension key");
        TestUtil.check(helper, level.dimensionType().hasSkyLight(), "alfheim should have a sky");
        TestUtil.check(helper, !level.dimensionType().hasCeiling(), "alfheim has no ceiling");
        TestUtil.check(helper, level.getChunkSource().getGenerator() != null, "no chunk generator");
        TestUtil.check(helper, level.getSeaLevel() > 0, "unexpected sea level " + level.getSeaLevel());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void chunksGenerateWithTerrainAndBiomes(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        Set<Identifier> biomes = new HashSet<>();
        int solidColumns = 0;
        int columns = 0;
        for (int cx = -6; cx <= 6; cx += 3) {
            for (int cz = -6; cz <= 6; cz += 3) {
                ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.FULL, true);
                TestUtil.check(helper, chunk != null, "chunk " + cx + "," + cz + " was not generated");
                BlockPos pos = new ChunkPos(cx, cz).getMiddleBlockPosition(0);
                int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() & 15, pos.getZ() & 15);
                columns++;
                if (height > level.getMinY() + 5) solidColumns++;
                BlockPos top = new BlockPos(pos.getX(), height, pos.getZ());
                level.getBiome(top).unwrapKey().ifPresent(k -> biomes.add(k.identifier()));
            }
        }
        TestUtil.check(helper, solidColumns == columns, "some chunks have no terrain: " + solidColumns + "/" + columns);
        TestUtil.check(helper, !biomes.isEmpty(), "no biomes found");
        TestUtil.check(helper, biomes.stream().allMatch(b -> b.getNamespace().equals("masterfulbotany")), "alfheim must only contain its own biomes, found " + biomes);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void biomesAreRegistered(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for (String name : List.of("alfheim_hills", "alfheim_lakes", "alfheim_plains", "dreamwood_forest", "golden_fields")) {
            TestUtil.check(helper, registry.containsKey(biome(name).identifier()), "missing biome " + name);
        }
        helper.succeed();
    }

    private static void structureStartsGenerate(GameTestHelper helper, String name) {
        ServerLevel level = alfheim(helper);
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Holder.Reference<Structure> holder = structures.get(ResourceKey.create(Registries.STRUCTURE, Identifier.parse("masterfulbotany:" + name))).orElse(null);
        TestUtil.check(helper, holder != null, "structure " + name + " is not registered");
        var placements = level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder);
        TestUtil.check(helper, placements.size() == 1 && placements.get(0) instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement, "structure " + name + " has no random spread placement in alfheim");
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
            ChunkAccess chunk = level.getChunk(pos.x(), pos.z(), ChunkStatus.STRUCTURE_STARTS, true);
            BlockPos mid = pos.getMiddleBlockPosition(0);
            int surface = level.getChunkSource().getGenerator().getFirstOccupiedHeight(mid.getX(), mid.getZ(), Heightmap.Types.WORLD_SURFACE_WG, level, level.getChunkSource().randomState());
            Holder<Biome> biomeHere = level.getBiome(new BlockPos(mid.getX(), surface, mid.getZ()));
            boolean fits = holder.value().biomes().contains(biomeHere);
            var start = chunk.getStartForStructure(holder.value());
            if (fits) checked[0]++;
            if (start != null && start.isValid()) found[0]++;
            report.add(pos.x() + "," + pos.z() + ":" + biomeHere.unwrapKey().map(k -> k.identifier().getPath()).orElse("?") + (fits ? "(fits)" : "") + (start != null && start.isValid() ? "+" : "-"));
        });
        helper.succeedWhen(() -> {
            TestUtil.check(helper, index[0] >= candidates.size(), "still checking chunks");
            TestUtil.check(helper, checked[0] > 0, "no candidate chunk of " + name + " is in a fitting biome");
            TestUtil.check(helper, found[0] > 0, name + " never generated (candidates in a fitting biome: " + checked[0] + "/" + candidates.size() + ") " + report);
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

    private static Identifier configuredId(String name) {
        return name.equals("alfheim_grass") ? Identifier.parse("minecraft:grass_jungle") : Identifier.parse("masterfulbotany:" + name);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void featuresPlace(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        ChunkAccess chunk = level.getChunk(0, 0, ChunkStatus.FULL, true);
        BlockPos surface = new BlockPos(8, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8), 8);
        var configured = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
        var placed = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        List<String> names = List.of("abandoned_apothecaries", "alfheim_grass", "dragonstone_ore", "dreamwood_trees", "elementium_ore", "gold_ore", "mana_crystals", "motif_flowers", "wheat_fields");
        for (String name : names) {
            Identifier id = configuredId(name);
            ConfiguredFeature<?, ?> feature = configured.getValue(id);
            TestUtil.check(helper, feature != null, "configured feature " + name + " is missing");
        }
        int placedCount = 0;
        for (String name : names) {
            ConfiguredFeature<?, ?> feature = configured.getValue(configuredId(name));
            for (int i = 0; i < 6; i++) {
                if (feature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), surface.offset(i * 7, 0, i * 5))) placedCount++;
            }
        }
        for (String name : List.of("abandoned_apothecaries", "alfheim_grass", "dragonstone_ore", "dense_dreamwood_trees", "loose_dreamwood_trees", "elementium_ore", "gold_ore", "extra_gold_ore", "mana_crystals", "motif_flowers", "wheat_fields", "metamorphic_desert_stone", "metamorphic_forest_stone", "metamorphic_fungal_stone", "metamorphic_mesa_stone", "metamorphic_mountain_stone", "metamorphic_swamp_stone", "metamorphic_taiga_stone")) {
            TestUtil.check(helper, placed.containsKey(Identifier.parse("masterfulbotany:" + name)), "placed feature " + name + " is missing");
        }
        TestUtil.check(helper, placedCount > 0, "no feature was placed at all");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void worldFeaturesAreInBiomes(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        Set<Identifier> used = new HashSet<>();
        for (String name : List.of("alfheim_hills", "alfheim_lakes", "alfheim_plains", "dreamwood_forest", "golden_fields")) {
            Biome biome = biomes.getValue(biome(name));
            for (var step : biome.getGenerationSettings().features()) {
                for (Holder<PlacedFeature> feature : step) feature.unwrapKey().ifPresent(k -> used.add(k.identifier()));
            }
        }
        for (String expected : List.of("dreamwood_trees", "alfheim_grass", "mana_crystals", "gold_ore")) {
            boolean ok = used.stream().anyMatch(id -> id.getPath().contains(expected.replace("dreamwood_trees", "dreamwood")));
            TestUtil.check(helper, ok, "no biome uses a '" + expected + "' feature, used: " + used);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void returnPortalValidityInsideAlfheim(GameTestHelper helper) {
        ServerLevel level = alfheim(helper);
        ChunkAccess chunk = level.getChunk(40, 40, ChunkStatus.FULL, true);
        BlockPos center = new BlockPos(40 * 16 + 8, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8) + 10, 40 * 16 + 8);
        TestUtil.check(helper, !TileReturnPortal.validPortal(level, center), "air is not a valid portal");
        buildPortalFrame(level, center);
        TestUtil.check(helper, TileReturnPortal.validPortal(level, center), "the frame should be a valid portal in alfheim");
        TestUtil.check(helper, !TileReturnPortal.validPortal(helper.getLevel(), helper.absolutePos(TestUtil.CENTER)), "the portal is only valid inside alfheim");
        level.setBlock(center, ModBlocks.returnPortal.defaultBlockState(), 3);
        TestUtil.check(helper, level.getBlockEntity(center) instanceof TileReturnPortal, "no block entity for the portal");
        level.setBlock(center.north(), Blocks.AIR.defaultBlockState(), 3);
        TestUtil.check(helper, !TileReturnPortal.validPortal(level, center), "a broken frame is not valid");
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
        TestUtil.check(helper, level.getBlockState(center).is(ModBlocks.returnPortal), "an expiring pixie dust on the frame should create the return portal");
        TestUtil.check(helper, dust.isRemoved(), "the pixie dust should have been consumed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void teleportToAlfheimAndBack(GameTestHelper helper) {
        ServerLevel alfheim = alfheim(helper);
        ServerPlayer player = TestUtil.player(helper, false);
        BlockPos source = new BlockPos(20 * 16, 70, 20 * 16);
        alfheim.getChunk(20, 20, ChunkStatus.FULL, true);
        boolean ok = AlfheimTeleporter.teleportToAlfheim(player, source);
        TestUtil.check(helper, ok, "teleportToAlfheim reported a failure");
        TestUtil.check(helper, player.level().dimension().equals(Alfheim.DIMENSION), "the player is not in alfheim, but in " + player.level().dimension().identifier());
        BlockPos arrival = player.blockPosition();
        boolean portalFound = false;
        for (int y = -3; y <= 1 && !portalFound; y++) portalFound = player.level().getBlockState(arrival.offset(0, y, 0)).is(ModBlocks.returnPortal);
        TestUtil.check(helper, portalFound, "no return portal was created at " + arrival);
        AlfheimTeleporter.teleportToOverworld(player, arrival);
        TestUtil.check(helper, player.level().dimension().equals(Level.OVERWORLD), "the player did not return to the overworld, but is in " + player.level().dimension().identifier());
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
        TestUtil.check(helper, teleport, "standing in a portal never triggered the teleport");
        TestUtil.check(helper, ticks == 120, "the teleport should happen after 120 ticks, but took " + ticks);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void alfPortalTeleportsPlayersWithKvasirKnowledge(GameTestHelper helper) {
        alfheim(helper);
        BlockPos portalPos = TestUtil.CENTER.above(2);
        helper.setBlock(portalPos, BotaniaBlocks.alfPortal);
        var portal = helper.getBlockEntity(portalPos, BlockEntity.class);
        ServerPlayer knows = TestUtil.player(helper, false);
        MasterfulPlayerData.getData(knows).putBoolean("KvasirKnowledge", true);
        ServerPlayer ignorant = TestUtil.player(helper, false);
        AABB box = new AABB(helper.absolutePos(portalPos)).inflate(10);
        knows.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 3, 12.5)));
        ignorant.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 3, 12.5)));
        helper.onEachTick(() -> NeoForge.EVENT_BUS.post(new ElvenPortalUpdateEvent(portal, box, true, List.of())));
        helper.succeedWhen(() -> {
            TestUtil.check(helper, knows.level().dimension().equals(Alfheim.DIMENSION), "the player with kvasir knowledge should have been sent to alfheim");
            TestUtil.check(helper, ignorant.level().dimension().equals(Level.OVERWORLD), "a player without kvasir knowledge must stay in the overworld");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void playersWithoutKnowledgeAreBlindedInAlfheim(GameTestHelper helper) {
        ServerLevel alfheim = alfheim(helper);
        ServerPlayer player = TestUtil.player(helper, false);
        alfheim.getChunk(10, 10, ChunkStatus.FULL, true);
        player.teleportTo(alfheim, 168, 120, 168, java.util.Set.of(), 0, 0, true);
        TestUtil.check(helper, player.level().dimension().equals(Alfheim.DIMENSION), "the player is not in alfheim");
        player.tickCount = 1;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        TestUtil.check(helper, player.hasEffect(MobEffects.BLINDNESS), "players without knowledge should be blinded in alfheim");
        player.removeAllEffects();
        MasterfulPlayerData.getData(player).putBoolean("KvasirKnowledge", true);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        TestUtil.check(helper, !player.hasEffect(MobEffects.BLINDNESS), "players with knowledge must not be blinded");
        helper.succeed();
    }
}
