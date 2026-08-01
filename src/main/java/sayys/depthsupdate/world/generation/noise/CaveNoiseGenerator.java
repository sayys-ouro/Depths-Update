package sayys.depthsupdate.world.generation.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

public class CaveNoiseGenerator {
    /** Absolute upper Y for the underground cave types (cheese, spaghetti, pillars). */
    private static final int DEFAULT_CAVE_MAX_Y = 30;
    /**
     * Entrances run far higher so they can break the surface. Only stone is
     * carvable, so terrain, not this number, is what actually stops them; the
     * ceiling just bounds the sampling grid.
     */
    private static final int ENTRANCE_MAX_Y = 96;
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
    private final HeightContext ctx;
    private final PillarGenerator pillarGenerator;
    private final AquiferSampler aquifer;
    private final UndergroundRiverGenerator riverGen;
    private final int[][] riverSpans;

    public CaveNoiseGenerator(@NonNull World world) {
        long seed = world.getSeed();
        Random rand = new Random(seed);

        this.offsetX = rand.nextDouble() * 100000.0;
        this.offsetY = rand.nextDouble() * 100000.0;
        this.offsetZ = rand.nextDouble() * 100000.0;

        HeightContext ctx = HeightManager.get(world);
        this.ctx = ctx;
        this.caveMinY = ctx.minY() + DEFAULT_MIN_Y_OFFSET;

        // Underground cave types keep their own ceiling so raising the loop for
        // entrances cannot shift where their top slides begin.
        int undergroundMaxY = Math.min(DEFAULT_CAVE_MAX_Y, ctx.maxY() - 1);
        int entranceMaxY = Math.min(ENTRANCE_MAX_Y, ctx.maxY() - 1);

        this.caveMaxY = Math.max(undergroundMaxY, entranceMaxY);

        this.generators.add(new CheeseCaveGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, caveMinY, undergroundMaxY));
        this.generators.add(new SpaghettiCaveGenerator(seed, undergroundMaxY));
        this.generators.add(new CaveEntranceGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, caveMinY, entranceMaxY));

        this.pillarGenerator = new PillarGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, caveMinY, undergroundMaxY);

        this.aquifer = DepthsUpdateConfig.aquifers.enableAquifers
                ? new AquiferSampler(seed, ctx.lavaLevel(), ctx.seaLevel(), caveMinY, undergroundMaxY)
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

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;

        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState water = Blocks.WATER.getDefaultState();
        IBlockState lava = Blocks.LAVA.getDefaultState();
        IBlockState deepslate = BlockUtils.getDeepslateBlockState();

        CaveSampleContext context = new CaveSampleContext();

        boolean generatePillars = DepthsUpdateConfig.generateCavePillars;

        for (ICaveGenerator gen : this.generators) {
            if (gen.canGenerate()) {
                gen.prepare(chunkX, chunkZ);
            }
        }

        if (generatePillars) {
            this.pillarGenerator.prepare(chunkX, chunkZ);
        }

        if (this.aquifer != null) {
            this.aquifer.prepare(chunkX, chunkZ);
        }

        boolean guardWater = this.aquifer == null && chunkHasWater(primer);

        if (this.riverGen != null) {
            prepareRiverSpans(worldX, worldZ);
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double realX = worldX + x + this.offsetX;
                double realZ = worldZ + z + this.offsetZ;

                for (int y = caveMinY; y <= caveMaxY; y++) {
                    IBlockState currentState = primer.getBlockState(x, y, z);
                    boolean isCarvable = (currentState == stone || currentState == deepslate);

                    if (!isCarvable) continue;

                    double realY = y + this.offsetY;
                    context.reset(realX, realY, realZ, x, y, z);

                    for (ICaveGenerator gen : generators) {
                        if (gen.canGenerate()) {
                            gen.sample(context);

                            if (context.shouldCarve) break;
                        }
                    }

                    // Vanilla applies pillars as max(caves, pillars): a block a cave
                    // would carve stays solid where the pillar density is high,
                    // which is what forms the rock columns inside caverns.
                    if (context.shouldCarve && generatePillars && pillarGenerator.isPillar(x, y, z)) {
                        continue;
                    }

                    if (context.shouldCarve) {
                        // The river guard applies in both modes: the aquifer owns
                        // its own ponds but knows nothing about river water.
                        boolean nearRiver = this.riverGen != null && riverNear(x, y, z);

                        if (this.aquifer != null) {
                            if (!nearRiver) {
                                switch (this.aquifer.substanceAt(worldX + x, y, worldZ + z, context.density)) {
                                    case SOLID -> { }
                                    case AIR -> primer.setBlockState(x, y, z, air);
                                    case WATER -> primer.setBlockState(x, y, z, water);
                                    case LAVA -> primer.setBlockState(x, y, z, lava);
                                }
                            }
                        } else if (!nearRiver && (!guardWater || isSafeToCarve(primer, x, y, z))) {
                            if (y < this.ctx.lavaLevel()) {
                                primer.setBlockState(x, y, z, lava);
                            } else {
                                primer.setBlockState(x, y, z, air);
                            }
                        }
                    } else if (context.shouldDebug) {
                        primer.setBlockState(x, y, z, context.debugBlock);
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
    private boolean chunkHasWater(ChunkPrimer primer) {
        int lowY = Math.max(this.caveMinY - 1, this.ctx.minY());
        int highY = Math.min(this.caveMaxY + WATER_SUPPORT_DEPTH, this.ctx.maxY() - 1);

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
