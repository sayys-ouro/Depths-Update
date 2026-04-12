package sayys.depthsupdate.world.surface;

import java.util.Arrays;
import java.util.function.Supplier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

import sayys.depthsupdate.world.density.NoiseChunk;
import sayys.depthsupdate.world.noise.NormalNoise;
import sayys.depthsupdate.world.noise.PositionalRandomFactory;
import sayys.depthsupdate.world.noise.RandomSource;

/**
 * Orchestrates surface block placement using the surface rule system.
 * Backport of vanilla SurfaceSystem for 1.12.2.
 */
public class SurfaceSystem {

    private static final IBlockState WHITE_TERRACOTTA = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(0);
    private static final IBlockState ORANGE_TERRACOTTA = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(1);
    private static final IBlockState TERRACOTTA = Blocks.HARDENED_CLAY.getDefaultState();
    private static final IBlockState YELLOW_TERRACOTTA = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(4);
    private static final IBlockState BROWN_TERRACOTTA = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(12);
    private static final IBlockState RED_TERRACOTTA = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(14);
    private static final IBlockState LIGHT_GRAY_TERRACOTTA = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(8);

    private final IBlockState defaultBlock;
    private final int seaLevel;
    private final IBlockState[] clayBands;
    private final NormalNoise clayBandsOffsetNoise;
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;
    private final PositionalRandomFactory noiseRandom;

    public SurfaceSystem(IBlockState defaultBlock, int seaLevel,
                         NormalNoise surfaceNoise, NormalNoise surfaceSecondaryNoise,
                         NormalNoise clayBandsOffsetNoise,
                         PositionalRandomFactory noiseRandom) {
        this.defaultBlock = defaultBlock;
        this.seaLevel = seaLevel;
        this.surfaceNoise = surfaceNoise;
        this.surfaceSecondaryNoise = surfaceSecondaryNoise;
        this.clayBandsOffsetNoise = clayBandsOffsetNoise;
        this.noiseRandom = noiseRandom;
        this.clayBands = generateBands(noiseRandom.fromHashOf("clay_bands"));
    }

