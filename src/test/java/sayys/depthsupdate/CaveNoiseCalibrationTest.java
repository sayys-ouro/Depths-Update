package sayys.depthsupdate;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import sayys.depthsupdate.world.generation.noise.AquiferSampler;
import sayys.depthsupdate.world.generation.noise.CheeseCaveNoise;
import sayys.depthsupdate.world.generation.noise.DensityField;
import sayys.depthsupdate.world.generation.noise.PillarNoise;

/**
 * Carves a block of world with the production cave noise and reports the shape
 * statistics its constants produce, so they can be calibrated against measured
 * carved volume and cave proportions instead of guessed. Run with:
 * gradlew test --tests sayys.depthsupdate.CaveNoiseCalibrationTest
 *
 * Targets taken from Vanilla cheese caves: roughly 6 to 12 percent of the deep
 * cave band carved, chambers a little wider than tall, and pillars occupying a
 * couple of percent of the volume in columns several blocks across.
 *
 * The band once held a slice the old absolute top slide suppressed, so the
 * figure it reports rose when caves moved to depth-relative placement. Density
 * below y=10 is unchanged: the old slide only applied above it.
 */
class CaveNoiseCalibrationTest {
    private static final int CHUNKS = 16;
    private static final int SIZE = CHUNKS * 16;
    private static final int CAVE_MIN_Y = -60;
    private static final int CAVE_MAX_Y = 30;
    private static final int HEIGHT = CAVE_MAX_Y - CAVE_MIN_Y + 1;

    @Test
    void reportCaveShapeStatistics() {
        long seed = 123456789L;

        CheeseCaveNoise cheeseNoise = new CheeseCaveNoise(seed, 31.7, 57.3, 12.9, 1.0);
        PillarNoise pillarNoise = new PillarNoise(seed, 31.7, 57.3, 12.9);

        DensityField cheeseField = new DensityField(cheeseNoise::density, CAVE_MIN_Y, CAVE_MAX_Y);
        DensityField pillarField = new DensityField(pillarNoise::density, CAVE_MIN_Y, CAVE_MAX_Y);

        boolean[] carved = new boolean[SIZE * HEIGHT * SIZE];
        boolean[] pillars = new boolean[SIZE * HEIGHT * SIZE];

        int cheeseHits = 0;
        int pillarHits = 0;
        int blocked = 0;

        for (int chunkX = 0; chunkX < CHUNKS; chunkX++) {
            for (int chunkZ = 0; chunkZ < CHUNKS; chunkZ++) {
                cheeseField.prepare(chunkX, chunkZ);
                pillarField.prepare(chunkX, chunkZ);

                for (int localX = 0; localX < 16; localX++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        for (int y = CAVE_MIN_Y; y <= CAVE_MAX_Y; y++) {
                            boolean cheese = cheeseField.get(localX, y, localZ) < 0.0;
                            boolean pillar = PillarNoise.isSolid(pillarField.get(localX, y, localZ));

                            if (cheese) cheeseHits++;
                            if (pillar) pillarHits++;
                            if (cheese && pillar) blocked++;

                            int index = index(chunkX * 16 + localX, y, chunkZ * 16 + localZ);
                            carved[index] = cheese && !pillar;
                            pillars[index] = pillar;
                        }
                    }
                }
            }
        }

        int total = SIZE * HEIGHT * SIZE;

        System.out.println("=== carved volume ===");
        System.out.printf(
            "cheese %.2f%% | pillars %.2f%% | pillars blocking cheese %.2f%% of cheese%n",
            100.0 * cheeseHits / total,
            100.0 * pillarHits / total,
            cheeseHits == 0 ? 0.0 : 100.0 * blocked / cheeseHits
        );

        RunStats caveVertical = verticalRuns(carved);
        RunStats caveHorizontal = horizontalRuns(carved);
        RunStats pillarWidthRuns = horizontalRuns(pillars);
        RunStats pillarHeightRuns = verticalRuns(pillars);

        System.out.println("=== cave proportions (run lengths in blocks) ===");
        System.out.printf(
            "vertical mean %.2f p95 %d | horizontal mean %.2f p95 %d | mean ratio %.2f%n",
            caveVertical.mean(),
            caveVertical.p95(),
            caveHorizontal.mean(),
            caveHorizontal.p95(),
            caveVertical.mean() / Math.max(0.01, caveHorizontal.mean())
        );
        System.out.printf(
            "chimney columns (air run of 48 or more) %.2f%%%n",
            100.0 * caveVertical.tallColumnFraction()
        );

