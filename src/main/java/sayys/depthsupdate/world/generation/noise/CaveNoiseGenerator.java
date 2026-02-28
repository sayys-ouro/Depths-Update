package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import sayys.depthsupdate.DepthsUpdateMod;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

import java.util.Random;

public class CaveNoiseGenerator {
    private final Perlin cheeseNoise;
    private final Perlin spaghettiNoiseA;
    private final Perlin spaghettiNoiseB;

    private static final double CHEESE_THRESHOLD = 0.75;

    private static final double SPAGHETTI_THICKNESS = 0.06;

    private static final double CHEESE_XZ_SCALE = 0.035;
    private static final double CHEESE_Y_SCALE = 0.025;

    private static final double SPAGHETTI_SCALE = 0.015;

    private static final int CAVE_MAX_Y = 30;
    private static final int CAVE_MIN_Y = -60; // Leave bottom bedrock

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    public CaveNoiseGenerator(World world) {
        long seed = world.getSeed();
        Random rand = new Random(seed);

        this.offsetX = rand.nextDouble() * 100000.0;
        this.offsetY = rand.nextDouble() * 100000.0;
        this.offsetZ = rand.nextDouble() * 100000.0;

        this.cheeseNoise = new Perlin();
        this.cheeseNoise.setSeed((int) seed + 420);
        this.cheeseNoise.setOctaveCount(2);

        this.spaghettiNoiseA = new Perlin();
        this.spaghettiNoiseA.setSeed((int) seed + 1337);
        this.spaghettiNoiseA.setOctaveCount(2);

        this.spaghettiNoiseB = new Perlin();
        this.spaghettiNoiseB.setSeed((int) seed + 7331);
        this.spaghettiNoiseB.setOctaveCount(2);
    }

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;

        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState deepslate = DepthsUpdateMod.RegistrationHandler.deepslate.getDefaultState();
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState water = Blocks.WATER.getDefaultState();
        IBlockState lava = Blocks.LAVA.getDefaultState();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double realX = worldX + x + this.offsetX;
                double realZ = worldZ + z + this.offsetZ;

                for (int y = CAVE_MIN_Y; y <= CAVE_MAX_Y; y++) {
                    double realY = y + this.offsetY;

                    IBlockState currentState = primer.getBlockState(x, y, z);
                    boolean isCarveable = (currentState == stone || currentState == deepslate);

                    if (!isCarveable) {
                        continue;
                    }

                    boolean carve = false;

                    double cheeseVal = cheeseNoise.getValue(realX * CHEESE_XZ_SCALE, realY * CHEESE_Y_SCALE,
                            realZ * CHEESE_XZ_SCALE);

                    double cheeseFade = 0.0;
                    if (y > 10) {
                        cheeseFade = ((double) (y - 10) / 20.0);
                    } else if (y < -50) {
                        cheeseFade = ((double) (-50 - y) / 10.0);
                    }

                    if (Math.abs(cheeseVal) - cheeseFade > CHEESE_THRESHOLD) {
                        carve = true;
                    }

                    if (!carve) {
                        double spagA = spaghettiNoiseA.getValue(realX * SPAGHETTI_SCALE, realY * SPAGHETTI_SCALE,
                                realZ * SPAGHETTI_SCALE);
                        double spagB = spaghettiNoiseB.getValue(realX * SPAGHETTI_SCALE, realY * SPAGHETTI_SCALE,
                                realZ * SPAGHETTI_SCALE);

                        double noodleThickness = Math.max(Math.abs(spagA), Math.abs(spagB));

                        double spagFade = 0.0;
                        if (y > 20) {
                            spagFade = ((double) (y - 20) / 10.0);
                        }

                        if (noodleThickness + (spagFade * 0.5) < SPAGHETTI_THICKNESS) {
                            carve = true;
                        }
                    }

                    if (carve) {
                        boolean touchesWater = false;
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                for (int dz = -1; dz <= 1; dz++) {
                                    int nx = x + dx;
                                    int ny = y + dy;
                                    int nz = z + dz;
                                    if (nx >= 0 && nx < 16 && ny >= -64 && ny < 256 && nz >= 0 && nz < 16) {
                                        if (primer.getBlockState(nx, ny, nz).getBlock() == Blocks.WATER) {
                                            touchesWater = true;
                                            break;
                                        }
                                    }
                                }
                                if (touchesWater)
                                    break;
                            }
                            if (touchesWater)
                                break;
                        }

                        if (!touchesWater) {
                            if (y < -54) {
                                primer.setBlockState(x, y, z, lava);
                            } else {
                                primer.setBlockState(x, y, z, air);
                            }
                        }
                    }
                }
            }
        }
    }
}
