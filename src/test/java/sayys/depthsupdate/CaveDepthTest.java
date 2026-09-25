package sayys.depthsupdate;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import sayys.depthsupdate.world.generation.noise.CaveEntranceNoise;
import sayys.depthsupdate.world.generation.noise.CheeseCaveNoise;
import sayys.depthsupdate.world.generation.noise.DensityField;
import sayys.depthsupdate.world.generation.noise.NoodleCaveNoise;
import sayys.depthsupdate.world.generation.noise.SpaghettiCaveNoise;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

/**
 * The four cave noises min-composed the way CaveNoiseGenerator composes them,
 * over synthetic rolling terrain. Guards the invariants that broke before:
 * every column keeps an opaque block (a transparent one drives the chunk
 * heightmap below zero and crashes vanilla decorators), caves follow terrain
 * upward instead of stopping at an absolute ceiling, and flat ground stays
 * essentially intact.
 */
class CaveDepthTest {
    /**
     * Openings are rare events, roughly one flare per 144 chunks, so the
     * sample has to be large enough that their absence is a finding rather
     * than noise.
     */
    private static final long[] SEEDS = {0L, 12345L, -98765L, 777L, 8675309L};
    private static final int CHUNKS = 8;
    private static final int WIDTH = CHUNKS * 16;

    private static final int MIN_Y = -60;
    private static final int TERRAIN_MAX_Y = 255;

    /** Above this the old absolute ceiling left no caves at all. */
    private static final int OLD_CEILING_Y = 30;

    @Test
    void composedCavesFollowTerrainAndLeaveGround() {
        long transparentColumns = 0;
        long highStone = 0, highCarved = 0;
        long flatColumns = 0, flatHoles = 0;

        for (long seed : SEEDS) {
            Random random = new Random(seed);
            double offsetX = random.nextDouble() * 100000.0;
            double offsetY = random.nextDouble() * 100000.0;
            double offsetZ = random.nextDouble() * 100000.0;

            CheeseCaveNoise cheeseNoise = new CheeseCaveNoise(seed, offsetX, offsetY, offsetZ, 1.0);
            DensityField cheese = new DensityField(cheeseNoise::density, MIN_Y, TERRAIN_MAX_Y);

            CaveEntranceNoise blobNoise = new CaveEntranceNoise(seed, offsetX, offsetY, offsetZ);
            DensityField blob = new DensityField(blobNoise::density, MIN_Y, TERRAIN_MAX_Y);

            SpaghettiCaveNoise spaghetti = new SpaghettiCaveNoise(seed);
            NoodleCaveNoise noodle = new NoodleCaveNoise(seed);
            DensityField noodleSelector = new DensityField(noodle::selector, MIN_Y, TERRAIN_MAX_Y);

            int[] surface = terrain(seed);
            boolean[][] carvedColumns = new boolean[WIDTH * WIDTH][];

            for (int chunkX = 0; chunkX < CHUNKS; chunkX++) {
                for (int chunkZ = 0; chunkZ < CHUNKS; chunkZ++) {
                    int highest = MIN_Y;

                    for (int localX = 0; localX < 16; localX++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            highest = Math.max(highest,
                                    surface[(chunkX * 16 + localX) * WIDTH + chunkZ * 16 + localZ]);
                        }
                    }

                    cheese.prepare(chunkX, chunkZ, highest);
                    blob.prepare(chunkX, chunkZ, highest);
                    noodleSelector.prepare(chunkX, chunkZ, highest);

                    for (int localX = 0; localX < 16; localX++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            int gx = chunkX * 16 + localX;
                            int gz = chunkZ * 16 + localZ;
                            int top = surface[gx * WIDTH + gz];

                            boolean[] carved = new boolean[top - MIN_Y + 1];
                            boolean solidSomewhere = false;

                            for (int y = MIN_Y; y <= top; y++) {
                                int depth = top - y;
                                double realX = gx + offsetX;
                                double realY = y + offsetY;
                                double realZ = gz + offsetZ;

                                double density = cheese.get(localX, y, localZ)
                                        + CheeseCaveNoise.surfaceSlide(depth);

                                double fade = SpaghettiCaveNoise.thicknessFade(depth);

                                if (fade > 0.0) {
                                    density = Math.min(density, spaghetti.ridge(realX, realY, realZ)
                                            - SpaghettiCaveNoise.THICKNESS * fade);
                                }

                                if (noodleSelector.get(localX, y, localZ) >= 0.0) {
                                    density = Math.min(density, noodle.density(realX, realY, realZ, depth));
                                }

                                if (!CaveEntranceNoise.closedAtDepth(depth)) {
                                    density = Math.min(density, blob.get(localX, y, localZ)
                                            + CaveEntranceNoise.surfaceSlide(depth));
                                }

                                boolean open = density < 0.0;
                                carved[y - MIN_Y] = open;

                                if (!open) {
                                    solidSomewhere = true;
                                }

                                if (y > OLD_CEILING_Y) {
                                    highStone++;

                                    if (open) {
                                        highCarved++;
                                    }
                                }
                            }

                            carvedColumns[gx * WIDTH + gz] = carved;

                            if (!solidSomewhere) {
                                transparentColumns++;
                            }
                        }
                    }
                }
            }

