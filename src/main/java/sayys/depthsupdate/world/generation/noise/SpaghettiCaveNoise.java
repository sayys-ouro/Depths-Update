package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

/**
 * Winding tunnels carved where two independent noises are both near zero,
 * the role vanilla's spaghetti_3d plays inside its entrances function. Runs
 * the full height of the rock; {@link #thicknessFade(int)} thins tunnels as
 * they near the surface so mouths form on slopes rather than as potholes.
 */
public final class SpaghettiCaveNoise {
    private static final double SCALE = 0.035;
    public static final double THICKNESS = 0.025;

    /**
     * Full thickness below this depth, closed above FADE_TO_DEPTH. Kept
     * shallow: tunnels near the surface are what entrance flares open into,
     * and hillside mouths come from slopes cutting into them sideways.
     */
    private static final int FADE_FROM_DEPTH = 8;
    private static final int FADE_TO_DEPTH = 2;

    private final Perlin noiseA;
    private final Perlin noiseB;

    public SpaghettiCaveNoise(long seed) {
        this.noiseA = new Perlin();
        this.noiseA.setSeed((int) seed + 1337);
        this.noiseA.setOctaveCount(2);

        this.noiseB = new Perlin();
        this.noiseB.setSeed((int) seed + 7331);
        this.noiseB.setOctaveCount(2);
    }

    /** Distance from the nearer tunnel spine; carve where below the faded thickness. */
    public double ridge(double x, double y, double z) {
        double a = this.noiseA.getValue(x * SCALE, y * SCALE, z * SCALE);
        double b = this.noiseB.getValue(x * SCALE, y * SCALE, z * SCALE);

        return Math.max(Math.abs(a), Math.abs(b));
    }

    public static double thicknessFade(int depth) {
        if (depth >= FADE_FROM_DEPTH) {
            return 1.0;
        }

        if (depth <= FADE_TO_DEPTH) {
            return 0.0;
        }

        return (depth - FADE_TO_DEPTH) / (double) (FADE_FROM_DEPTH - FADE_TO_DEPTH);
    }
}
