package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

/**
 * Vertical shafts that connect the cave systems to daylight.
 *
 * Modelled on vanilla's `entrances` density function, which is a term of its
 * own rather than a variation of the cheese noise:
 *
 * <pre>
 * add(caveEntranceNoise.add(0.37), yClampedGradient(-10, 30, 0.3, 0.0))
 * </pre>
 *
 * The gradient is the part that matters. It adds solidity low down and none
 * near the surface, so entrances grow *stronger* as they rise - the opposite of
 * the cheese top slide, and the reason vanilla gets cave mouths at all.
 *
 * Vanilla can afford to run this everywhere because its result is combined with
 * the density field that builds the terrain, so an entrance cannot carve where
 * there is no rock. We carve a finished primer instead, so the threshold here is
 * calibrated to keep entrances rare enough to read as shafts rather than as
 * holes in every hillside.
 */
public final class CaveEntranceNoise {
    /** Vanilla's constant offset on the entrance noise. */
    private static final double DENSITY_OFFSET = 0.37;

    /** yClampedGradient(-10, 30, 0.3, 0.0). */
    private static final int GRADIENT_FROM_Y = -10;
    private static final int GRADIENT_TO_Y = 30;
    private static final double GRADIENT_FROM = 0.3;

    /**
     * Scales the raw Perlin sum into vanilla's roughly [-1, 1] range. The
     * sponge Perlin runs far weaker than its nominal amplitude, so this is a
     * measured value, not a derived one.
     */
    private static final double NOISE_NORMALIZER = 2.35;

    /**
     * Additional solidity applied everywhere. Vanilla leans on the terrain
     * density to keep entrances scarce; lacking that, this is what stops them
     * from perforating every slope.
     *
     * This is the dial to turn. Measured carved fraction per horizontal slice:
     * at 0.95 roughly 1.9 blocks per chunk slice near the surface, at 1.05
     * roughly 1.1, at 1.15 roughly 0.5. Lower means more and wider mouths.
     */
    private static final double RARITY_BIAS = 1.05;

    private static final double WAVELENGTH = 128.0;
    /** Below one, so features stretch vertically into shafts. */
    private static final double Y_SCALE = 0.5;

    private final Perlin noise;

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    public CaveEntranceNoise(long seed, double offsetX, double offsetY, double offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;

        // Vanilla samples CAVE_ENTRANCE at first octave -7 with amplitudes
        // 0.4/0.5/1.0; persistence 2 over three octaves is the closest this
        // Perlin can express.
        this.noise = new Perlin();
        this.noise.setSeed((int) seed + 517);
        this.noise.setOctaveCount(3);
        this.noise.setPersistence(2.0);
        this.noise.setFrequency(1.0 / WAVELENGTH);
    }

    /** Vanilla's yClampedGradient: full solidity deep, none above the surface band. */
    private static double gradient(int y) {
        if (y <= GRADIENT_FROM_Y) {
            return GRADIENT_FROM;
        }

        if (y >= GRADIENT_TO_Y) {
            return 0.0;
        }

        return GRADIENT_FROM * (GRADIENT_TO_Y - y) / (double) (GRADIENT_TO_Y - GRADIENT_FROM_Y);
    }

    public double density(double x, int y, double z) {
        double value = this.noise.getValue(
                x + this.offsetX,
                (y + this.offsetY) * Y_SCALE,
                z + this.offsetZ) * NOISE_NORMALIZER;

        return value + DENSITY_OFFSET + RARITY_BIAS + gradient(y);
    }
}
