package sayys.depthsupdate.world.generation.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.GenerationLog;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

public class CaveNoiseGenerator {
    /**
     * Absolute ceiling for the features that are genuinely deep: spaghetti,
     * pillars and aquifers. Cheese and entrances are bounded by depth below the
     * terrain instead, which is what puts them inside hills and mountains.
     */
    private static final int DEEP_BAND_MAX_Y = 30;
    /** Highest Y the vanilla overworld generator writes terrain to. */
    private static final int TERRAIN_MAX_Y = 255;
    /** Default lower Y bound - offset from minY. Vanilla: minY(-64) + 4 = -60. */
    private static final int DEFAULT_MIN_Y_OFFSET = 4;
    /** Rock kept beneath standing water so river and pool beds read as solid ground. */
    private static final int WATER_SUPPORT_DEPTH = 3;

    private final List<ICaveGenerator> generators = new ArrayList<>();

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    private final int caveMinY;
    private final int caveMaxY;
    private final int deepBandMaxY;
    private final HeightContext ctx;
    private final PillarGenerator pillarGenerator;
    private final AquiferSampler aquifer;
    private final UndergroundRiverGenerator riverGen;
    private final int[][] riverSpans;
    private final int[] surfaceHeights = new int[256];
    private final Block deepslateBlock = BlockUtils.getDeepslateBlockState().getBlock();