    /**
     * Builds surface blocks for a chunk column using the rule system.
     *
     * @param primer the chunk primer to modify
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param noiseChunk the noise chunk for preliminary surface calculations
     * @param ruleSource the compiled surface rule tree
     * @param biomeGetter function to get the biome at any position
     * @param minY minimum world Y
     * @param maxY maximum world Y (inclusive)
     */
    public void buildSurface(ChunkPrimer primer, int chunkX, int chunkZ,
                              NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource,
                              BiomeGetter biomeGetter, int minY, int maxY) {
        int minBlockX = chunkX << 4;
        int minBlockZ = chunkZ << 4;

        // Create context and rule ONCE for the whole chunk — vanilla reuses these
        SurfaceRules.Context context = new SurfaceRules.Context(
                this, noiseChunk, (bx, bz) -> biomeGetter.getBiome(bx, bz), minY, maxY);
        SurfaceRules.SurfaceRule rule = ruleSource.apply(context);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int blockX = minBlockX + x;
                int blockZ = minBlockZ + z;

                // Find highest non-air block
                int height = maxY;
                for (int y = maxY; y >= minY; --y) {
                    IBlockState state = primer.getBlockState(x, y, z);
                    if (state != null && state.getBlock() != Blocks.AIR) {
                        height = y;
                        break;
                    }
                }

                context.updateXZ(blockX, blockZ);
                int stoneAboveDepth = 0;
                int waterHeight = Integer.MIN_VALUE;
                int nextCeilingStoneY = Integer.MAX_VALUE;

                for (int y = height + 1; y >= minY; --y) {
                    IBlockState old = primer.getBlockState(x, y, z);
                    if (old == null || old.getBlock() == Blocks.AIR) {
                        stoneAboveDepth = 0;
                        waterHeight = Integer.MIN_VALUE;
                    } else if (old.getBlock() == Blocks.WATER || old.getBlock() == Blocks.FLOWING_WATER
                            || old.getBlock() == Blocks.LAVA || old.getBlock() == Blocks.FLOWING_LAVA) {
                        if (waterHeight == Integer.MIN_VALUE) {
                            waterHeight = y + 1;
                        }
                    } else {
                        // Solid block - check for ceiling stone depth
                        if (nextCeilingStoneY >= y) {
                            nextCeilingStoneY = Integer.MIN_VALUE;
                            for (int lookY = y - 1; lookY >= minY; --lookY) {
                                IBlockState lookState = primer.getBlockState(x, lookY, z);
                                if (!isStone(lookState)) {
                                    nextCeilingStoneY = lookY + 1;
                                    break;
                                }
                            }
                            // If we reached minY without finding non-stone, set to minY
                            if (nextCeilingStoneY == Integer.MIN_VALUE) {
                                nextCeilingStoneY = minY;
                            }
                        }

                        ++stoneAboveDepth;
                        int stoneBelow = y - nextCeilingStoneY + 1;
                        context.updateY(stoneAboveDepth, stoneBelow, waterHeight, blockX, y, blockZ);

                        // Only replace the default block (stone)
                        if (old == this.defaultBlock) {
                            IBlockState replacement = rule.tryApply(blockX, y, blockZ);
                            if (replacement != null) {
                                primer.setBlockState(x, y, z, replacement);
                            }
                        }
                    }
                }
            }
        }
    }

    public int getSurfaceDepth(int blockX, int blockZ) {
        double noiseValue = this.surfaceNoise.getValue(blockX, 0.0, blockZ);
        return (int) (noiseValue * 2.75 + 3.0 + this.noiseRandom.at(blockX, 0, blockZ).nextDouble() * 0.25);
    }

    public double getSurfaceSecondary(int blockX, int blockZ) {
        return this.surfaceSecondaryNoise.getValue(blockX, 0.0, blockZ);
    }

    /**
     * Gets the terracotta band color for the given position (for badlands biomes).
     */
    public IBlockState getBand(int worldX, int y, int worldZ) {
        int offset = (int) Math.round(this.clayBandsOffsetNoise.getValue(worldX, 0.0, worldZ) * 4.0);
        return this.clayBands[(y + offset + this.clayBands.length) % this.clayBands.length];
    }

    public int getSeaLevel() {
        return this.seaLevel;
    }

    private boolean isStone(IBlockState state) {
        if (state == null) return false;
        return state.getBlock() != Blocks.AIR
                && state.getBlock() != Blocks.WATER
                && state.getBlock() != Blocks.FLOWING_WATER
                && state.getBlock() != Blocks.LAVA
                && state.getBlock() != Blocks.FLOWING_LAVA;
    }

    // ===== Clay band generation =====

    private static IBlockState[] generateBands(RandomSource random) {
        IBlockState[] bands = new IBlockState[192];
        Arrays.fill(bands, TERRACOTTA);

        // Orange bands
        for (int i = 0; i < bands.length; ) {
            i += random.nextInt(5) + 1;
            if (i < bands.length) {
                bands[i] = ORANGE_TERRACOTTA;
            }
        }

        makeBands(random, bands, 1, YELLOW_TERRACOTTA);
        makeBands(random, bands, 2, BROWN_TERRACOTTA);
        makeBands(random, bands, 1, RED_TERRACOTTA);

        // White bands
        int whiteBandCount = 9 + random.nextInt(7); // 9-15 inclusive
        int placed = 0;
        for (int start = 0; placed < whiteBandCount && start < bands.length; start += random.nextInt(16) + 4) {
            bands[start] = WHITE_TERRACOTTA;
            if (start - 1 > 0 && random.nextInt(2) == 0) {
                bands[start - 1] = LIGHT_GRAY_TERRACOTTA;
            }
            if (start + 1 < bands.length && random.nextInt(2) == 0) {
                bands[start + 1] = LIGHT_GRAY_TERRACOTTA;
            }
            ++placed;
        }

        return bands;
    }

    private static void makeBands(RandomSource random, IBlockState[] bands, int baseWidth, IBlockState state) {
        int bandCount = 6 + random.nextInt(10); // 6-15 inclusive
        for (int i = 0; i < bandCount; ++i) {
            int width = baseWidth + random.nextInt(3);
            int start = random.nextInt(bands.length);
            for (int p = 0; start + p < bands.length && p < width; ++p) {
                bands[start + p] = state;
            }
        }
    }

    // ===== BiomeGetter interface =====

    @FunctionalInterface
    public interface BiomeGetter {
        Biome getBiome(int blockX, int blockZ);
    }
}
