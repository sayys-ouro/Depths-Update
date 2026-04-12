package sayys.depthsupdate.world.density;

import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;

import sayys.depthsupdate.world.noise.NoiseUtils;
import sayys.depthsupdate.world.noise.PositionalRandomFactory;
import sayys.depthsupdate.world.noise.RandomSource;

/**
 * Aquifer system integrated with the density function pipeline.
 * Determines fluid placement (water/lava) based on noise-driven Voronoi cells.
 *
 * Faithful backport of modern Minecraft's Aquifer.
 */
public interface Aquifer {

    /**
     * Computes the block state for a position given its terrain density.
     * Returns null if the position should be solid stone, or the fluid/air block state.
     *
     * @param context the function context providing block coordinates
     * @param density the terrain density value (positive = solid, negative = air/fluid)
     * @return null for solid, or the appropriate fluid/air state
     */
    IBlockState computeSubstance(DensityFunction.FunctionContext context, double density);

    boolean shouldScheduleFluidUpdate();

    static Aquifer create(NoiseChunk noiseChunk, int chunkX, int chunkZ, NoiseRouter router,
                           PositionalRandomFactory randomFactory, int minBlockY, int yBlockSize,
                           FluidPicker globalFluidPicker) {
        return new NoiseBasedAquifer(noiseChunk, chunkX, chunkZ, router, randomFactory,
                minBlockY, yBlockSize, globalFluidPicker);
    }