        System.out.println("=== pillar proportions (run lengths in blocks) ===");
        System.out.printf(
            "width mean %.2f p95 %d | height mean %.2f%n",
            pillarWidthRuns.mean(),
            pillarWidthRuns.p95(),
            pillarHeightRuns.mean()
        );

        System.out.println("=== carve profile by height (percent of layer) ===");

        for (int y : new int[] {30, 27, 24, 20, 15, 10, 0, -20, -40, -59}) {
            long air = 0;

            for (int x = 0; x < SIZE; x++) {
                for (int z = 0; z < SIZE; z++) {
                    if (carved[index(x, y, z)]) air++;
                }
            }

            System.out.printf("y %4d: %5.2f%%%n", y, 100.0 * air / (SIZE * SIZE));
        }

        double cheeseVolume = 100.0 * cheeseHits / total;
        double pillarVolume = 100.0 * pillarHits / total;

        reportAquiferStatistics(seed, cheeseField);

        assertBetween("cheese volume", cheeseVolume, 4.0, 12.0);
        assertBetween("cave height to width", caveVertical.mean() / caveHorizontal.mean(), 0.5, 1.4);
        assertBetween("cave vertical p95", caveVertical.p95(), 10, 40);
        // Runs used to stop where the absolute top slide shut caves off at y=10,
        // so they now reach the top of the band. What this guards against is a
        // vertical shaft breaking daylight, which CaveDepthTest measures directly.
        assertBetween("chimney column fraction", caveVertical.tallColumnFraction(), 0.0, 0.012);
        assertBetween("pillar volume", pillarVolume, 0.5, 4.0);
        assertBetween("pillar width", pillarWidthRuns.mean(), 3.0, 8.0);
    }

    /**
     * Runs the aquifer over every carved cheese block and reports what fraction
     * turns into water, lava or barrier stone. Also asserts cell status purity,
     * which is what prevents water walls at chunk seams.
     */
    private void reportAquiferStatistics(long seed, DensityField cheeseField) {
        AquiferSampler aquifer = new AquiferSampler(seed, -54, 63, CAVE_MIN_Y, CAVE_MAX_Y);

        // 0 = uncarved, 1 = air, 2 = water, 3 = lava, 4 = barrier
        byte[] substances = new byte[SIZE * HEIGHT * SIZE];
        long air = 0, waterBlocks = 0, lavaBlocks = 0, barrier = 0;

        for (int chunkX = 0; chunkX < CHUNKS; chunkX++) {
            for (int chunkZ = 0; chunkZ < CHUNKS; chunkZ++) {
                cheeseField.prepare(chunkX, chunkZ);
                aquifer.prepare(chunkX, chunkZ);

                for (int localX = 0; localX < 16; localX++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        for (int y = CAVE_MIN_Y; y <= CAVE_MAX_Y; y++) {
                            double density = cheeseField.get(localX, y, localZ);

                            if (density >= 0.0) {
                                continue;
                            }

                            int index = index(chunkX * 16 + localX, y, chunkZ * 16 + localZ);

                            switch (aquifer.substanceAt(chunkX * 16 + localX, y, chunkZ * 16 + localZ, density)) {
                                case AIR -> { air++; substances[index] = 1; }
                                case WATER -> { waterBlocks++; substances[index] = 2; }
                                case LAVA -> { lavaBlocks++; substances[index] = 3; }
                                case SOLID -> { barrier++; substances[index] = 4; }
                            }
                        }
                    }
                }
            }
        }

        long carvedTotal = air + waterBlocks + lavaBlocks + barrier;

        System.out.println("=== aquifer over carved cheese volume ===");
        System.out.printf("air %.2f%% | water %.2f%% | lava %.2f%% | barrier %.2f%%%n",
                100.0 * air / carvedTotal, 100.0 * waterBlocks / carvedTotal,
                100.0 * lavaBlocks / carvedTotal, 100.0 * barrier / carvedTotal);

        // Fluid faces exposed to air sideways or below read as broken ponds.
        long exposed = 0;

        for (int x = 1; x < SIZE - 1; x++) {
            for (int z = 1; z < SIZE - 1; z++) {
                for (int y = CAVE_MIN_Y + 1; y <= CAVE_MAX_Y; y++) {
                    byte here = substances[index(x, y, z)];

                    if (here != 2 && here != 3) {
                        continue;
                    }

                    if (substances[index(x + 1, y, z)] == 1 || substances[index(x - 1, y, z)] == 1
                            || substances[index(x, y, z + 1)] == 1 || substances[index(x, y, z - 1)] == 1
                            || substances[index(x, y - 1, z)] == 1) {
                        exposed++;
                    }
                }
            }
        }

        long fluidTotal = waterBlocks + lavaBlocks;
        System.out.printf("exposed fluid faces: %d of %d fluid blocks (%.3f%%)%n",
                exposed, fluidTotal, fluidTotal == 0 ? 0.0 : 100.0 * exposed / fluidTotal);

        assertBetween("aquifer water fraction", 100.0 * waterBlocks / carvedTotal, 0.5, 40.0);
        assertBetween("aquifer barrier fraction", 100.0 * barrier / carvedTotal, 0.0, 15.0);
        assertBetween("exposed fluid fraction", fluidTotal == 0 ? 0.0 : 100.0 * exposed / fluidTotal, 0.0, 0.5);

        // Same block asked from two different chunk contexts must agree.
        aquifer.prepare(0, 0);
        AquiferSampler.Substance fromFirstChunk = aquifer.substanceAt(15, -30, 8, -0.4);
        aquifer.prepare(1, 0);
        AquiferSampler.Substance fromSecondChunk = aquifer.substanceAt(15, -30, 8, -0.4);

        Assertions.assertEquals(fromFirstChunk, fromSecondChunk, "aquifer must be chunk independent");

        AquiferSampler.CellStatus status = aquifer.computeCellStatus(100, -40, 100);
        Assertions.assertEquals(status, aquifer.computeCellStatus(100, -40, 100), "cell status must be pure");
    }

    private record RunStats(double mean, int p95, double tallColumnFraction) {}

    private static void assertBetween(String name, double value, double min, double max) {
        Assertions.assertTrue(value >= min && value <= max,
                () -> "%s is %.2f, outside the calibrated range %.2f to %.2f".formatted(name, value, min, max));
    }

    private static int index(int x, int y, int z) {
        return ((y - CAVE_MIN_Y) * SIZE + z) * SIZE + x;
    }

    private static final int TALL_RUN = 48;

    private static RunStats verticalRuns(boolean[] volume) {
        List<Integer> runs = new ArrayList<>();
        int tallColumns = 0;

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int run = 0;
                boolean tall = false;

                for (int y = CAVE_MIN_Y; y <= CAVE_MAX_Y + 1; y++) {
                    boolean current = y <= CAVE_MAX_Y && volume[index(x, y, z)];
                    if (current) {
                        run++;
                    } else if (run > 0) {
                        runs.add(run);
                        if (run >= TALL_RUN) tall = true;
                        run = 0;
                    }
                }

                if (tall) tallColumns++;
            }
        }

        return toStats(runs, (double) tallColumns / (SIZE * SIZE));
    }

    private static RunStats horizontalRuns(boolean[] volume) {
        List<Integer> runs = new ArrayList<>();

        for (int y = CAVE_MIN_Y; y <= CAVE_MAX_Y; y++) {
            for (int z = 0; z < SIZE; z++) {
                int run = 0;

                for (int x = 0; x <= SIZE; x++) {
                    boolean current = x < SIZE && volume[index(x, y, z)];
                    if (current) {
                        run++;
                    } else if (run > 0) {
                        runs.add(run);
                        run = 0;
                    }
                }
            }
        }

        return toStats(runs, 0.0);
    }

    private static RunStats toStats(List<Integer> runs, double tallColumnFraction) {
        if (runs.isEmpty()) {
            return new RunStats(0.0, 0, tallColumnFraction);
        }

        runs.sort(null);
        double mean = runs.stream().mapToLong(Integer::longValue).sum() / (double) runs.size();
        int p95 = runs.get(Math.min(runs.size() - 1, (int) (runs.size() * 0.95)));

        return new RunStats(mean, p95, tallColumnFraction);
    }
}
