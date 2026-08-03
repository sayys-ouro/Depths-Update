package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

/**
 * Port of vanilla's noodle caves: thin twisting worms carved where two ridge
 * noises are both near zero, gated off entirely in half the world by a slow
 * selector noise. Thresholds are vanilla's scaled by the measured strength of
 * this Perlin (single octave runs near [-0.2, 0.2], not [-1, 1]).
 */
public final class NoodleCaveNoise {
    private static final double SELECTOR_WAVELENGTH = 256.0;
    private static final double THICKNESS_WAVELENGTH = 256.0;
    /** Vanilla samples its ridge pair at 2.6667x of a 128-block noise. */
    private static final double RIDGE_WAVELENGTH = 48.0;

    private static final double RIDGE_WEIGHT = 1.5;

    /** Vanilla maps thickness into [-0.1, -0.05]; ours is that times ~0.2. */
    private static final double THICKNESS_BASE = -0.015;
    private static final double THICKNESS_SPREAD = 0.025;
    private static final double THICKNESS_MIN = -0.02;
    private static final double THICKNESS_MAX = -0.01;

    /** Full thickness below this depth, closed above FADE_TO_DEPTH. */
    private static final int FADE_FROM_DEPTH = 6;
    private static final int FADE_TO_DEPTH = 2;

    private final Perlin selectorNoise;
    private final Perlin thicknessNoise;
    private final Perlin ridgeA;
    private final Perlin ridgeB;

    public NoodleCaveNoise(long seed) {
        this.selectorNoise = createNoise((int) seed + 601, SELECTOR_WAVELENGTH);
        this.thicknessNoise = createNoise((int) seed + 602, THICKNESS_WAVELENGTH);
        this.ridgeA = createNoise((int) seed + 603, RIDGE_WAVELENGTH);
        this.ridgeB = createNoise((int) seed + 604, RIDGE_WAVELENGTH);
    }

    private static Perlin createNoise(int seed, double wavelength) {
        Perlin perlin = new Perlin();
        perlin.setSeed(seed);
        perlin.setOctaveCount(1);
        perlin.setFrequency(1.0 / wavelength);

        return perlin;
    }

    /** Noodles exist only where this is non-negative. */
    public double selector(double x, int y, double z) {
        return this.selectorNoise.getValue(x, y, z);
    }

    public double density(double x, double y, double z, int depth) {
        double fade = thicknessFade(depth);

        if (fade <= 0.0) {
            return RIDGE_WEIGHT;
        }

        double thickness = Math.clamp(
                THICKNESS_BASE + this.thicknessNoise.getValue(x, y, z) * THICKNESS_SPREAD,
                THICKNESS_MIN, THICKNESS_MAX) * fade;

        double ridge = Math.max(
                Math.abs(this.ridgeA.getValue(x, y, z)),
                Math.abs(this.ridgeB.getValue(x, y, z)));

        return RIDGE_WEIGHT * ridge + thickness;
    }

    private static double thicknessFade(int depth) {
        if (depth >= FADE_FROM_DEPTH) {
            return 1.0;
        }

        if (depth <= FADE_TO_DEPTH) {
            return 0.0;
        }

        return (depth - FADE_TO_DEPTH) / (double) (FADE_FROM_DEPTH - FADE_TO_DEPTH);
    }
}
