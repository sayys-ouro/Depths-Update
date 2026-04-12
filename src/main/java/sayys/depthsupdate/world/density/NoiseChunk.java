package sayys.depthsupdate.world.density;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sayys.depthsupdate.world.noise.NoiseUtils;

/**
 * Evaluates the density function tree for a single chunk, with caching and interpolation.
 * Implements FunctionContext (provides block coordinates) and ContextProvider (bulk evaluation).
 *
 * The Visitor pattern replaces Marker density functions with cached implementations:
 * - Interpolated → NoiseInterpolator (trilinear interpolation across cell corners)
 * - FlatCache → FlatCacheImpl (2D XZ grid, evaluated once per chunk)
 * - Cache2D → Cache2DImpl (single-value XZ cache)
 * - CacheOnce → CacheOnceImpl (single-value tick cache)
 * - CacheAllInCell → CacheAllInCellImpl (per-cell array cache)
 *
 * Faithful backport of modern Minecraft's NoiseChunk.
 */
public class NoiseChunk implements DensityFunction.FunctionContext, DensityFunction.ContextProvider {

    private final int cellCountXZ;
    private final int cellCountY;
    private final int cellNoiseMinY;
    private final int firstCellX;
    private final int firstCellZ;
    private final int firstNoiseX;
    private final int firstNoiseZ;
    private final int noiseSizeXZ;
    private final int cellWidth;
    private final int cellHeight;

    private final List<NoiseInterpolator> interpolators = new ArrayList<>();
    private final List<CacheAllInCellImpl> cellCaches = new ArrayList<>();
    private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

    private final DensityFunction preliminarySurfaceLevel;
    private final DensityFunction fullNoiseDensity;

    // Interpolation state
    private boolean interpolating;
    private boolean fillingCell;
    private int cellStartBlockX;
    private int cellStartBlockY;
    private int cellStartBlockZ;
    private int inCellX;
    private int inCellY;
    private int inCellZ;
    private long interpolationCounter;
    private long arrayInterpolationCounter;
    private int arrayIndex;

    private final DensityFunction.ContextProvider sliceFillingContextProvider;