    static Aquifer createDisabled(FluidPicker fluidPicker) {
        return new Aquifer() {
            @Override
            public IBlockState computeSubstance(DensityFunction.FunctionContext context, double density) {
                if (density > 0.0) {
                    return null; // solid
                }
                FluidStatus status = fluidPicker.computeFluid(context.blockX(), context.blockY(), context.blockZ());
                return status.at(context.blockY());
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    // ===== NoiseBasedAquifer =====

    class NoiseBasedAquifer implements Aquifer {
        private static final int X_RANGE = 10;
        private static final int Y_RANGE = 9;
        private static final int Z_RANGE = 10;
        private static final double FLOWING_UPDATE_SIMILARITY = similarity(
                MathHelper.floor(10.0 * 10.0), MathHelper.floor(12.0 * 12.0));

        private final NoiseChunk noiseChunk;
        private final DensityFunction barrierNoise;
        private final DensityFunction fluidLevelFloodednessNoise;
        private final DensityFunction fluidLevelSpreadNoise;
        private final DensityFunction lavaNoise;
        private final DensityFunction erosion;
        private final DensityFunction depth;
        private final PositionalRandomFactory positionalRandomFactory;
        private final FluidStatus[] aquiferCache;
        private final long[] aquiferLocationCache;
        private final FluidPicker globalFluidPicker;
        private boolean shouldScheduleFluidUpdate;
        private final int skipSamplingAboveY;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;

        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = {
                {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1},
                {-3, 0}, {-2, 0}, {-1, 0}, {1, 0},
                {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
        };

        NoiseBasedAquifer(NoiseChunk noiseChunk, int chunkX, int chunkZ, NoiseRouter router,
                          PositionalRandomFactory randomFactory, int minBlockY, int yBlockSize,
                          FluidPicker globalFluidPicker) {
            this.noiseChunk = noiseChunk;
            this.barrierNoise = router.barrierNoise();
            this.fluidLevelFloodednessNoise = router.fluidLevelFloodednessNoise();
            this.fluidLevelSpreadNoise = router.fluidLevelSpreadNoise();
            this.lavaNoise = router.lavaNoise();
            this.erosion = router.erosion();
            this.depth = router.depth();
            this.positionalRandomFactory = randomFactory;
            int chunkMinBlockX = chunkX << 4;
            int chunkMinBlockZ = chunkZ << 4;
            int chunkMaxBlockX = chunkMinBlockX + 15;
            int chunkMaxBlockZ = chunkMinBlockZ + 15;
            this.minGridX = gridX(chunkMinBlockX - 5);
            int maxGridX = gridX(chunkMaxBlockX - 5) + 1;
            this.gridSizeX = maxGridX - this.minGridX + 1;
            this.minGridY = gridY(minBlockY + 1) - 1;
            int maxGridY = gridY(minBlockY + yBlockSize + 1) + 1;
            int gridSizeY = maxGridY - this.minGridY + 1;
            this.minGridZ = gridZ(chunkMinBlockZ - 5);
            int maxGridZ = gridZ(chunkMaxBlockZ - 5) + 1;
            this.gridSizeZ = maxGridZ - this.minGridZ + 1;
            int totalGridSize = this.gridSizeX * gridSizeY * this.gridSizeZ;
            this.aquiferCache = new FluidStatus[totalGridSize];
            this.aquiferLocationCache = new long[totalGridSize];
            Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
            this.globalFluidPicker = globalFluidPicker;

            // Compute skip sampling Y — sample the entire grid region like vanilla does
            int maxAdjustedSurface = adjustSurfaceLevel(noiseChunk.maxPreliminarySurfaceLevel(
                    fromGridX(this.minGridX, 0), fromGridZ(this.minGridZ, 0),
                    fromGridX(maxGridX, 9), fromGridZ(maxGridZ, 9)));
            int skipGridY = gridY(maxAdjustedSurface + 12) + 1;
            this.skipSamplingAboveY = fromGridY(skipGridY, 11) - 1;
        }

        private int getIndex(int gridX, int gridY, int gridZ) {
            int x = gridX - this.minGridX;
            int y = gridY - this.minGridY;
            int z = gridZ - this.minGridZ;
            return (y * this.gridSizeZ + z) * this.gridSizeX + x;
        }

        @Override
        public IBlockState computeSubstance(DensityFunction.FunctionContext context, double density) {
            if (density > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null; // solid
            }

            int posX = context.blockX();
            int posY = context.blockY();
            int posZ = context.blockZ();
            FluidStatus globalFluid = this.globalFluidPicker.computeFluid(posX, posY, posZ);

            if (posY > this.skipSamplingAboveY) {
                this.shouldScheduleFluidUpdate = false;
                return globalFluid.at(posY);
            }

            if (globalFluid.at(posY) == Blocks.LAVA.getDefaultState()) {
                this.shouldScheduleFluidUpdate = false;
                return Blocks.LAVA.getDefaultState();
            }

            // Find 4 closest aquifer centers using Voronoi grid
            int xAnchor = gridX(posX - 5);
            int yAnchor = gridY(posY + 1);
            int zAnchor = gridZ(posZ - 5);
            int distSq1 = Integer.MAX_VALUE, distSq2 = Integer.MAX_VALUE;
            int distSq3 = Integer.MAX_VALUE, distSq4 = Integer.MAX_VALUE;
            int idx1 = 0, idx2 = 0, idx3 = 0, idx4 = 0;

            for (int x1 = 0; x1 <= 1; ++x1) {
                for (int y1 = -1; y1 <= 1; ++y1) {
                    for (int z1 = 0; z1 <= 1; ++z1) {
                        int gx = xAnchor + x1;
                        int gy = yAnchor + y1;
                        int gz = zAnchor + z1;
                        int index = this.getIndex(gx, gy, gz);
                        long existing = this.aquiferLocationCache[index];
                        long location;
                        if (existing != Long.MAX_VALUE) {
                            location = existing;
                        } else {
                            RandomSource random = this.positionalRandomFactory.at(gx, gy, gz);
                            location = packPos(
                                    fromGridX(gx, random.nextInt(X_RANGE)),
                                    fromGridY(gy, random.nextInt(Y_RANGE)),
                                    fromGridZ(gz, random.nextInt(Z_RANGE)));
                            this.aquiferLocationCache[index] = location;
                        }

                        int dx = unpackX(location) - posX;
                        int dy = unpackY(location) - posY;
                        int dz = unpackZ(location) - posZ;
                        int newDist = dx * dx + dy * dy + dz * dz;

                        if (distSq1 >= newDist) {
                            idx4 = idx3; idx3 = idx2; idx2 = idx1; idx1 = index;
                            distSq4 = distSq3; distSq3 = distSq2; distSq2 = distSq1; distSq1 = newDist;
                        } else if (distSq2 >= newDist) {
                            idx4 = idx3; idx3 = idx2; idx2 = index;
                            distSq4 = distSq3; distSq3 = distSq2; distSq2 = newDist;
                        } else if (distSq3 >= newDist) {
                            idx4 = idx3; idx3 = index;
                            distSq4 = distSq3; distSq3 = newDist;
                        } else if (distSq4 >= newDist) {
                            idx4 = index;
                            distSq4 = newDist;
                        }
                    }
                }
            }

            FluidStatus status1 = this.getAquiferStatus(idx1);
            double sim12 = similarity(distSq1, distSq2);
            IBlockState fluidState = status1.at(posY);

            if (sim12 <= 0.0) {
                this.shouldScheduleFluidUpdate = sim12 >= FLOWING_UPDATE_SIMILARITY
                        && !status1.equals(this.getAquiferStatus(idx2));
                return fluidState;
            }

            if (fluidState == Blocks.WATER.getDefaultState()) {
                FluidStatus belowGlobal = this.globalFluidPicker.computeFluid(posX, posY - 1, posZ);
                if (belowGlobal.at(posY - 1) == Blocks.LAVA.getDefaultState()) {
                    this.shouldScheduleFluidUpdate = true;
                    return fluidState;
                }
            }

            // Barrier calculations
            double[] barrierCache = {Double.NaN};
            FluidStatus status2 = this.getAquiferStatus(idx2);
            double barrier12 = sim12 * this.calculatePressure(context, barrierCache, status1, status2);
            if (density + barrier12 > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null; // solid barrier
            }

            FluidStatus status3 = this.getAquiferStatus(idx3);
            double sim13 = similarity(distSq1, distSq3);
            if (sim13 > 0.0) {
                double barrier13 = sim12 * sim13 * this.calculatePressure(context, barrierCache, status1, status3);
                if (density + barrier13 > 0.0) {
                    this.shouldScheduleFluidUpdate = false;
                    return null;
                }
            }

            double sim23 = similarity(distSq2, distSq3);
            if (sim23 > 0.0) {
                double barrier23 = sim12 * sim23 * this.calculatePressure(context, barrierCache, status2, status3);
                if (density + barrier23 > 0.0) {
                    this.shouldScheduleFluidUpdate = false;
                    return null;
                }
            }

            // Match vanilla's shouldScheduleFluidUpdate logic exactly
            boolean mayFlow12 = !status1.equals(status2);
            boolean mayFlow23 = sim23 >= FLOWING_UPDATE_SIMILARITY && !status2.equals(status3);
            boolean mayFlow13 = sim13 >= FLOWING_UPDATE_SIMILARITY && !status1.equals(status3);
            if (mayFlow12 || mayFlow23 || mayFlow13) {
                this.shouldScheduleFluidUpdate = true;
            } else {
                this.shouldScheduleFluidUpdate = sim13 >= FLOWING_UPDATE_SIMILARITY
                        && similarity(distSq1, distSq4) >= FLOWING_UPDATE_SIMILARITY
                        && !status1.equals(this.getAquiferStatus(idx4));
            }
            return fluidState;
        }

        @Override
        public boolean shouldScheduleFluidUpdate() {
            return this.shouldScheduleFluidUpdate;
        }

        private static double similarity(int distSq1, int distSq2) {
            return 1.0 - (double) (distSq2 - distSq1) / 25.0;
        }

        private double calculatePressure(DensityFunction.FunctionContext context, double[] barrierCache,
                                          FluidStatus status1, FluidStatus status2) {
            int posY = context.blockY();
            IBlockState type1 = status1.at(posY);
            IBlockState type2 = status2.at(posY);

            // Lava-water interface: always create barrier
            if ((type1 == Blocks.LAVA.getDefaultState() && type2 == Blocks.WATER.getDefaultState())
                    || (type1 == Blocks.WATER.getDefaultState() && type2 == Blocks.LAVA.getDefaultState())) {
                return 2.0;
            }

            int fluidYDiff = Math.abs(status1.fluidLevel - status2.fluidLevel);
            if (fluidYDiff == 0) {
                return 0.0;
            }

            double averageFluidY = 0.5 * (status1.fluidLevel + status2.fluidLevel);
            double howFarAbove = (double) posY + 0.5 - averageFluidY;
            double baseValue = (double) fluidYDiff / 2.0;
            double distFromEdge = baseValue - Math.abs(howFarAbove);

            double gradient;
            if (howFarAbove > 0.0) {
                gradient = distFromEdge > 0.0 ? distFromEdge / 1.5 : distFromEdge / 2.5;
            } else {
                double amp = 3.0 + distFromEdge;
                gradient = amp > 0.0 ? amp / 3.0 : amp / 10.0;
            }

            double noiseValue;
            if (gradient < -2.0 || gradient > 2.0) {
                noiseValue = 0.0;
            } else {
                if (Double.isNaN(barrierCache[0])) {
                    barrierCache[0] = this.barrierNoise.compute(context);
                }
                noiseValue = barrierCache[0];
            }

            return 2.0 * (noiseValue + gradient);
        }

        private FluidStatus getAquiferStatus(int index) {
            FluidStatus cached = this.aquiferCache[index];
            if (cached != null) {
                return cached;
            }
            long location = this.aquiferLocationCache[index];
            FluidStatus status = this.computeFluid(
                    unpackX(location),
                    unpackY(location),
                    unpackZ(location));
            this.aquiferCache[index] = status;
            return status;
        }

        private FluidStatus computeFluid(int x, int y, int z) {
            FluidStatus globalFluid = this.globalFluidPicker.computeFluid(x, y, z);
            int lowestSurface = Integer.MAX_VALUE;
            int topOfCell = y + 12;
            int bottomOfCell = y - 12;
            boolean surfaceUnderGlobal = false;

            for (int[] offset : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                int sampleX = x + (offset[0] << 4);
                int sampleZ = z + (offset[1] << 4);
                int prelimSurface = this.noiseChunk.preliminarySurfaceLevel(sampleX, sampleZ);
                int adjSurface = adjustSurfaceLevel(prelimSurface);
                boolean isCenter = offset[0] == 0 && offset[1] == 0;

                if (isCenter && bottomOfCell > adjSurface) {
                    return globalFluid;
                }

                boolean topAboveSurface = topOfCell > adjSurface;
                if (topAboveSurface || isCenter) {
                    FluidStatus surfaceFluid = this.globalFluidPicker.computeFluid(sampleX, adjSurface, sampleZ);
                    if (surfaceFluid.at(adjSurface) != Blocks.AIR.getDefaultState()) {
                        if (isCenter) surfaceUnderGlobal = true;
                        if (topAboveSurface) return surfaceFluid;
                    }
                }
                lowestSurface = Math.min(lowestSurface, prelimSurface);
            }

            int fluidLevel = computeSurfaceLevel(x, y, z, globalFluid, lowestSurface, surfaceUnderGlobal);
            return new FluidStatus(fluidLevel, computeFluidType(x, y, z, globalFluid, fluidLevel));
        }

        private int adjustSurfaceLevel(int preliminarySurfaceLevel) {
            return preliminarySurfaceLevel + 8;
        }

        private int computeSurfaceLevel(int x, int y, int z, FluidStatus globalFluid,
                                         int lowestSurface, boolean surfaceUnderGlobal) {
            DensityFunction.SinglePointContext ctx = new DensityFunction.SinglePointContext(x, y, z);

            double partialFlood;
            double fullFlood;
            // Simplified: skip deep dark check (not relevant for 1.12.2 overworld)
            int surfDiff = lowestSurface + 8 - y;
            double floodFactor = surfaceUnderGlobal
                    ? NoiseUtils.clampedMap(surfDiff, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double floodNoise = NoiseUtils.clamp(this.fluidLevelFloodednessNoise.compute(ctx), -1.0, 1.0);
            double fullyThreshold = NoiseUtils.clampedMap(floodFactor, 1.0, 0.0, -0.3, 0.8);
            double partialThreshold = NoiseUtils.clampedMap(floodFactor, 1.0, 0.0, -0.8, 0.4);
            partialFlood = floodNoise - partialThreshold;
            fullFlood = floodNoise - fullyThreshold;

            int fluidLevel;
            if (fullFlood > 0.0) {
                fluidLevel = globalFluid.fluidLevel;
            } else if (partialFlood > 0.0) {
                fluidLevel = computeRandomizedFluidSurfaceLevel(x, y, z, lowestSurface);
            } else {
                fluidLevel = Integer.MIN_VALUE; // WAY_BELOW_MIN_Y equivalent
            }
            return fluidLevel;
        }

        private int computeRandomizedFluidSurfaceLevel(int x, int y, int z, int lowestSurface) {
            int cellX = Math.floorDiv(x, 16);
            int cellY = Math.floorDiv(y, 40);
            int cellZ = Math.floorDiv(z, 16);
            int cellMiddleY = cellY * 40 + 20;
            double spread = this.fluidLevelSpreadNoise.compute(
                    new DensityFunction.SinglePointContext(cellX, cellY, cellZ)) * 10.0;
            int spreadQ = quantize(spread, 3);
            return Math.min(lowestSurface, cellMiddleY + spreadQ);
        }

        private IBlockState computeFluidType(int x, int y, int z, FluidStatus globalFluid, int fluidLevel) {
            IBlockState type = globalFluid.fluidType;
            if (fluidLevel <= -10 && fluidLevel != Integer.MIN_VALUE
                    && type != Blocks.LAVA.getDefaultState()) {
                int cellX = Math.floorDiv(x, 64);
                int cellY = Math.floorDiv(y, 40);
                int cellZ = Math.floorDiv(z, 64);
                double lava = this.lavaNoise.compute(new DensityFunction.SinglePointContext(cellX, cellY, cellZ));
                if (Math.abs(lava) > 0.3) {
                    type = Blocks.LAVA.getDefaultState();
                }
            }
            return type;
        }

        private static int gridX(int blockCoord) { return blockCoord >> 4; }
        private static int fromGridX(int gridCoord, int offset) { return (gridCoord << 4) + offset; }
        private static int gridY(int blockCoord) { return Math.floorDiv(blockCoord, 12); }
        private static int fromGridY(int gridCoord, int offset) { return gridCoord * 12 + offset; }
        private static int gridZ(int blockCoord) { return blockCoord >> 4; }
        private static int fromGridZ(int gridCoord, int offset) { return (gridCoord << 4) + offset; }

        private static int quantize(double value, int quant) {
            return (int) Math.floor(value / quant) * quant;
        }

        // ===== Position packing helpers (replaces BlockPos.toLong/unpack which differ in 1.12.2) =====
        // Layout: X in bits 0-25 (26 bits, signed), Y in bits 26-37 (12 bits, signed), Z in bits 38-63 (26 bits, signed)
        private static final int NUM_X_BITS = 26;
        private static final int NUM_Y_BITS = 12;
        private static final int NUM_Z_BITS = 26;
        private static final long X_MASK = (1L << NUM_X_BITS) - 1;
        private static final long Y_MASK = (1L << NUM_Y_BITS) - 1;
        private static final long Z_MASK = (1L << NUM_Z_BITS) - 1;

        private static long packPos(int x, int y, int z) {
            return ((long) x & X_MASK) | (((long) y & Y_MASK) << NUM_X_BITS) | (((long) z & Z_MASK) << (NUM_X_BITS + NUM_Y_BITS));
        }

        private static int unpackX(long packed) {
            int raw = (int) (packed & X_MASK);
            return (raw << (32 - NUM_X_BITS)) >> (32 - NUM_X_BITS); // sign extend
        }

        private static int unpackY(long packed) {
            int raw = (int) ((packed >> NUM_X_BITS) & Y_MASK);
            return (raw << (32 - NUM_Y_BITS)) >> (32 - NUM_Y_BITS); // sign extend
        }

        private static int unpackZ(long packed) {
            int raw = (int) ((packed >> (NUM_X_BITS + NUM_Y_BITS)) & Z_MASK);
            return (raw << (32 - NUM_Z_BITS)) >> (32 - NUM_Z_BITS); // sign extend
        }
    }

    // ===== FluidPicker =====

    @FunctionalInterface
    interface FluidPicker {
        FluidStatus computeFluid(int blockX, int blockY, int blockZ);
    }

    // ===== FluidStatus =====

    class FluidStatus {
        public final int fluidLevel;
        public final IBlockState fluidType;

        public FluidStatus(int fluidLevel, IBlockState fluidType) {
            this.fluidLevel = fluidLevel;
            this.fluidType = fluidType;
        }

        public IBlockState at(int blockY) {
            return blockY < this.fluidLevel ? this.fluidType : Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FluidStatus other)) return false;
            return this.fluidLevel == other.fluidLevel && this.fluidType == other.fluidType;
        }

        @Override
        public int hashCode() {
            return 31 * this.fluidLevel + System.identityHashCode(this.fluidType);
        }
    }
}
