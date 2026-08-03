package sayys.depthsupdate.world.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.block.BlockAmethystCluster;
import sayys.depthsupdate.registry.AmethystRegistry;
import sayys.depthsupdate.registry.DeepslateRegistry;

/**
 * Port of vanilla Minecraft's algorithm.
 */
public class AmethystGeodeGenerator implements IWorldGenerator {
    // Layer thickness thresholds
    private static final double FILLING = 1.7;
    private static final double INNER_LAYER = 2.2;
    private static final double MIDDLE_LAYER = 3.2;
    private static final double OUTER_LAYER = 4.2;

    // Crack settings
    private static final double CRACK_CHANCE = 0.95;
    private static final double BASE_CRACK_SIZE = 2.0;
    private static final int CRACK_POINT_OFFSET = 2;

    private static final double USE_POTENTIAL_PLACEMENTS_CHANCE = 0.35;
    private static final double USE_ALTERNATE_LAYER0_CHANCE = 0.083;
    private static final int OUTER_WALL_DIST_MIN = 4;
    private static final int OUTER_WALL_DIST_MAX = 6;
    private static final int DISTRIBUTION_POINTS_MIN = 3;
    private static final int DISTRIBUTION_POINTS_MAX = 4;
    private static final int POINT_OFFSET_MIN = 1;
    private static final int POINT_OFFSET_MAX = 2;
    private static final int MIN_GEN_OFFSET = -16;
    private static final int MAX_GEN_OFFSET = 16;
    private static final double NOISE_MULTIPLIER = 0.05;
    private static final int INVALID_BLOCKS_THRESHOLD = 1;

    private static final EnumFacing[] ALL_FACINGS = EnumFacing.values();

    public static void register() {
        if (DepthsUpdateConfig.amethystGeodes.enableAmethystGeodes
                && DepthsUpdateConfig.REGISTRY.enableAmethystFamily
                && DepthsUpdateConfig.REGISTRY.enableSmoothBasalt
                && DepthsUpdateConfig.REGISTRY.enableCalcite) {
            GameRegistry.registerWorldGenerator(new AmethystGeodeGenerator(), 120);
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }

        if (chunkGenerator instanceof ChunkGeneratorFlat || chunkGenerator instanceof ChunkGeneratorDebug) {
            return;
        }

        int rarity = DepthsUpdateConfig.amethystGeodes.geodeRarity;

        if (random.nextInt(rarity) != 0) {
            return;
        }

        int y = CaveRegion.randomYInWindow(random, HeightManager.get(world),
                DepthsUpdateConfig.amethystGeodes.geodeMinY,
                DepthsUpdateConfig.amethystGeodes.geodeMaxY);

        if (y == CaveRegion.NO_Y) {
            return;
        }

        int x = chunkX * 16 + 8 + random.nextInt(16);
        int z = chunkZ * 16 + 8 + random.nextInt(16);

        BlockPos origin = new BlockPos(x, y, z);
        GenerationLog.featurePlaced("amethyst geode", origin);
        generateGeode(world, random, origin);
    }

