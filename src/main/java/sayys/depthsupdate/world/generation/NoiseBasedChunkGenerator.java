package sayys.depthsupdate.world.generation;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import sayys.depthsupdate.DepthsUpdateConfig;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.world.density.Aquifer;
import sayys.depthsupdate.world.density.DensityFunction;
import sayys.depthsupdate.world.density.NoiseChunk;
import sayys.depthsupdate.world.density.NoiseRouter;
import sayys.depthsupdate.world.density.NoiseRouterData;
import sayys.depthsupdate.world.density.NoiseSettings;
import sayys.depthsupdate.world.noise.NormalNoise;
import sayys.depthsupdate.world.noise.PositionalRandomFactory;
import sayys.depthsupdate.world.noise.XoroshiroRandomSource;
import sayys.depthsupdate.world.surface.SurfaceRuleData;
import sayys.depthsupdate.world.surface.SurfaceRules;
import sayys.depthsupdate.world.surface.SurfaceSystem;

/**
 * Noise-based chunk generator using the Caves & Cliffs density function pipeline.
 * Replaces vanilla terrain generation with modern C&C world generation.
 *
 * This generator:
 * 1. Fills the chunk with blocks using density functions (NoiseChunk)
 * 2. Applies aquifer-based fluid placement
 * 3. Applies surface rules (grass, sand, deepslate, bedrock, etc.)
 */
public class NoiseBasedChunkGenerator {
    private static final Logger LOGGER = LogManager.getLogger("DepthsUpdate/NoiseGen");
    private static final IBlockState DEFAULT_BLOCK = Blocks.STONE.getDefaultState();
    private static final IBlockState DEFAULT_FLUID = Blocks.WATER.getDefaultState();
    private static final int SEA_LEVEL = 63;

    // Diagnostic: log details for first N chunks to diagnose surface height
    private static final int DIAGNOSTIC_CHUNK_COUNT = 10;
    private int diagnosticChunksLogged = 0;

    private final World world;
    private final long seed;
    private final NoiseSettings noiseSettings;
    private final NoiseRouter noiseRouter;
    private final PositionalRandomFactory randomFactory;
    private final PositionalRandomFactory aquiferRandomFactory;
    private final SurfaceSystem surfaceSystem;
    private final SurfaceRules.RuleSource surfaceRuleSource;

    // Noise instances for surface rules
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;
    private final NormalNoise clayBandsOffsetNoise;

    public NoiseBasedChunkGenerator(World world) {
        LOGGER.info("Initializing modern noise-based chunk generator (seed: {})", world.getSeed());
        this.world = world;
        this.seed = world.getSeed();

        XoroshiroRandomSource baseRandom = new XoroshiroRandomSource(this.seed);
        this.randomFactory = baseRandom.forkPositional();

        // Aquifer uses its own dedicated random factory (vanilla: randomFactory.fromHashOf("minecraft:aquifer").forkPositional())
        this.aquiferRandomFactory = this.randomFactory.fromHashOf("aquifer").forkPositional();

        // Use overworld noise settings
        this.noiseSettings = NoiseSettings.overworld();

        // Build the noise router (full density function tree)
        this.noiseRouter = NoiseRouterData.overworld(this.randomFactory,
                DepthsUpdateConfig.modernWorldGen.largeBiomes,
                DepthsUpdateConfig.modernWorldGen.amplified,
                DepthsUpdateConfig.modernWorldGen.enableCheeseCaves,
                DepthsUpdateConfig.modernWorldGen.enableSpaghettiCaves,
                DepthsUpdateConfig.modernWorldGen.enableNoodleCaves);

        // Create surface noises
        this.surfaceNoise = NormalNoise.create(this.randomFactory.fromHashOf("surface"),
                new NormalNoise.NoiseParameters(-6, 1.0, 1.0, 1.0));
        this.surfaceSecondaryNoise = NormalNoise.create(this.randomFactory.fromHashOf("surface_secondary"),
                new NormalNoise.NoiseParameters(-6, 1.0, 1.0, 1.0, 1.0));
        this.clayBandsOffsetNoise = NormalNoise.create(this.randomFactory.fromHashOf("clay_bands_offset"),
                new NormalNoise.NoiseParameters(-8, 1.0));

        // Create surface system
        this.surfaceSystem = new SurfaceSystem(
                DEFAULT_BLOCK, SEA_LEVEL,
                this.surfaceNoise, this.surfaceSecondaryNoise,
                this.clayBandsOffsetNoise,
                this.randomFactory);

        // Build surface rule tree
        NormalNoise gravelNoise = NormalNoise.create(this.randomFactory.fromHashOf("gravel"),
                new NormalNoise.NoiseParameters(-8, 1.0, 1.0, 1.0, 1.0));
        this.surfaceRuleSource = SurfaceRuleData.overworld(
                this.surfaceNoise, gravelNoise, this.randomFactory, this.surfaceSystem);
    }