            for (int gx = 2; gx < WIDTH - 2; gx++) {
                for (int gz = 2; gz < WIDTH - 2; gz++) {
                    int top = surface[gx * WIDTH + gz];
                    boolean topOpen = carvedColumns[gx * WIDTH + gz][top - MIN_Y];

                    if (isFlat(surface, gx, gz)) {
                        flatColumns++;

                        if (topOpen) {
                            flatHoles++;
                        }
                    }
                }
            }
        }

        double highFraction = 100.0 * highCarved / highStone;
        double flatHoleFraction = flatColumns == 0 ? 0.0 : 100.0 * flatHoles / flatColumns;

        System.out.printf("rock above y=%d carved %.2f%% | flat holes %.3f%%%n",
                OLD_CEILING_Y, highFraction, flatHoleFraction);

        Assertions.assertEquals(0, transparentColumns,
                "columns with no opaque block drive the heightmap below zero and crash vanilla decorators");

        Assertions.assertTrue(highFraction > 2.0,
                () -> "only %.2f%% of rock above y=%d is carved, so caves are not following the terrain"
                        .formatted(highFraction, OLD_CEILING_Y));

        Assertions.assertTrue(flatHoleFraction < 2.0,
                () -> "%.2f%% of flat ground opens into caves, which reads as a perforated surface"
                        .formatted(flatHoleFraction));
    }

    /**
     * Openings are rare and heavily clustered by seed region, so a composed
     * sample cannot assert their presence without flaking. The blob noise is
     * checked directly over a wide fixed sample instead: some columns must
     * open at the surface, but never so many that the ground reads as
     * perforated. Measured 0.23 percent over these seeds.
     */
    private static final long[] BLOB_SEEDS = {0L, 12345L, -98765L, 777L, 8675309L, 42L, -1L, 999331L,
            31337L, 2026L, -777777L, 555L};

    @Test
    void entranceFlaresOpenTheSurfaceAtTheExpectedRarity() {
        long open = 0;
        long columns = 0;

        for (long seed : BLOB_SEEDS) {
            Random random = new Random(seed);
            double offsetX = random.nextDouble() * 100000.0;
            double offsetY = random.nextDouble() * 100000.0;
            double offsetZ = random.nextDouble() * 100000.0;

            CaveEntranceNoise blob = new CaveEntranceNoise(seed, offsetX, offsetY, offsetZ);
            int[] surface = terrain(seed);

            for (int x = 0; x < WIDTH; x++) {
                for (int z = 0; z < WIDTH; z++) {
                    columns++;

                    if (blob.density(x, surface[x * WIDTH + z], z) + CaveEntranceNoise.surfaceSlide(0) < 0.0) {
                        open++;
                    }
                }
            }
        }

        double fraction = 100.0 * open / columns;

        System.out.printf("blob surface openings %.3f%% of columns%n", fraction);

        Assertions.assertTrue(fraction > 0.02,
                () -> "%.3f%% of columns open at the surface, so entrance flares have effectively vanished"
                        .formatted(fraction));
        Assertions.assertTrue(fraction < 3.0,
                () -> "%.3f%% of columns open at the surface, which reads as holes in every hillside"
                        .formatted(fraction));
    }

    /** Rolling hills spanning roughly y=40 to y=110, so terrain sits both sides of the old ceiling. */
    private static int[] terrain(long seed) {
        Perlin broad = new Perlin();
        broad.setSeed((int) seed + 991);
        broad.setOctaveCount(3);
        broad.setFrequency(1.0 / 180.0);

        Perlin rough = new Perlin();
        rough.setSeed((int) seed + 992);
        rough.setOctaveCount(2);
        rough.setFrequency(1.0 / 24.0);

        int[] surface = new int[WIDTH * WIDTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < WIDTH; z++) {
                double broadValue = Math.clamp(broad.getValue(x, 0.5, z) * 5.0, -1.0, 1.0);
                double roughValue = Math.clamp(rough.getValue(x, 0.5, z) * 5.0, -1.0, 1.0);
                surface[x * WIDTH + z] = (int) Math.round(75 + 45 * broadValue + 7 * roughValue);
            }
        }

        return surface;
    }

    private static boolean isFlat(int[] surface, int gx, int gz) {
        int h = surface[gx * WIDTH + gz];

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(surface[(gx + dx) * WIDTH + gz + dz] - h) > 2) {
                    return false;
                }
            }
        }

        return true;
    }
}
