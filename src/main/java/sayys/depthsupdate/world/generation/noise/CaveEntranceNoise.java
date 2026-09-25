package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

/**
 * The blob half of vanilla's entrances function, which is
 * min(entrance_noise + 0.37 + gradient, spaghetti_3d): a rare wide flare near
 * the surface that opens the tunnel network into a cave mouth. The min against
 * the tunnels comes from the generator composition; this class is only the
 * flare.
 *
 * Vanilla bounds the flare with the terrain density it is added to, so it
 * cannot outlive the rock around it. We carve a finished primer instead, and
 * {@link #surfaceSlide(int)} stands in for that terrain term.
 */
public final class CaveEntranceNoise {
    /** Vanilla's constant offset on the entrance noise. */
    private static final double DENSITY_OFFSET = 0.37;

    /**
     * Depth below the column's surface over which a flare closes. Replaces
     * vanilla's absolute yClampedGradient, which assumes a terrain term we do
     * not have. Deep enough to overlap the tunnel network, which fades in at
     * depth 8; measured, half the flares open into it.
     */
    private static final int SURFACE_SLIDE_FROM_DEPTH = 16;
    private static final int SURFACE_SLIDE_TO_DEPTH = 56;
    private static final double SURFACE_SLIDE_MAX = 2.0;

    /**
     * Absolute backstop under the surface slide. Nothing should reach this far
     * down once depth is accounted for; it holds the invariant that every
     * column keeps an opaque block even if the surface estimate is ever wrong.
     */
    private static final int FLOOR_FROM_Y = 0;
    private static final int FLOOR_TO_Y = -30;
    private static final double FLOOR_MAX = 2.0;

    /**
     * Scales the raw Perlin sum into vanilla's roughly [-1, 1] range. The
     * sponge Perlin runs far weaker than its nominal amplitude, so this is a
     * measured value, not a derived one.
     */
    private static final double NOISE_NORMALIZER = 2.35;

    /**
     * Additional solidity applied everywhere, which is what keeps openings from
     * appearing on every slope. Raising it does not just make them rarer, it
     * makes them smaller, because a single noise thresholded further into its
     * tail shrinks in every dimension at once.
     *
     * Measured over 8 seeds against rolling terrain with the composed pipeline:
     * 0.95 gives an opening per 32 chunks with 1.6 percent of flat ground
     * broken; 1.15 one per 58 chunks at 0.4 percent, half opening into the
     * tunnel network.
     */
    private static final double RARITY_BIAS = 1.15;

    private static final double WAVELENGTH = 128.0;

    /**
     * Near-isotropic, so the opening is a three-dimensional hollow rather than
     * a tube. Vanilla additionally stretches xz by 0.75.
     */
    private static final double Y_SCALE = 0.5;

    private final Perlin noise;

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    public CaveEntranceNoise(long seed, double offsetX, double offsetY, double offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;

        // Vanilla's CAVE_ENTRANCE runs octaves 128/64/32 weighted 0.4/0.5/1.0.
        // That exact profile does not port: this Perlin's output is hard
        // bounded near 0.2 per octave with thin tails, and at entrance rarity
        // the carve threshold sits so deep in the tail that whole regions get
        // either no openings or hundreds (measured: 11 of 12 seeds at zero).
        // Persistence 2 over three octaves keeps the same wavelengths with a
        // wider bounded range, which spreads openings across seeds.
        this.noise = new Perlin();
        this.noise.setSeed((int) seed + 517);
        this.noise.setOctaveCount(3);
        this.noise.setPersistence(2.0);
        this.noise.setFrequency(1.0 / WAVELENGTH);
    }

    /** Applied by the generator, which is the only place the column's surface is known. */
    public static double surfaceSlide(int depth) {
        if (depth <= SURFACE_SLIDE_FROM_DEPTH) {
            return 0.0;
        }

        if (depth >= SURFACE_SLIDE_TO_DEPTH) {
            return SURFACE_SLIDE_MAX;
        }

        return SURFACE_SLIDE_MAX * (depth - SURFACE_SLIDE_FROM_DEPTH)
                / (double) (SURFACE_SLIDE_TO_DEPTH - SURFACE_SLIDE_FROM_DEPTH);
    }

    /** Past the full slide the noise cannot reach zero, so sampling is wasted work. */
    public static boolean closedAtDepth(int depth) {
        return depth >= SURFACE_SLIDE_TO_DEPTH;
    }

    private static double floor(int y) {
        if (y >= FLOOR_FROM_Y) {
            return 0.0;
        }

        if (y <= FLOOR_TO_Y) {
            return FLOOR_MAX;
        }

        return FLOOR_MAX * (FLOOR_FROM_Y - y) / (double) (FLOOR_FROM_Y - FLOOR_TO_Y);
    }

    public double density(double x, int y, double z) {
        double value = this.noise.getValue(
                x + this.offsetX,
                (y + this.offsetY) * Y_SCALE,
                z + this.offsetZ) * NOISE_NORMALIZER;

        return value + DENSITY_OFFSET + RARITY_BIAS + floor(y);
    }
}