    /**
     * Generates terrain for a chunk using the density function pipeline.
     *
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param primer the chunk primer to fill
     * @param biomes the biome array for the chunk (256 biomes, one per column)
     */
    public void generateTerrain(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
        try {
            generateTerrainInternal(chunkX, chunkZ, primer, biomes);
        } catch (Exception e) {
            LOGGER.error("Failed to generate terrain for chunk [{}, {}]", chunkX, chunkZ, e);
            // Fill with stone as fallback so the game doesn't crash
            int fallbackMinY = HeightManager.get(this.world).minY();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = fallbackMinY; y <= 0; y++) {
                        primer.setBlockState(x, y, z, DEFAULT_BLOCK);
                    }
                }
            }
        }
    }

    private void generateTerrainInternal(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
        int chunkMinBlockX = chunkX << 4;
        int chunkMinBlockZ = chunkZ << 4;

        int minY = this.noiseSettings.minY();
        int height = this.noiseSettings.height();
        int maxY = minY + height;

        // Clear vanilla terrain — our mixin runs after setBlocksInChunk which already
        // filled Y=0..~128 with vanilla terrain. We need a blank slate.
        IBlockState air = Blocks.AIR.getDefaultState();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    primer.setBlockState(x, y, z, air);
                }
            }
        }

        // Create a NoiseChunk for this chunk
        NoiseChunk noiseChunk = new NoiseChunk(chunkMinBlockX, chunkMinBlockZ,
                this.noiseSettings, this.noiseRouter);

        int cellWidth = noiseChunk.cellWidth();
        int cellHeight = noiseChunk.cellHeight();
        int cellCountXZ = Math.floorDiv(16, cellWidth);
        int cellCountY = Math.floorDiv(height, cellHeight);

        // Create aquifer
        Aquifer.FluidPicker globalFluidPicker = (x, y, z) -> {
            if (y < HeightManager.getLavaLevel(world)) {
                return new Aquifer.FluidStatus(HeightManager.getLavaLevel(world),
                        Blocks.LAVA.getDefaultState());
            }
            return new Aquifer.FluidStatus(SEA_LEVEL, DEFAULT_FLUID);
        };

        Aquifer aquifer;
        if (DepthsUpdateConfig.modernWorldGen.enableAquifers) {
            aquifer = Aquifer.create(noiseChunk, chunkX, chunkZ, this.noiseRouter,
                    this.aquiferRandomFactory, minY, height, globalFluidPicker);
        } else {
            // No aquifers: only lava below lava level, no water flooding caves
            int lavaLvl = HeightManager.getLavaLevel(world);
            Aquifer.FluidPicker noAquiferPicker = (x, y, z) -> {
                if (y < lavaLvl) {
                    return new Aquifer.FluidStatus(lavaLvl, Blocks.LAVA.getDefaultState());
                }
                return new Aquifer.FluidStatus(-1000, Blocks.AIR.getDefaultState());
            };
            aquifer = Aquifer.createDisabled(noAquiferPicker);
        }

        // Fill blocks using density function interpolation
        noiseChunk.initializeForFirstCellX();

        for (int cellX = 0; cellX < cellCountXZ; ++cellX) {
            noiseChunk.advanceCellX(cellX);

            for (int cellZ = 0; cellZ < cellCountXZ; ++cellZ) {
                for (int cellY = cellCountY - 1; cellY >= 0; --cellY) {
                    noiseChunk.selectCellYZ(cellY, cellZ);

                    for (int localY = cellHeight - 1; localY >= 0; --localY) {
                        int blockY = (minY + cellY * cellHeight) + localY;
                        double factorY = (double) localY / cellHeight;
                        noiseChunk.updateForY(blockY, factorY);

                        for (int localX = 0; localX < cellWidth; ++localX) {
                            int blockX = chunkMinBlockX + cellX * cellWidth + localX;
                            int localBlockX = blockX & 15;
                            double factorX = (double) localX / cellWidth;
                            noiseChunk.updateForX(blockX, factorX);

                            for (int localZ = 0; localZ < cellWidth; ++localZ) {
                                int blockZ = chunkMinBlockZ + cellZ * cellWidth + localZ;
                                int localBlockZ = blockZ & 15;
                                double factorZ = (double) localZ / cellWidth;
                                noiseChunk.updateForZ(blockZ, factorZ);

                                double density = noiseChunk.getInterpolatedDensity();

                                // Apply aquifer logic
                                IBlockState state = aquifer.computeSubstance(noiseChunk, density);
                                if (state == null) {
                                    state = DEFAULT_BLOCK; // solid
                                }

                                // Only set non-air blocks, or fluid blocks
                                if (state.getBlock() != Blocks.AIR) {
                                    primer.setBlockState(localBlockX, blockY, localBlockZ, state);
                                } else if (blockY <= SEA_LEVEL) {
                                    // Below sea level, fill empty space with water
                                    // (aquifer should have handled this, but as fallback)
                                }
                            }
                        }
                    }
                }
            }

            noiseChunk.swapSlices();
        }

        noiseChunk.stopInterpolation();

        // Post-process aquifer: fix floating water artifacts.
        // In vanilla, shouldScheduleFluidUpdate positions get post-processed by the
        // fluid system causing water to flow. We don't have that in 1.12.2, so instead
        // we remove water blocks that are clearly floating (air below, above sea level)
        // or isolated (surrounded by air on multiple sides).
        cleanupFloatingWater(primer, minY, maxY);

        // === Diagnostic logging for first N chunks ===
        if (diagnosticChunksLogged < DIAGNOSTIC_CHUNK_COUNT) {
            diagnosticChunksLogged++;
            int sampleX = chunkMinBlockX + 8;
            int sampleZ = chunkMinBlockZ + 8;
            DensityFunction.SinglePointContext sampleCtx =
                    new DensityFunction.SinglePointContext(sampleX, 0, sampleZ);

            double continents = this.noiseRouter.continents().compute(sampleCtx);
            double erosion = this.noiseRouter.erosion().compute(sampleCtx);
            double ridges = this.noiseRouter.ridges().compute(sampleCtx);
            double depth = this.noiseRouter.depth().compute(sampleCtx);
            int prelimSurface = noiseChunk.preliminarySurfaceLevel(sampleX, sampleZ);

            // Find actual highest solid block at sample column
            int actualSurface = minY;
            for (int y = maxY - 1; y >= minY; y--) {
                IBlockState state = primer.getBlockState(8, y, 8);
                if (state.getBlock() != Blocks.AIR && state.getBlock() != Blocks.WATER) {
                    actualSurface = y;
                    break;
                }
            }

            LOGGER.info("=== DIAG chunk [{}, {}] at ({},{}) ===", chunkX, chunkZ, sampleX, sampleZ);
            LOGGER.info("  continents={}  erosion={}  ridges={}  depth(Y=0)={}",
                    String.format("%.4f", continents), String.format("%.4f", erosion),
                    String.format("%.4f", ridges), String.format("%.4f", depth));
            LOGGER.info("  prelimSurface={}  actualSurface={}", prelimSurface, actualSurface);

            // Sample density at a few Y levels to see the density gradient
            for (int sampleY : new int[]{50, 55, 60, 63, 65, 70, 75, 80}) {
                DensityFunction.SinglePointContext yCtx =
                        new DensityFunction.SinglePointContext(sampleX, sampleY, sampleZ);
                double finalDensity = this.noiseRouter.finalDensity().compute(yCtx);
                IBlockState blockAt = primer.getBlockState(8, sampleY, 8);
                LOGGER.info("  Y={}: finalDensity={}  block={}", sampleY,
                        String.format("%.6f", finalDensity), blockAt.getBlock().getRegistryName());
            }
        }

        // Compute climate-mapped biomes for all 256 columns and cache them.
        // This fixes both surface rule application (sand/grass) and the biome array
        // stored on the chunk (F3 display, mob spawning).
        Biome[] climateBiomes = new Biome[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int blockX = chunkMinBlockX + x;
                int blockZ = chunkMinBlockZ + z;
                climateBiomes[x * 16 + z] = getClimateBiome(blockX, blockZ, primer,
                        chunkMinBlockX, chunkMinBlockZ, minY, maxY);
            }
        }

        // Write climate biomes back into the passed biomes array so the mixin
        // can apply them to the chunk's biome data
        if (biomes != null) {
            System.arraycopy(climateBiomes, 0, biomes, 0, Math.min(biomes.length, 256));
        }

        // Apply surface rules using the cached climate biomes
        this.surfaceSystem.buildSurface(primer, chunkX, chunkZ, noiseChunk,
                this.surfaceRuleSource,
                (blockX, blockZ) -> climateBiomes[((blockX - chunkMinBlockX) & 15) * 16 + ((blockZ - chunkMinBlockZ) & 15)],
                minY, maxY - 1);
    }

    /**
     * Removes floating water artifacts left by the aquifer system.
     * In vanilla, these positions get fluid post-processing that causes water to flow.
     * Since 1.12.2 doesn't have that system, we clean up the worst artifacts:
     * - Water with air directly below (floating water above sea level)
     * - Water blocks that are isolated (air on 4+ sides, not connected to a water body)
     */
    private void cleanupFloatingWater(ChunkPrimer primer, int minY, int maxY) {
        IBlockState air = Blocks.AIR.getDefaultState();

        // Pass 1: Remove water with air below (above sea level only — below sea level is ocean)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = SEA_LEVEL + 1; y < maxY; y++) {
                    IBlockState state = primer.getBlockState(x, y, z);
                    if (isWater(state)) {
                        IBlockState below = primer.getBlockState(x, y - 1, z);
                        if (below.getBlock() == Blocks.AIR) {
                            // Floating water — remove it
                            primer.setBlockState(x, y, z, air);
                        }
                    }
                }
            }
        }

        // Pass 2: Remove isolated water pockets above sea level
        // (water surrounded by air on 4+ of 6 sides, not part of a real water body)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = SEA_LEVEL + 1; y < maxY; y++) {
                    IBlockState state = primer.getBlockState(x, y, z);
                    if (isWater(state)) {
                        int airCount = 0;
                        // Check 6 neighbors (clamped to chunk bounds for X/Z)
                        if (y + 1 < maxY && primer.getBlockState(x, y + 1, z).getBlock() == Blocks.AIR) airCount++;
                        if (y - 1 >= minY && primer.getBlockState(x, y - 1, z).getBlock() == Blocks.AIR) airCount++;
                        if (x > 0 && primer.getBlockState(x - 1, y, z).getBlock() == Blocks.AIR) airCount++;
                        if (x < 15 && primer.getBlockState(x + 1, y, z).getBlock() == Blocks.AIR) airCount++;
                        if (z > 0 && primer.getBlockState(x, y, z - 1).getBlock() == Blocks.AIR) airCount++;
                        if (z < 15 && primer.getBlockState(x, y, z + 1).getBlock() == Blocks.AIR) airCount++;
                        if (airCount >= 4) {
                            primer.setBlockState(x, y, z, air);
                        }
                    }
                }
            }
        }

        // Pass 3: Below sea level — remove water that has air directly below
        // (prevents floating water sheets inside caves below sea level)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY + 1; y <= SEA_LEVEL; y++) {
                    IBlockState state = primer.getBlockState(x, y, z);
                    if (isWater(state)) {
                        IBlockState below = primer.getBlockState(x, y - 1, z);
                        if (below.getBlock() == Blocks.AIR) {
                            // Floating water inside underground cave — remove
                            primer.setBlockState(x, y, z, air);
                        }
                    }
                }
            }
        }
    }

    private static boolean isWater(IBlockState state) {
        return state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER;
    }

    /**
     * Uses C&C climate noise values to pick an appropriate 1.12.2 biome for surface rules.
     * This replaces vanilla's disconnected 2D biome placement with climate-coherent mapping.
     *
     * The mapping uses continentalness (ocean vs land), erosion (flat vs mountainous),
     * temperature, and humidity to select the closest 1.12.2 biome.
     */
    private Biome getClimateBiome(int blockX, int blockZ, ChunkPrimer primer,
                                   int chunkMinBlockX, int chunkMinBlockZ, int minY, int maxY) {
        DensityFunction.SinglePointContext ctx =
                new DensityFunction.SinglePointContext(blockX, 0, blockZ);

        double continentalness = this.noiseRouter.continents().compute(ctx);
        double erosion = this.noiseRouter.erosion().compute(ctx);
        double temperature = this.noiseRouter.temperature().compute(ctx);
        double humidity = this.noiseRouter.vegetation().compute(ctx);

        // Find actual surface height at this column
        int localX = (blockX - chunkMinBlockX) & 15;
        int localZ = (blockZ - chunkMinBlockZ) & 15;
        int surfaceY = SEA_LEVEL;
        for (int y = maxY - 1; y >= minY; y--) {
            IBlockState state = primer.getBlockState(localX, y, localZ);
            if (state.getBlock() != Blocks.AIR && state.getBlock() != Blocks.WATER) {
                surfaceY = y;
                break;
            }
        }

        // Deep ocean: very low continentalness (1.12.2 has no FROZEN_DEEP_OCEAN)
        if (continentalness < -0.45) {
            if (temperature < -0.45) return Biomes.FROZEN_OCEAN;
            return Biomes.DEEP_OCEAN;
        }

        // Ocean: low continentalness
        if (continentalness < -0.19) {
            if (temperature < -0.45) return Biomes.FROZEN_OCEAN;
            return Biomes.OCEAN;
        }

        // Beach: near-coast continentalness AND surface near sea level
        if (continentalness < -0.1 && surfaceY <= SEA_LEVEL + 3) {
            if (temperature < -0.45) return Biomes.COLD_BEACH;
            return Biomes.BEACH;
        }

        // Mushroom island (rare, skip for now)

        // Land biomes based on erosion + temperature + humidity
        boolean isCold = temperature < -0.45;
        boolean isTemperate = temperature >= -0.45 && temperature < 0.2;
        boolean isWarm = temperature >= 0.2 && temperature < 0.55;
        boolean isHot = temperature >= 0.55;

        boolean isWet = humidity > 0.3;
        boolean isDry = humidity < -0.35;

        // High erosion = flat terrain
        if (erosion > 0.55) {
            // Very flat terrain
            if (isHot && isDry) return Biomes.DESERT;
            if (isHot) return Biomes.SAVANNA;
            if (isCold) return Biomes.ICE_PLAINS;
            if (isWet) return Biomes.SWAMPLAND;
            return Biomes.PLAINS;
        }

        // Mountains: low erosion + inland
        if (erosion < -0.35 && continentalness > 0.3) {
            if (isCold) return Biomes.ICE_MOUNTAINS;
            if (isHot) return Biomes.SAVANNA_PLATEAU;
            return Biomes.EXTREME_HILLS;
        }

        // Hills: moderate-low erosion
        if (erosion < -0.1) {
            if (isHot && isDry) return Biomes.DESERT_HILLS;
            if (isCold) return Biomes.COLD_TAIGA_HILLS;
            if (isWet) return Biomes.JUNGLE_HILLS;
            return Biomes.FOREST_HILLS;
        }

        // Mid-erosion: normal terrain
        if (isHot && isDry) return Biomes.DESERT;
        if (isHot && isWet) return Biomes.JUNGLE;
        if (isHot) return Biomes.SAVANNA;
        if (isCold && isWet) return Biomes.COLD_TAIGA;
        if (isCold) return Biomes.TAIGA;
        if (isWarm && isWet) return Biomes.JUNGLE;
        if (isWarm && isDry) return Biomes.PLAINS;
        if (isTemperate && isWet) return Biomes.ROOFED_FOREST;
        if (isTemperate && isDry) return Biomes.PLAINS;
        if (isWet) return Biomes.FOREST;

        // Default
        return Biomes.PLAINS;
    }

    /**
     * Checks if the modern noise-based generation should be used.
     */
    public static boolean isEnabled() {
        return sayys.depthsupdate.DepthsUpdateConfig.modernWorldGen.enableModernWorldGen;
    }
}