    private void generateGeode(World world, Random random, BlockPos origin) {
        int numPoints = uniformInt(random, DISTRIBUTION_POINTS_MIN, DISTRIBUTION_POINTS_MAX);

        long noiseSeed = world.getSeed() ^ 0x5DEECE66DL;

        double crackSizeAdjustment = (double) numPoints / (double) OUTER_WALL_DIST_MAX;

        double thresholdAir = 1.0 / Math.sqrt(FILLING);
        double thresholdInnerBlock = 1.0 / Math.sqrt(INNER_LAYER + crackSizeAdjustment);
        double thresholdMiddle = 1.0 / Math.sqrt(MIDDLE_LAYER + crackSizeAdjustment);
        double thresholdOuter = 1.0 / Math.sqrt(OUTER_LAYER + crackSizeAdjustment);
        double crackThreshold = 1.0 / Math.sqrt(BASE_CRACK_SIZE + random.nextDouble() / 2.0
                + (numPoints > 3 ? crackSizeAdjustment : 0.0));
        boolean shouldGenerateCrack = random.nextFloat() < CRACK_CHANCE;

        List<long[]> points = new ArrayList<>(); // [x, y, z, offset]
        int numInvalidPoints = 0;

        for (int i = 0; i < numPoints; i++) {
            int dx = uniformInt(random, OUTER_WALL_DIST_MIN, OUTER_WALL_DIST_MAX);
            int dy = uniformInt(random, OUTER_WALL_DIST_MIN, OUTER_WALL_DIST_MAX);
            int dz = uniformInt(random, OUTER_WALL_DIST_MIN, OUTER_WALL_DIST_MAX);
            BlockPos pointPos = origin.add(dx, dy, dz);

            IBlockState stateAtPoint = world.getBlockState(pointPos);

            if (stateAtPoint.getBlock().isAir(stateAtPoint, world, pointPos)
                    || isInvalidBlock(stateAtPoint)) {
                numInvalidPoints++;

                if (numInvalidPoints > INVALID_BLOCKS_THRESHOLD) {
                    return;
                }
            }

            int pointOffset = uniformInt(random, POINT_OFFSET_MIN, POINT_OFFSET_MAX);
            points.add(new long[]{pointPos.getX(), pointPos.getY(), pointPos.getZ(), pointOffset});
        }

        List<BlockPos> crackPoints = new ArrayList<>();

        if (shouldGenerateCrack) {
            int crackDir = random.nextInt(4);
            int crackOffset = numPoints * 2 + 1;

            if (crackDir == 0) {
                crackPoints.add(origin.add(crackOffset, 7, 0));
                crackPoints.add(origin.add(crackOffset, 5, 0));
                crackPoints.add(origin.add(crackOffset, 1, 0));
            } else if (crackDir == 1) {
                crackPoints.add(origin.add(0, 7, crackOffset));
                crackPoints.add(origin.add(0, 5, crackOffset));
                crackPoints.add(origin.add(0, 1, crackOffset));
            } else if (crackDir == 2) {
                crackPoints.add(origin.add(crackOffset, 7, crackOffset));
                crackPoints.add(origin.add(crackOffset, 5, crackOffset));
                crackPoints.add(origin.add(crackOffset, 1, crackOffset));
            } else {
                crackPoints.add(origin.add(0, 7, 0));
                crackPoints.add(origin.add(0, 5, 0));
                crackPoints.add(origin.add(0, 1, 0));
            }
        }

        // Iterate the bounding box and place blocks by layer
        List<BlockPos> potentialCrystalPlacements = new ArrayList<>();

        BlockPos cornerMin = origin.add(MIN_GEN_OFFSET, MIN_GEN_OFFSET, MIN_GEN_OFFSET);
        BlockPos cornerMax = origin.add(MAX_GEN_OFFSET, MAX_GEN_OFFSET, MAX_GEN_OFFSET);

        for (BlockPos pos : BlockPos.getAllInBox(cornerMin, cornerMax)) {
            double noiseValue = sampleNoise(pos.getX(), pos.getY(), pos.getZ(), noiseSeed) * NOISE_MULTIPLIER;

            // Sum inverse-sqrt distances to distribution points
            double distSumShell = 0.0;

            for (long[] point : points) {
                double dx = pos.getX() - point[0];
                double dy = pos.getY() - point[1];
                double dz = pos.getZ() - point[2];
                double distSq = dx * dx + dy * dy + dz * dz + point[3];
                distSumShell += fastInvSqrt(distSq) + noiseValue;
            }

            // Sum inverse-sqrt distances to crack points
            double distSumCrack = 0.0;

            for (BlockPos crackPoint : crackPoints) {
                double dx = pos.getX() - crackPoint.getX();
                double dy = pos.getY() - crackPoint.getY();
                double dz = pos.getZ() - crackPoint.getZ();
                double distSq = dx * dx + dy * dy + dz * dz + CRACK_POINT_OFFSET;
                distSumCrack += fastInvSqrt(distSq) + noiseValue;
            }

            if (distSumShell < thresholdOuter) {
                continue;
            }

            IBlockState existingState = world.getBlockState(pos);

            if (!canReplace(existingState)) {
                continue;
            }

            if (shouldGenerateCrack && distSumCrack >= crackThreshold && distSumShell < thresholdAir) {
                // Crack
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            } else if (distSumShell >= thresholdAir) {
                // air
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            } else if (distSumShell >= thresholdInnerBlock) {
                // amethyst block or budding amethyst
                boolean useAlternate = random.nextFloat() < USE_ALTERNATE_LAYER0_CHANCE;
                if (useAlternate) {
                    world.setBlockState(pos, AmethystRegistry.budding_amethyst.getDefaultState(), 2);
                } else {
                    world.setBlockState(pos, AmethystRegistry.amethyst_block.getDefaultState(), 2);
                }

                if (useAlternate && random.nextFloat() < USE_POTENTIAL_PLACEMENTS_CHANCE) {
                    potentialCrystalPlacements.add(pos.toImmutable());
                }
            } else if (distSumShell >= thresholdMiddle) {
                // calcite
                world.setBlockState(pos, DeepslateRegistry.calcite.getDefaultState(), 2);
            } else if (distSumShell >= thresholdOuter) {
                // smooth basalt
                world.setBlockState(pos, DeepslateRegistry.smooth_basalt.getDefaultState(), 2);
            }
        }

        // Place amethyst buds and clusters on budding amethyst surfaces
        IBlockState[] crystalStates = {
            AmethystRegistry.small_amethyst_bud.getDefaultState(),
            AmethystRegistry.medium_amethyst_bud.getDefaultState(),
            AmethystRegistry.large_amethyst_bud.getDefaultState(),
            AmethystRegistry.amethyst_cluster.getDefaultState()
        };

        for (BlockPos crystalPos : potentialCrystalPlacements) {
            IBlockState chosenCrystal = crystalStates[random.nextInt(crystalStates.length)];

            for (EnumFacing facing : ALL_FACINGS) {
                BlockPos placePos = crystalPos.offset(facing);
                IBlockState placeState = world.getBlockState(placePos);

                if (placeState.getBlock().isAir(placeState, world, placePos)) {
                    IBlockState orientedCrystal = chosenCrystal.withProperty(
                        BlockAmethystCluster.FACING, facing
                    );
                    world.setBlockState(placePos, orientedCrystal, 2);
                    break; // Only place one crystal per budding position
                }
            }
        }
    }

