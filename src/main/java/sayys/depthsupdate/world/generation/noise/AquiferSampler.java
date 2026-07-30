package sayys.depthsupdate.world.generation.noise;

import java.util.Arrays;
import java.util.Random;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

/**
 * Port of Vanilla's NoiseBasedAquifer, consulted at carve time. Every block a
 * cave generator wants to open asks substanceAt with its density; the answer is
 * solid (barrier), air, water or lava. Barriers form where neighbouring grid
 * cells disagree on fluid level and the pressure beats the density:
 *
 *   density + similarity * pressure > 0  ->  stays solid
 *
 * Cell status is a pure function of cell coordinates. The surface estimate is a
 * constant at sea level instead of Vanilla's terrain-coupled function, which
 * keeps the result identical no matter which chunk computes the cell first.
 */
public final class AquiferSampler {
    public enum Substance {
        SOLID,
        AIR,
        WATER,
        LAVA
    }

    private static final int X_SPACING = 16;
    private static final int Y_SPACING = 12;
    private static final int Z_SPACING = 16;
    private static final int X_JITTER = 10;
    private static final int Y_JITTER = 9;
    private static final int Z_JITTER = 10;

    private static final double SIMILARITY_RANGE = 25.0;

    /**
     * Finite like Vanilla's WAY_BELOW_MIN_Y: the sentinel enters the pressure
     * arithmetic, where Integer.MIN_VALUE overflows and randomizes the dams.
     */
    private static final int NOT_FLOODED = -32768;

    /** Flood thresholds at Vanilla's dry-land setting (floodedness factor 0). */
    private static final double FULL_FLOOD_THRESHOLD = 0.8;
    private static final double PARTIAL_FLOOD_THRESHOLD = 0.4;

    private static final int LAVA_MAX_LEVEL = -10;
    private static final double LAVA_CHANCE_THRESHOLD = 0.3;

    /** Sponge Perlin octaves are ~5x weaker than Vanilla's normalized noise. */
    private static final double NOISE_SCALE = 5.0;

    private static final double BARRIER_WAVELENGTH = 8.0;
    private static final double BARRIER_Y_SCALE = 0.5;
    private static final double FLOODEDNESS_WAVELENGTH = 128.0;
    private static final double FLOODEDNESS_Y_SCALE = 0.67;
    private static final double SPREAD_WAVELENGTH = 32.0;
    private static final double SPREAD_XZ_Y_SCALE = 0.7142857142857143;
    private static final double LAVA_WAVELENGTH = 2.0;

    private static final int FLUID_CELL_XZ = 16;
    private static final int FLUID_CELL_Y = 40;
    private static final int LAVA_CELL_XZ = 64;
    private static final int LAVA_CELL_Y = 40;

    private final Perlin barrierNoise;
    private final Perlin floodednessNoise;
    private final Perlin spreadNoise;
    private final Perlin lavaNoise;

    private final long worldSeed;
    private final int lavaLevel;
    private final int seaLevel;
    private final int bandMinY;
    private final int bandMaxY;

    private long[] locationCache;
    private int[] levelCache;
    private boolean[] lavaCache;
    private boolean[] cacheValid;

    private int minGridX, minGridY, minGridZ;
    private int gridSizeX, gridSizeY, gridSizeZ;

    public AquiferSampler(long seed, int lavaLevel, int seaLevel, int bandMinY, int bandMaxY) {
        this.worldSeed = seed;
        this.lavaLevel = lavaLevel;
        this.seaLevel = seaLevel;
        this.bandMinY = bandMinY;
        this.bandMaxY = bandMaxY;

        this.barrierNoise = createNoise((int) seed + 10000, 1.0 / BARRIER_WAVELENGTH);
        this.floodednessNoise = createNoise((int) seed + 10001, 1.0 / FLOODEDNESS_WAVELENGTH);
        this.spreadNoise = createNoise((int) seed + 10002, 1.0 / SPREAD_WAVELENGTH);
        this.lavaNoise = createNoise((int) seed + 10003, 1.0 / LAVA_WAVELENGTH);
    }

    private static Perlin createNoise(int seed, double frequency) {
        Perlin perlin = new Perlin();
        perlin.setSeed(seed);
        perlin.setOctaveCount(1);
        perlin.setFrequency(frequency);

        return perlin;
    }

    public void prepare(int chunkX, int chunkZ) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;

        this.minGridX = gridXZ(worldX - 5);
        int maxGridX = gridXZ(worldX + 15 - 5) + 1;
        this.gridSizeX = maxGridX - minGridX + 1;

        this.minGridY = gridY(bandMinY + 1) - 1;
        int maxGridY = gridY(bandMaxY + 1) + 1;
        this.gridSizeY = maxGridY - minGridY + 1;

        this.minGridZ = gridXZ(worldZ - 5);
        int maxGridZ = gridXZ(worldZ + 15 - 5) + 1;
        this.gridSizeZ = maxGridZ - minGridZ + 1;

