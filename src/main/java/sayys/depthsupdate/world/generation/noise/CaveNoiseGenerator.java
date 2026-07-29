package sayys.depthsupdate.world.generation.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;

public class CaveNoiseGenerator {
    /** Default upper Y bound for cave generation (relative to sea level). */
    private static final int DEFAULT_CAVE_MAX_Y = 30;
    /** Default lower Y bound — offset from minY. Vanilla: minY(-64) + 4 = -60. */
    private static final int DEFAULT_MIN_Y_OFFSET = 4;

    private final List<ICaveGenerator> generators = new ArrayList<>();

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    private final int caveMinY;
    private final int caveMaxY;
    private final HeightContext ctx;

    public CaveNoiseGenerator(@NonNull World world) {
        long seed = world.getSeed();
        Random rand = new Random(seed);

        this.offsetX = rand.nextDouble() * 100000.0;
        this.offsetY = rand.nextDouble() * 100000.0;
        this.offsetZ = rand.nextDouble() * 100000.0;

        HeightContext ctx = HeightManager.get(world);
        this.ctx = ctx;
        this.caveMinY = ctx.minY() + DEFAULT_MIN_Y_OFFSET;
        this.caveMaxY = Math.min(DEFAULT_CAVE_MAX_Y, ctx.maxY() - 1);

        this.generators.add(new CheeseCaveGenerator(seed, caveMinY, caveMaxY));
        this.generators.add(new SpaghettiCaveGenerator(seed, caveMaxY));
    }

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;

        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState lava = Blocks.LAVA.getDefaultState();
        IBlockState deepslate = BlockUtils.getDeepslateBlockState();

        CaveSampleContext context = new CaveSampleContext();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double realX = worldX + x + this.offsetX;
                double realZ = worldZ + z + this.offsetZ;

                for (int y = caveMinY; y <= caveMaxY; y++) {
                    IBlockState currentState = primer.getBlockState(x, y, z);
                    boolean isCarvable = (currentState == stone || currentState == deepslate);

                    if (!isCarvable) continue;

                    double realY = y + this.offsetY;
                    context.reset(realX, realY, realZ, y);

                    for (ICaveGenerator gen : generators) {
                        if (gen.canGenerate()) {
                            gen.sample(context);

                            if (context.shouldCarve) break;
                        }
                    }

                    if (context.shouldCarve) {
                        if (isSafeToCarve(primer, x, y, z)) {
                            if (y - 1 < this.ctx.lavaLevel()) {
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

    private boolean isSafeToCarve(ChunkPrimer primer, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    int nz = z + dz;

                    if (nx >= 0 && nx < 16 && ny >= this.ctx.minY() && ny < this.ctx.maxY() && nz >= 0 && nz < 16) {
                        if (primer.getBlockState(nx, ny, nz).getBlock() == Blocks.WATER) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}