    private static double fastInvSqrt(double value) {
        return 1.0 / Math.sqrt(value);
    }

    /**
     * Smooth 3D value noise approximating vanilla's NormalNoise with octave -4.
     * Uses trilinear interpolation with smoothstep for gradual transitions.
     * The noise varies over ~16 blocks, producing organic but smooth layer boundaries.
     * Returns values roughly in [-1, 1].
     */
    private static double sampleNoise(int x, int y, int z, long seed) {
        // Scale to match vanilla's octave -4 (period of ~16 blocks)
        double sx = x / 16.0;
        double sy = y / 16.0;
        double sz = z / 16.0;

        int ix = (int) Math.floor(sx);
        int iy = (int) Math.floor(sy);
        int iz = (int) Math.floor(sz);

        double fx = sx - ix;
        double fy = sy - iy;
        double fz = sz - iz;

        // Smoothstep interpolation for smooth transitions
        fx = fx * fx * (3.0 - 2.0 * fx);
        fy = fy * fy * (3.0 - 2.0 * fy);
        fz = fz * fz * (3.0 - 2.0 * fz);

        // Hash all 8 cube corners
        double c000 = hashCorner(ix,     iy,     iz,     seed);
        double c100 = hashCorner(ix + 1, iy,     iz,     seed);
        double c010 = hashCorner(ix,     iy + 1, iz,     seed);
        double c110 = hashCorner(ix + 1, iy + 1, iz,     seed);
        double c001 = hashCorner(ix,     iy,     iz + 1, seed);
        double c101 = hashCorner(ix + 1, iy,     iz + 1, seed);
        double c011 = hashCorner(ix,     iy + 1, iz + 1, seed);
        double c111 = hashCorner(ix + 1, iy + 1, iz + 1, seed);

        // Trilinear interpolation
        double c00 = c000 + fx * (c100 - c000);
        double c10 = c010 + fx * (c110 - c010);
        double c01 = c001 + fx * (c101 - c001);
        double c11 = c011 + fx * (c111 - c011);

        double c0 = c00 + fy * (c10 - c00);
        double c1 = c01 + fy * (c11 - c01);

        return c0 + fz * (c1 - c0);
    }

    /**
     * Hash an integer corner position to a deterministic value in [-1, 1].
     */
    private static double hashCorner(int x, int y, int z, long seed) {
        long hash = seed;
        hash ^= (long) x * 0x27D4EB2F165B6L;
        hash ^= (long) y * 0x1B03738712FADL;
        hash ^= (long) z * 0x3C6EF372FE94FL;
        hash ^= hash >>> 33;
        hash *= 0xFF51AFD7ED558CCDL;
        hash ^= hash >>> 33;
        hash *= 0xC4CEB9FE1A85EC53L;
        hash ^= hash >>> 33;

        return ((double) (hash & 0xFFFFFFL) / (double) 0xFFFFFFL) * 2.0 - 1.0;
    }

    /**
     * Returns a uniform random int in [min, max] inclusive.
     */
    private static int uniformInt(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static boolean isInvalidBlock(IBlockState state) {
        Block block = state.getBlock();

        return block == Blocks.BEDROCK
            || block == Blocks.ICE
            || block == Blocks.PACKED_ICE
            || state.getMaterial().isLiquid();
    }

    /**
     * Determines whether the geode can replace the existing block at a position.
     */
    private static boolean canReplace(IBlockState state) {
        Block block = state.getBlock();
        return block != Blocks.BEDROCK
            && block != Blocks.MOB_SPAWNER
            && block != Blocks.CHEST
            && block != Blocks.END_PORTAL_FRAME;
    }
}