        int cacheSize = gridSizeX * gridSizeY * gridSizeZ;

        if (locationCache == null || locationCache.length < cacheSize) {
            this.locationCache = new long[cacheSize];
            this.levelCache = new int[cacheSize];
            this.lavaCache = new boolean[cacheSize];
            this.cacheValid = new boolean[cacheSize];
        } else {
            Arrays.fill(cacheValid, 0, cacheSize, false);
        }
    }

    public Substance substanceAt(int x, int y, int z, double density) {
        if (density > 0.0) {
            return Substance.SOLID;
        }

        if (y < Math.min(lavaLevel, seaLevel)) {
            return Substance.LAVA;
        }

        int anchorX = gridXZ(x - 5);
        int anchorY = gridY(y + 1);
        int anchorZ = gridXZ(z - 5);

        int dist1 = Integer.MAX_VALUE, dist2 = Integer.MAX_VALUE, dist3 = Integer.MAX_VALUE;
        int idx1 = -1, idx2 = -1, idx3 = -1;

        for (int gx = 0; gx <= 1; gx++) {
            for (int gy = -1; gy <= 1; gy++) {
                for (int gz = 0; gz <= 1; gz++) {
                    int index = cellIndex(anchorX + gx, anchorY + gy, anchorZ + gz);

                    if (index < 0) {
                        continue;
                    }

                    long location = cellLocation(anchorX + gx, anchorY + gy, anchorZ + gz, index);

                    int dx = unpackX(location) - x;
                    int dy = unpackY(location) - y;
                    int dz = unpackZ(location) - z;
                    int distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq <= dist1) {
                        dist3 = dist2; idx3 = idx2;
                        dist2 = dist1; idx2 = idx1;
                        dist1 = distSq; idx1 = index;
                    } else if (distSq <= dist2) {
                        dist3 = dist2; idx3 = idx2;
                        dist2 = distSq; idx2 = index;
                    } else if (distSq <= dist3) {
                        dist3 = distSq; idx3 = index;
                    }
                }
            }
        }

        if (idx1 < 0) {
            return Substance.AIR;
        }

        Substance fluid1 = fluidAt(idx1, y);
        double similarity12 = similarity(dist1, dist2);

        if (similarity12 <= 0.0) {
            return fluid1;
        }

        if (fluid1 == Substance.WATER && y - 1 < Math.min(lavaLevel, seaLevel)) {
            return Substance.WATER;
        }

        double[] barrierHolder = {Double.NaN};

        double pressure12 = similarity12 * pressure(x, y, z, idx1, idx2, barrierHolder);

        if (density + pressure12 > 0.0) {
            return Substance.SOLID;
        }

        if (idx3 >= 0) {
            double similarity13 = similarity(dist1, dist3);

            if (similarity13 > 0.0
                    && density + similarity12 * similarity13 * pressure(x, y, z, idx1, idx3, barrierHolder) > 0.0) {
                return Substance.SOLID;
            }

            double similarity23 = similarity(dist2, dist3);

            if (similarity23 > 0.0
                    && density + similarity12 * similarity23 * pressure(x, y, z, idx2, idx3, barrierHolder) > 0.0) {
                return Substance.SOLID;
            }
        }

        return fluid1;
    }

    private Substance fluidAt(int index, int y) {
        int level = levelCache[index];

        if (level == NOT_FLOODED || y >= level) {
            return Substance.AIR;
        }

        return lavaCache[index] ? Substance.LAVA : Substance.WATER;
    }

    private static double similarity(int distSq1, int distSq2) {
        return 1.0 - (double) (distSq2 - distSq1) / SIMILARITY_RANGE;
    }

    private double pressure(int x, int y, int z, int indexA, int indexB, double[] barrierHolder) {
        Substance typeA = fluidAt(indexA, y);
        Substance typeB = fluidAt(indexB, y);

        if ((typeA == Substance.LAVA && typeB == Substance.WATER)
                || (typeA == Substance.WATER && typeB == Substance.LAVA)) {
            return 2.0;
        }

        // Vanilla lets barrier noise punch holes in dams and heals them with
        // scheduled fluid ticks. Placed primer blocks never tick, so wherever
        // one side is fluid and the other air the dam must be airtight.
        if ((typeA == Substance.AIR) != (typeB == Substance.AIR)) {
            return 2.0;
        }

        int levelA = levelCache[indexA];
        int levelB = levelCache[indexB];
        int levelDiff = Math.abs(levelA - levelB);

        if (levelDiff == 0) {
            return 0.0;
        }

        double averageLevel = 0.5 * (levelA + levelB);
        double aboveAverage = y + 0.5 - averageLevel;
        double edgeDistance = levelDiff / 2.0 - Math.abs(aboveAverage);

        double gradient;

        if (aboveAverage > 0.0) {
            gradient = edgeDistance > 0.0 ? edgeDistance / 1.5 : edgeDistance / 2.5;
        } else {
            double fromBottom = 3.0 + edgeDistance;
            gradient = fromBottom > 0.0 ? fromBottom / 3.0 : fromBottom / 10.0;
        }

        double noise = 0.0;

        if (gradient >= -2.0 && gradient <= 2.0) {
            if (Double.isNaN(barrierHolder[0])) {
                barrierHolder[0] = barrierNoise.getValue(x, y * BARRIER_Y_SCALE, z) * NOISE_SCALE;
            }

            noise = barrierHolder[0];
        }

        return 2.0 * (noise + gradient);
    }

    private long cellLocation(int gridX, int gridY, int gridZ, int index) {
        if (cacheValid[index]) {
            return locationCache[index];
        }

        Random cellRandom = new Random(hashCell(gridX, gridY, gridZ, worldSeed));

        int centerX = gridX * X_SPACING + cellRandom.nextInt(X_JITTER);
        int centerY = gridY * Y_SPACING + cellRandom.nextInt(Y_JITTER);
        int centerZ = gridZ * Z_SPACING + cellRandom.nextInt(Z_JITTER);

        long location = packPos(centerX, centerY, centerZ);
        CellStatus status = computeCellStatus(centerX, centerY, centerZ);

        locationCache[index] = location;
        levelCache[index] = status.level();
        lavaCache[index] = status.lava();
        cacheValid[index] = true;

        return location;
    }

    /** Pure function of the cell center, so every chunk agrees on the result. */
    public CellStatus computeCellStatus(int centerX, int centerY, int centerZ) {
        int globalLevel = centerY < Math.min(lavaLevel, seaLevel) ? lavaLevel : seaLevel;
        boolean globalLava = centerY < Math.min(lavaLevel, seaLevel);

        double floodedness = clamp(floodednessNoise.getValue(
                centerX, centerY * FLOODEDNESS_Y_SCALE, centerZ) * NOISE_SCALE, -1.0, 1.0);

        int level;

        if (floodedness > FULL_FLOOD_THRESHOLD) {
            level = globalLevel;
        } else if (floodedness > PARTIAL_FLOOD_THRESHOLD) {
            level = randomizedLevel(centerX, centerY, centerZ);
        } else {
            level = NOT_FLOODED;
        }

        boolean lava = globalLava;

        if (!lava && level != NOT_FLOODED && level <= LAVA_MAX_LEVEL) {
            double lavaValue = lavaNoise.getValue(
                    Math.floorDiv(centerX, LAVA_CELL_XZ),
                    Math.floorDiv(centerY, LAVA_CELL_Y),
                    Math.floorDiv(centerZ, LAVA_CELL_XZ)) * NOISE_SCALE;
            lava = Math.abs(lavaValue) > LAVA_CHANCE_THRESHOLD;
        }

        return new CellStatus(level, lava);
    }

    private int randomizedLevel(int x, int y, int z) {
        int cellX = Math.floorDiv(x, FLUID_CELL_XZ);
        int cellY = Math.floorDiv(y, FLUID_CELL_Y);
        int cellZ = Math.floorDiv(z, FLUID_CELL_XZ);

        double spread = spreadNoise.getValue(
                cellX * SPREAD_XZ_Y_SCALE, cellY * SPREAD_XZ_Y_SCALE, cellZ * SPREAD_XZ_Y_SCALE)
                * NOISE_SCALE * 10.0;

        int target = cellY * FLUID_CELL_Y + FLUID_CELL_Y / 2 + quantize(spread, 3);

        return Math.min(seaLevel, target);
    }

    public record CellStatus(int level, boolean lava) {
    }

    private static int gridXZ(int blockCoord) {
        return blockCoord >> 4;
    }

    private static int gridY(int blockCoord) {
        return Math.floorDiv(blockCoord, Y_SPACING);
    }

    private int cellIndex(int gridX, int gridY, int gridZ) {
        int x = gridX - minGridX;
        int y = gridY - minGridY;
        int z = gridZ - minGridZ;

        if (x < 0 || y < 0 || z < 0 || x >= gridSizeX || y >= gridSizeY || z >= gridSizeZ) {
            return -1;
        }

        return (y * gridSizeZ + z) * gridSizeX + x;
    }

    private static long packPos(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) << 26 | ((long) z & 0x3FFFFFFL);
    }

    private static int unpackX(long packed) {
        return (int) (packed >> 38);
    }

    private static int unpackY(long packed) {
        return (int) (packed << 26 >> 52);
    }

    private static int unpackZ(long packed) {
        return (int) (packed << 38 >> 38);
    }

    private static long hashCell(int gx, int gy, int gz, long seed) {
        long hash = seed;
        hash = hash * 6364136223846793005L + 1442695040888963407L;
        hash += gx;
        hash = hash * 6364136223846793005L + 1442695040888963407L;
        hash += gy;
        hash = hash * 6364136223846793005L + 1442695040888963407L;
        hash += gz;
        hash = hash * 6364136223846793005L + 1442695040888963407L;

        return hash;
    }

    private static int quantize(double value, int step) {
        return (int) Math.floor(value / step) * step;
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }
}