    /**
     * Creates a NoiseChunk for terrain generation.
     *
     * @param chunkMinBlockX minimum block X of the chunk (chunkX * 16)
     * @param chunkMinBlockZ minimum block Z of the chunk (chunkZ * 16)
     * @param noiseSettings noise configuration for the dimension
     * @param router the NoiseRouter containing all density functions
     */
    public NoiseChunk(int chunkMinBlockX, int chunkMinBlockZ, NoiseSettings noiseSettings, NoiseRouter router) {
        this.cellWidth = noiseSettings.getCellWidth();
        this.cellHeight = noiseSettings.getCellHeight();
        this.cellCountXZ = 16 / this.cellWidth;
        this.cellCountY = Math.floorDiv(noiseSettings.height(), this.cellHeight);
        this.cellNoiseMinY = Math.floorDiv(noiseSettings.minY(), this.cellHeight);
        this.firstCellX = Math.floorDiv(chunkMinBlockX, this.cellWidth);
        this.firstCellZ = Math.floorDiv(chunkMinBlockZ, this.cellWidth);
        // QuartPos: divide by 4 (biome resolution)
        this.firstNoiseX = Math.floorDiv(chunkMinBlockX, 4);
        this.firstNoiseZ = Math.floorDiv(chunkMinBlockZ, 4);
        this.noiseSizeXZ = Math.floorDiv(this.cellCountXZ * this.cellWidth, 4);

        // Slice filling context: iterates over Y cells for a given XZ column
        this.sliceFillingContextProvider = new DensityFunction.ContextProvider() {
            @Override
            public DensityFunction.FunctionContext forIndex(int cellYIndex) {
                NoiseChunk.this.cellStartBlockY = (cellYIndex + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
                ++NoiseChunk.this.interpolationCounter;
                NoiseChunk.this.inCellY = 0;
                NoiseChunk.this.arrayIndex = cellYIndex;
                return NoiseChunk.this;
            }

            @Override
            public void fillAllDirectly(double[] output, DensityFunction function) {
                for (int cellYIndex = 0; cellYIndex < NoiseChunk.this.cellCountY + 1; ++cellYIndex) {
                    NoiseChunk.this.cellStartBlockY = (cellYIndex + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
                    ++NoiseChunk.this.interpolationCounter;
                    NoiseChunk.this.inCellY = 0;
                    NoiseChunk.this.arrayIndex = cellYIndex;
                    output[cellYIndex] = function.compute(NoiseChunk.this);
                }
            }
        };

        // Wrap the router through our caching visitor
        NoiseRouter wrappedRouter = router.mapAll(this::wrap);
        this.preliminarySurfaceLevel = wrappedRouter.preliminarySurfaceLevel();

        // Final density with cacheAllInCell
        DensityFunction fullNoiseValue = DensityFunctions.cacheAllInCell(wrappedRouter.finalDensity())
                .mapAll(this::wrap);
        this.fullNoiseDensity = fullNoiseValue;
    }

    // ===== FunctionContext implementation =====

    @Override
    public int blockX() {
        return this.cellStartBlockX + this.inCellX;
    }

    @Override
    public int blockY() {
        return this.cellStartBlockY + this.inCellY;
    }

    @Override
    public int blockZ() {
        return this.cellStartBlockZ + this.inCellZ;
    }

    // ===== ContextProvider implementation =====

    @Override
    public DensityFunction.FunctionContext forIndex(int cellIndex) {
        int zInCell = Math.floorMod(cellIndex, this.cellWidth);
        int xyIndex = Math.floorDiv(cellIndex, this.cellWidth);
        int xInCell = Math.floorMod(xyIndex, this.cellWidth);
        int yInCell = this.cellHeight - 1 - Math.floorDiv(xyIndex, this.cellWidth);
        this.inCellX = xInCell;
        this.inCellY = yInCell;
        this.inCellZ = zInCell;
        this.arrayIndex = cellIndex;
        return this;
    }

    @Override
    public void fillAllDirectly(double[] output, DensityFunction function) {
        this.arrayIndex = 0;
        for (int yInCell = this.cellHeight - 1; yInCell >= 0; --yInCell) {
            this.inCellY = yInCell;
            for (int xInCell = 0; xInCell < this.cellWidth; ++xInCell) {
                this.inCellX = xInCell;
                for (int zInCell = 0; zInCell < this.cellWidth; ++zInCell) {
                    this.inCellZ = zInCell;
                    output[this.arrayIndex++] = function.compute(this);
                }
            }
        }
    }

    // ===== Interpolation control =====

    public void initializeForFirstCellX() {
        if (this.interpolating) {
            throw new IllegalStateException("Starting interpolation twice");
        }
        this.interpolating = true;
        this.interpolationCounter = 0L;
        this.fillSlice(true, this.firstCellX);
    }

    public void advanceCellX(int cellXIndex) {
        this.fillSlice(false, this.firstCellX + cellXIndex + 1);
        this.cellStartBlockX = (this.firstCellX + cellXIndex) * this.cellWidth;
    }

    public void selectCellYZ(int cellYIndex, int cellZIndex) {
        for (NoiseInterpolator interpolator : this.interpolators) {
            interpolator.selectCellYZ(cellYIndex, cellZIndex);
        }
        this.fillingCell = true;
        this.cellStartBlockY = (cellYIndex + this.cellNoiseMinY) * this.cellHeight;
        this.cellStartBlockZ = (this.firstCellZ + cellZIndex) * this.cellWidth;
        ++this.arrayInterpolationCounter;

        for (CacheAllInCellImpl cellCache : this.cellCaches) {
            cellCache.noiseFiller.fillArray(cellCache.values, this);
        }
        ++this.arrayInterpolationCounter;
        this.fillingCell = false;
    }

    public void updateForY(int posY, double factorY) {
        this.inCellY = posY - this.cellStartBlockY;
        for (NoiseInterpolator interpolator : this.interpolators) {
            interpolator.updateForY(factorY);
        }
    }

    public void updateForX(int posX, double factorX) {
        this.inCellX = posX - this.cellStartBlockX;
        for (NoiseInterpolator interpolator : this.interpolators) {
            interpolator.updateForX(factorX);
        }
    }

    public void updateForZ(int posZ, double factorZ) {
        this.inCellZ = posZ - this.cellStartBlockZ;
        ++this.interpolationCounter;
        for (NoiseInterpolator interpolator : this.interpolators) {
            interpolator.updateForZ(factorZ);
        }
    }

    public void swapSlices() {
        this.interpolators.forEach(NoiseInterpolator::swapSlices);
    }

    public void stopInterpolation() {
        if (!this.interpolating) {
            throw new IllegalStateException("Not currently interpolating");
        }
        this.interpolating = false;
    }

    // ===== Public accessors =====

    public double getInterpolatedDensity() {
        return this.fullNoiseDensity.compute(this);
    }

    public int preliminarySurfaceLevel(int sampleX, int sampleZ) {
        int quantizedX = Math.floorDiv(sampleX, 4) * 4;
        int quantizedZ = Math.floorDiv(sampleZ, 4) * 4;
        return (int) Math.floor(this.preliminarySurfaceLevel.compute(
                new DensityFunction.SinglePointContext(quantizedX, 0, quantizedZ)));
    }

    /**
     * Returns the maximum preliminary surface level across a rectangular region,
     * sampling every 4 blocks. Used by the aquifer constructor to determine
     * the skipSamplingAboveY threshold.
     */
    public int maxPreliminarySurfaceLevel(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        int maxY = Integer.MIN_VALUE;
        for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ += 4) {
            for (int blockX = minBlockX; blockX <= maxBlockX; blockX += 4) {
                int surfaceLevel = this.preliminarySurfaceLevel(blockX, blockZ);
                if (surfaceLevel > maxY) {
                    maxY = surfaceLevel;
                }
            }
        }
        return maxY;
    }

    public int cellWidth() { return cellWidth; }
    public int cellHeight() { return cellHeight; }
    public int cellCountXZ() { return cellCountXZ; }
    public int cellCountY() { return cellCountY; }
    public int cellNoiseMinY() { return cellNoiseMinY; }

    // ===== Visitor: wrap Marker functions with cached implementations =====

    private DensityFunction wrap(DensityFunction function) {
        return this.wrapped.computeIfAbsent(function, this::wrapNew);
    }

    private DensityFunction wrapNew(DensityFunction function) {
        if (function instanceof DensityFunctions.Marker marker) {
            return switch (marker.type()) {
                case Interpolated -> new NoiseInterpolator(marker.wrapped());
                case FlatCache -> new FlatCacheImpl(marker.wrapped(), true);
                case Cache2D -> new Cache2DImpl(marker.wrapped());
                case CacheOnce -> new CacheOnceImpl(marker.wrapped());
                case CacheAllInCell -> new CacheAllInCellImpl(marker.wrapped());
            };
        }
        // BlendAlpha/BlendOffset: no world blending in 1.12.2
        if (function instanceof DensityFunctions.BlendAlpha) {
            return DensityFunctions.constant(1.0);
        }
        if (function instanceof DensityFunctions.BlendOffset) {
            return DensityFunctions.constant(0.0);
        }
        return function;
    }

    // ===== Slice filling =====

    private void fillSlice(boolean slice0, int cellX) {
        this.cellStartBlockX = cellX * this.cellWidth;
        this.inCellX = 0;

        for (int cellZIndex = 0; cellZIndex < this.cellCountXZ + 1; ++cellZIndex) {
            int cellZ = this.firstCellZ + cellZIndex;
            this.cellStartBlockZ = cellZ * this.cellWidth;
            this.inCellZ = 0;
            ++this.arrayInterpolationCounter;

            for (NoiseInterpolator interpolator : this.interpolators) {
                double[] slice = (slice0 ? interpolator.slice0 : interpolator.slice1)[cellZIndex];
                interpolator.fillSliceColumn(slice, this.sliceFillingContextProvider);
            }
        }
        ++this.arrayInterpolationCounter;
    }

    // ===== Inner interface for NoiseChunk density functions =====

    private interface NoiseChunkDensityFunction extends DensityFunction {
        DensityFunction wrapped();

        @Override
        default double minValue() { return this.wrapped().minValue(); }

        @Override
        default double maxValue() { return this.wrapped().maxValue(); }
    }

    // ===== NoiseInterpolator =====

    public class NoiseInterpolator implements NoiseChunkDensityFunction {
        double[][] slice0;
        double[][] slice1;
        private final DensityFunction noiseFiller;
        private double noise000, noise001, noise100, noise101;
        private double noise010, noise011, noise110, noise111;
        private double valueXZ00, valueXZ10, valueXZ01, valueXZ11;
        private double valueZ0, valueZ1;
        private double value;

        NoiseInterpolator(DensityFunction noiseFiller) {
            this.noiseFiller = noiseFiller;
            this.slice0 = allocateSlice(cellCountY, cellCountXZ);
            this.slice1 = allocateSlice(cellCountY, cellCountXZ);
            interpolators.add(this);
        }

        private double[][] allocateSlice(int cellCountY, int cellCountZ) {
            int sizeZ = cellCountZ + 1;
            int sizeY = cellCountY + 1;
            double[][] result = new double[sizeZ][];
            for (int i = 0; i < sizeZ; i++) {
                result[i] = new double[sizeY];
            }
            return result;
        }

        void selectCellYZ(int cellYIndex, int cellZIndex) {
            this.noise000 = this.slice0[cellZIndex][cellYIndex];
            this.noise001 = this.slice0[cellZIndex + 1][cellYIndex];
            this.noise100 = this.slice1[cellZIndex][cellYIndex];
            this.noise101 = this.slice1[cellZIndex + 1][cellYIndex];
            this.noise010 = this.slice0[cellZIndex][cellYIndex + 1];
            this.noise011 = this.slice0[cellZIndex + 1][cellYIndex + 1];
            this.noise110 = this.slice1[cellZIndex][cellYIndex + 1];
            this.noise111 = this.slice1[cellZIndex + 1][cellYIndex + 1];
        }

        void updateForY(double factorY) {
            this.valueXZ00 = NoiseUtils.lerp(factorY, this.noise000, this.noise010);
            this.valueXZ10 = NoiseUtils.lerp(factorY, this.noise100, this.noise110);
            this.valueXZ01 = NoiseUtils.lerp(factorY, this.noise001, this.noise011);
            this.valueXZ11 = NoiseUtils.lerp(factorY, this.noise101, this.noise111);
        }

        void updateForX(double factorX) {
            this.valueZ0 = NoiseUtils.lerp(factorX, this.valueXZ00, this.valueXZ10);
            this.valueZ1 = NoiseUtils.lerp(factorX, this.valueXZ01, this.valueXZ11);
        }

        void updateForZ(double factorZ) {
            this.value = NoiseUtils.lerp(factorZ, this.valueZ0, this.valueZ1);
        }

        void swapSlices() {
            double[][] tmp = this.slice0;
            this.slice0 = this.slice1;
            this.slice1 = tmp;
        }

        /** Fill a slice column from the underlying noise filler (used during slice initialization). */
        void fillSliceColumn(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.noiseFiller.fillArray(output, contextProvider);
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            if (context != NoiseChunk.this) {
                return this.noiseFiller.compute(context);
            }
            if (!interpolating) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            }
            if (fillingCell) {
                // During cell filling, use full trilinear interpolation
                return NoiseUtils.lerp3(
                        (double) inCellX / (double) cellWidth,
                        (double) inCellY / (double) cellHeight,
                        (double) inCellZ / (double) cellWidth,
                        this.noise000, this.noise100,
                        this.noise010, this.noise110,
                        this.noise001, this.noise101,
                        this.noise011, this.noise111);
            }
            return this.value;
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            if (fillingCell) {
                contextProvider.fillAllDirectly(output, this);
            } else {
                this.noiseFiller.fillArray(output, contextProvider);
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public DensityFunction wrapped() { return this.noiseFiller; }
    }

    // ===== FlatCacheImpl =====

    private class FlatCacheImpl implements NoiseChunkDensityFunction {
        private final DensityFunction noiseFiller;
        private final double[] values;
        private final int sizeXZ;

        FlatCacheImpl(DensityFunction noiseFiller, boolean fill) {
            this.noiseFiller = noiseFiller;
            this.sizeXZ = noiseSizeXZ + 1;
            this.values = new double[this.sizeXZ * this.sizeXZ];
            if (fill) {
                for (int x = 0; x <= noiseSizeXZ; ++x) {
                    int quartX = firstNoiseX + x;
                    int blockX = quartX * 4;
                    for (int z = 0; z <= noiseSizeXZ; ++z) {
                        int quartZ = firstNoiseZ + z;
                        int blockZ = quartZ * 4;
                        this.values[x + z * this.sizeXZ] =
                                noiseFiller.compute(new DensityFunction.SinglePointContext(blockX, 0, blockZ));
                    }
                }
            }
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            int quartX = Math.floorDiv(context.blockX(), 4);
            int quartZ = Math.floorDiv(context.blockZ(), 4);
            int x = quartX - firstNoiseX;
            int z = quartZ - firstNoiseZ;
            return (x >= 0 && z >= 0 && x < this.sizeXZ && z < this.sizeXZ)
                    ? this.values[x + z * this.sizeXZ]
                    : this.noiseFiller.compute(context);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public DensityFunction wrapped() { return this.noiseFiller; }
    }

    // ===== Cache2DImpl =====

    private static class Cache2DImpl implements NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastPos2D = Long.MIN_VALUE;
        private double lastValue;

        Cache2DImpl(DensityFunction function) {
            this.function = function;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            int blockX = context.blockX();
            int blockZ = context.blockZ();
            long pos2D = packXZ(blockX, blockZ);
            if (this.lastPos2D == pos2D) {
                return this.lastValue;
            }
            this.lastPos2D = pos2D;
            double value = this.function.compute(context);
            this.lastValue = value;
            return value;
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.function.fillArray(output, contextProvider);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public DensityFunction wrapped() { return this.function; }

        private static long packXZ(int x, int z) {
            return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
        }
    }

    // ===== CacheOnceImpl =====

    private class CacheOnceImpl implements NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastCounter;
        private long lastArrayCounter;
        private double lastValue;
        private double[] lastArray;

        CacheOnceImpl(DensityFunction function) {
            this.function = function;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            if (context != NoiseChunk.this) {
                return this.function.compute(context);
            }
            if (this.lastArray != null && this.lastArrayCounter == arrayInterpolationCounter) {
                return this.lastArray[arrayIndex];
            }
            if (this.lastCounter == interpolationCounter) {
                return this.lastValue;
            }
            this.lastCounter = interpolationCounter;
            double value = this.function.compute(context);
            this.lastValue = value;
            return value;
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            if (this.lastArray != null && this.lastArrayCounter == arrayInterpolationCounter) {
                System.arraycopy(this.lastArray, 0, output, 0, output.length);
            } else {
                this.function.fillArray(output, contextProvider);
                if (this.lastArray != null && this.lastArray.length == output.length) {
                    System.arraycopy(output, 0, this.lastArray, 0, output.length);
                } else {
                    this.lastArray = output.clone();
                }
                this.lastArrayCounter = arrayInterpolationCounter;
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public DensityFunction wrapped() { return this.function; }
    }

    // ===== CacheAllInCellImpl =====

    private class CacheAllInCellImpl implements NoiseChunkDensityFunction {
        final DensityFunction noiseFiller;
        final double[] values;

        CacheAllInCellImpl(DensityFunction noiseFiller) {
            this.noiseFiller = noiseFiller;
            this.values = new double[cellWidth * cellWidth * cellHeight];
            cellCaches.add(this);
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            if (context != NoiseChunk.this) {
                return this.noiseFiller.compute(context);
            }
            if (!interpolating) {
                throw new IllegalStateException("Trying to sample cache outside the interpolation loop");
            }
            int x = inCellX;
            int y = inCellY;
            int z = inCellZ;
            if (x >= 0 && y >= 0 && z >= 0 && x < cellWidth && y < cellHeight && z < cellWidth) {
                return this.values[((cellHeight - 1 - y) * cellWidth + x) * cellWidth + z];
            }
            return this.noiseFiller.compute(context);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public DensityFunction wrapped() { return this.noiseFiller; }
    }
}