    public CaveNoiseGenerator(@NonNull World world) {
        long seed = world.getSeed();
        Random rand = new Random(seed);

        this.offsetX = rand.nextDouble() * 100000.0;
        this.offsetY = rand.nextDouble() * 100000.0;
        this.offsetZ = rand.nextDouble() * 100000.0;

        HeightContext ctx = HeightManager.get(world);
        this.ctx = ctx;
        this.caveMinY = ctx.minY() + DEFAULT_MIN_Y_OFFSET;

        int deepBandMaxY = Math.min(DEEP_BAND_MAX_Y, ctx.maxY() - 1);
        int terrainMaxY = Math.min(TERRAIN_MAX_Y, ctx.maxY() - 1);

        this.deepBandMaxY = deepBandMaxY;
        this.caveMaxY = terrainMaxY;

        this.generators.add(new CheeseCaveGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, caveMinY, terrainMaxY));
        this.generators.add(new SpaghettiCaveGenerator(seed));
        this.generators.add(new NoodleCaveGenerator(seed, caveMinY, terrainMaxY));
        this.generators.add(new CaveEntranceGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, caveMinY, terrainMaxY));

        this.pillarGenerator = new PillarGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, caveMinY, deepBandMaxY);

        this.aquifer = DepthsUpdateConfig.aquifers.enableAquifers
                ? new AquiferSampler(seed, ctx.lavaLevel(), ctx.seaLevel(), caveMinY, deepBandMaxY)
                : null;

        this.riverGen = DepthsUpdateConfig.generateUndergroundRivers
                ? new UndergroundRiverGenerator(world)
                : null;
        this.riverSpans = this.riverGen != null ? new int[18 * 18][] : null;
    }

    /**
     * Water spans for every column of this chunk plus a one-block margin,
     * so border candidates can see rivers in neighbouring chunks.
     */
    private void prepareRiverSpans(int worldX, int worldZ) {
        for (int dx = -1; dx <= 16; dx++) {
            for (int dz = -1; dz <= 16; dz++) {
                this.riverSpans[(dx + 1) * 18 + (dz + 1)] = this.riverGen.waterSpan(worldX + dx, worldZ + dz);
            }
        }
    }

    private boolean riverNear(int x, int y, int z) {
        int lowY = y - 1;
        int highY = y + WATER_SUPPORT_DEPTH;

        for (int nx = x - 1; nx <= x + 1; nx++) {
            for (int nz = z - 1; nz <= z + 1; nz++) {
                int[] span = this.riverSpans[(nx + 1) * 18 + (nz + 1)];

                if (span != null && highY >= span[0] && lowY <= span[1]) {
                    return true;
                }
            }
        }

        return false;
    }

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;

        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState water = Blocks.WATER.getDefaultState();
        IBlockState lava = Blocks.LAVA.getDefaultState();

        CaveSampleContext context = new CaveSampleContext();

        boolean generatePillars = DepthsUpdateConfig.generateCavePillars;
        boolean logging = GenerationLog.enabled();

        int highestSurface = readSurfaceHeights(primer);

        for (ICaveGenerator gen : this.generators) {
            if (gen.canGenerate()) {
                gen.prepare(chunkX, chunkZ, highestSurface);
            }
        }

        if (generatePillars) {
            this.pillarGenerator.prepare(chunkX, chunkZ);
        }

        if (this.aquifer != null) {
            this.aquifer.prepare(chunkX, chunkZ);
        }

        // With aquifers on this still matters: carves above the aquifer band
        // fall through to the plain branch and must not breach primer water.
        boolean guardWater = chunkHasWater(primer, highestSurface);

        if (this.riverGen != null) {
            prepareRiverSpans(worldX, worldZ);
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double realX = worldX + x + this.offsetX;
                double realZ = worldZ + z + this.offsetZ;

                // replaceBiomeBlocks' transposed lookup and genTerrainBlocks'
                // swapped write cancel out: column (x, z) is painted with the
                // biome at plain index x + z * 16.
                Biome biome = biomes[x + z * 16];
                IBlockState topBlock = biome.topBlock;
                IBlockState fillerBlock = biome.fillerBlock;

                int surface = this.surfaceHeights[x << 4 | z];
                int columnMaxY = Math.min(this.caveMaxY, surface);

                for (int y = caveMinY; y <= columnMaxY; y++) {
                    IBlockState currentState = primer.getBlockState(x, y, z);

                    if (!isCarvable(currentState.getBlock())
                            && !(isLooseSurface(currentState.getBlock())
                                    && primer.getBlockState(x, y + 1, z).getMaterial() != Material.WATER)) {
                        continue;
                    }

                    double realY = y + this.offsetY;
                    context.reset(realX, realY, realZ, x, y, z, surface - y);

                    // Vanilla min-composes its cave functions: every generator
                    // contributes and the aquifer weighs against the true minimum.
                    for (ICaveGenerator gen : generators) {
                        if (gen.canGenerate()) {
                            gen.sample(context);
                        }
                    }

                    // Vanilla applies pillars as max(caves, pillars): a block a cave
                    // would carve stays solid where the pillar density is high,
                    // which is what forms the rock columns inside caverns.
                    if (context.shouldCarve() && generatePillars && pillarGenerator.isPillar(x, y, z)) {
                        if (logging) {
                            GenerationLog.pillarBlocked();
                        }

                        continue;
                    }

                    if (context.shouldCarve()) {
                        // The river guard applies in every mode: the aquifer owns
                        // its own ponds but knows nothing about river water.
                        boolean nearRiver = this.riverGen != null && riverNear(x, y, z);

                        if (nearRiver) {
                            continue;
                        }

                        // Above its band the aquifer has no cells and would answer
                        // air for everything, so ask it only where it has data.
                        if (this.aquifer != null && y <= this.deepBandMaxY) {
                            switch (this.aquifer.substanceAt(worldX + x, y, worldZ + z, context.density)) {
                                case SOLID -> { }
                                case AIR -> {
                                    primer.setBlockState(x, y, z, air);

                                    if (logging) {
                                        GenerationLog.carved(context.openMask);
                                    }
                                }
                                case WATER -> {
                                    primer.setBlockState(x, y, z, water);

                                    if (logging) {
                                        GenerationLog.aquiferWater();
                                    }
                                }
                                case LAVA -> {
                                    primer.setBlockState(x, y, z, lava);

                                    if (logging) {
                                        GenerationLog.aquiferLava();
                                    }
                                }
                            }
                        } else if (!guardWater || isSafeToCarve(primer, x, y, z)) {
                            if (y < this.ctx.lavaLevel()) {
                                primer.setBlockState(x, y, z, lava);
                            } else {
                                primer.setBlockState(x, y, z, air);

                                // Vanilla's digBlock: carving the painted surface
                                // pulls the top block down onto the exposed filler.
                                if (currentState == topBlock
                                        && primer.getBlockState(x, y - 1, z) == fillerBlock) {
                                    primer.setBlockState(x, y - 1, z, topBlock);
                                }
                            }

                            if (logging) {
                                GenerationLog.carved(context.openMask);
                            }
                        }
                    } else if (context.shouldDebug) {
                        primer.setBlockState(x, y, z, context.debugBlock);
                    }
                }
            }
        }

        ensureOpaqueGround(primer);

        if (logging) {
            GenerationLog.chunkGenerated();
        }
    }

    /** The blocks a cave may cut through; vanilla MapGenCaves' list plus deepslate. */
    private boolean isCarvable(Block block) {
        return block == Blocks.STONE
                || block == this.deepslateBlock
                || block == Blocks.DIRT
                || block == Blocks.GRASS
                || block == Blocks.HARDENED_CLAY
                || block == Blocks.STAINED_HARDENED_CLAY
                || block == Blocks.SANDSTONE
                || block == Blocks.RED_SANDSTONE
                || block == Blocks.MYCELIUM;
    }

    /** Carvable like vanilla only when not directly below water. */
    private static boolean isLooseSurface(Block block) {
        return block == Blocks.SAND || block == Blocks.GRAVEL;
    }

    /**
     * Top solid block of every column, the reference caves are placed against.
     * Runs after replaceBiomeBlocks, so the painted surface sits at the same Y
     * the bare stone did and the depth semantics are unchanged.
     *
     * Each column belongs to exactly one chunk and both sides of a border
     * measure their own columns the same way, so this cannot disagree at a seam.
     *
     * @return the tallest column in the chunk
     */
    private int readSurfaceHeights(ChunkPrimer primer) {
        int highest = this.caveMinY;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int surface = this.ctx.minY();

                for (int y = this.caveMaxY; y >= this.caveMinY; y--) {
                    Block block = primer.getBlockState(x, y, z).getBlock();

                    if (isCarvable(block) || isLooseSurface(block)) {
                        surface = y;
                        break;
                    }
                }

                this.surfaceHeights[x << 4 | z] = surface;

                if (surface > highest) {
                    highest = surface;
                }
            }
        }

        return highest;
    }

    /**
     * Vanilla decorators assume the heightmap stays in its historical range:
     * BiomePlains does nextInt(height + 32) and others nextInt(height * 2), so
     * a column whose every opaque block above y=0 was carved crashes them. The
     * composed noises make such columns measurably rare but not impossible;
     * this puts a stone plug where one slips through.
     */
    private void ensureOpaqueGround(ChunkPrimer primer) {
        IBlockState stone = Blocks.STONE.getDefaultState();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int surface = this.surfaceHeights[x << 4 | z];

                if (surface < 1) {
                    continue;
                }

                boolean opaque = false;

                for (int y = surface; y >= 1; y--) {
                    Material material = primer.getBlockState(x, y, z).getMaterial();

                    if (material != Material.AIR && material != Material.WATER && material != Material.LAVA) {
                        opaque = true;
                        break;
                    }
                }

                if (!opaque) {
                    primer.setBlockState(x, 1, z, stone);
                    primer.setBlockState(x, 2, z, stone);

                    if (GenerationLog.enabled()) {
                        GenerationLog.groundPlug();
                    }
                }
            }
        }
    }

    /**
     * A carve is refused next to primer water, and refused for
     * WATER_SUPPORT_DEPTH blocks beneath it. Rivers are handled separately by
     * riverNear, which also sees across chunk borders.
     */
    private boolean isSafeToCarve(ChunkPrimer primer, int x, int y, int z) {
        int lowY = Math.max(y - 1, this.ctx.minY());
        int highY = Math.min(y + WATER_SUPPORT_DEPTH, this.ctx.maxY() - 1);

        for (int nx = Math.max(x - 1, 0); nx <= Math.min(x + 1, 15); nx++) {
            for (int nz = Math.max(z - 1, 0); nz <= Math.min(z + 1, 15); nz++) {
                for (int ny = lowY; ny <= highY; ny++) {
                    if (isWater(primer.getBlockState(nx, ny, nz))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isWater(IBlockState state) {
        return state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER;
    }

    /**
     * The support scan is only worth running in chunks that actually hold
     * water, which is a small minority of them.
     */
    private boolean chunkHasWater(ChunkPrimer primer, int highestSurface) {
        int lowY = Math.max(this.caveMinY - 1, this.ctx.minY());
        int highY = Math.min(highestSurface + WATER_SUPPORT_DEPTH, this.ctx.maxY() - 1);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = lowY; y <= highY; y++) {
                    if (isWater(primer.getBlockState(x, y, z))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
