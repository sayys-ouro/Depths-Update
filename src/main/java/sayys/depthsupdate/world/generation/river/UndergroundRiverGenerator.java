package sayys.depthsupdate.world.generation.river;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.RidgedMulti;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.block.state.IBlockState;
import java.util.Random;

public class UndergroundRiverGenerator {
    private final RidgedMulti ridgedMultiNoise = new RidgedMulti();
    private final Perlin perlinNoise;
    private World world;
    private Random random;

    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState WATER = Blocks.WATER.getDefaultState();
    protected static final IBlockState STONE = Blocks.STONE.getDefaultState();

    public UndergroundRiverGenerator(World worldIn) {
        this.world = worldIn;
        long seed = worldIn.getSeed();
        this.random = new Random(seed);

        this.ridgedMultiNoise.setSeed((int) seed);
        this.ridgedMultiNoise.setFrequency(0.005);
        this.ridgedMultiNoise.setOctaveCount(1);
        this.ridgedMultiNoise.setLacunarity(0.0);

        this.perlinNoise = new Perlin();
        this.perlinNoise.setSeed((int) seed);
        this.perlinNoise.setFrequency(0.1);
        this.perlinNoise.setOctaveCount(2);
        this.perlinNoise.setLacunarity(0.0);
        this.perlinNoise.setPersistence(-0.4);
    }

    private double calculatePercent(double noise, double minNoise, double maxNoise) {
        double maxNoiseCalc = maxNoise - minNoise;
        double noiseCalc = noise - minNoise;
        return noiseCalc / maxNoiseCalc;
    }

    public int calculateHeightByCenter(double noise, double minNoise, double maxNoise, int minHeight, int maxHeight,
            int maxHeightIfCavern, double cavernWhenNoise) {
        double noiseCenter = (maxNoise - minNoise) / 2.0 + minNoise;
        double noisePercent = 0.0;
        if (noiseCenter > noise) {
            noisePercent = this.calculatePercent(noise, minNoise, noiseCenter);
        } else if (noiseCenter < noise) {
            if (noise > cavernWhenNoise) {
                noisePercent = this.calculatePercent(noise, maxHeightIfCavern, noiseCenter);
            } else {
                noisePercent = this.calculatePercent(noise, maxHeight, noiseCenter);
            }
        } else {
            noisePercent = 1.0;
        }
        int maxHeightCalc = maxHeight - minHeight;
        double height = (double) maxHeightCalc * noisePercent;
        return (int) Math.round(height) + minHeight;
    }

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        int baseY = -30;
        int waterLevel = baseY - 3;

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;

                double vnoise = this.ridgedMultiNoise.getValue(realX, 1.0, realZ);
                if (vnoise >= 0.6 && vnoise <= 2.0) {

                    int height = calculateHeightByCenter(vnoise, 0.6, 0.625, 1, 10, 17, 0.63);
                    if (height == 10 || height == 9) {
                        double pnoise = this.perlinNoise.getValue(realX, 1.0, realZ);
                        if (pnoise >= 0.3) {
                            ++height;
                            if (pnoise >= 0.38) {
                                ++height;
                            }
                        } else if (pnoise < 0.16) {
                            --height;
                        }
                    }

                    int startPointY = -height / 2;
                    for (int a = baseY - startPointY; a > baseY - height; --a) {
                        if (a <= -63 || a >= 255)
                            continue;

                        IBlockState current = primer.getBlockState(x, a, z);
                        if (current.getBlock() == Blocks.STONE
                                || current.getBlock() instanceof sayys.depthsupdate.block.BlockDeepslate) {
                            if (a > waterLevel) {
                                primer.setBlockState(x, a, z, AIR);
                            } else {
                                primer.setBlockState(x, a, z, WATER);
                            }
                        }
                    }
                }
            }
        }
    }
}
