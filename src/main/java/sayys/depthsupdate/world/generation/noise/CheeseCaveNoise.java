package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

/**
 * Vanilla bounds its cheese caves with the terrain density they are subtracted
 * from, so they exist at every altitude and simply run out of rock near the
 * surface. We carve a finished primer and have no terrain term to lose to, so
 * {@link #surfaceSlide(int)} plays that part: it is a function of depth below
 * the column's own surface, which is what puts caves inside mountains instead
 * of only under a fixed Y.
 */
public final class CheeseCaveNoise {
    /**
     * Size and frequency lever, and the only one that acts at every depth.
     * Vanilla's constant is 0.27; ours is higher because we lack the terrain
     * density that bounds vanilla's caverns.
     *
     * Deep rock carved, and horizontal chamber runs mean / p95:
     * 0.27 gives 13.8 percent, 8.5 / 28. 0.40 gives 8.6 percent, 7.1 / 21.
     *
     * The "Cheese Caves Size" config multiplies this, so either calibration is
     * reachable without a rebuild: 0.675 against 0.40 gives 0.27.
     */
    private static final double DENSITY_OFFSET = 0.40;
    private static final double LAYER_WEIGHT = 4.0;

    /**
     * Near vanilla's clamp(1.5 - 0.64 * slopedCheese, 0, 0.5): a weak, shallow
     * push. The clamp in density() bounds the noise at -1, so this alone keeps
     * flat ground intact while letting caverns open on hillsides.
     */
    private static final double SURFACE_SLIDE_MAX = 0.7;
    private static final int SURFACE_SLIDE_FROM_DEPTH = 20;
    private static final int SURFACE_SLIDE_TO_DEPTH = 4;

    private static final double Y_SCALE = 1.0;
    private static final double LAYER_Y_SCALE = 8.0;

    private static final double NOISE_NORMALIZER = 2.6;
    private static final double LAYER_NOISE_SCALE = 7.5;

    private static final double WIDE_WAVELENGTH = 256.0;
    private static final double MAIN_WAVELENGTH = 64.0;
    private static final double DETAIL_WAVELENGTH = 16.0;
    private static final double LAYER_WAVELENGTH = 256.0;

    private final Perlin wideNoise;
    private final Perlin mainNoise;
    private final Perlin detailNoise;
    private final Perlin layerNoise;

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    private final double densityOffset;

    public CheeseCaveNoise(long seed, double offsetX, double offsetY, double offsetZ, double abundance) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.densityOffset = DENSITY_OFFSET * abundance;

        // 256 and 128 blocks at amplitudes 0.5 and 1.0 (persistence 2 doubles).
        this.wideNoise = new Perlin();
        this.wideNoise.setSeed((int) seed + 420);
        this.wideNoise.setOctaveCount(2);
        this.wideNoise.setPersistence(2.0);
        this.wideNoise.setFrequency(1.0 / WIDE_WAVELENGTH);

        // 64 and 32 blocks at amplitudes 2.0 and 1.0.
        this.mainNoise = new Perlin();
        this.mainNoise.setSeed((int) seed + 421);
        this.mainNoise.setOctaveCount(2);
        this.mainNoise.setPersistence(0.5);
        this.mainNoise.setFrequency(1.0 / MAIN_WAVELENGTH);

        // 16 and 8 blocks at amplitudes 2.0 and 1.0.
        this.detailNoise = new Perlin();
        this.detailNoise.setSeed((int) seed + 422);
        this.detailNoise.setOctaveCount(2);
        this.detailNoise.setPersistence(0.5);
        this.detailNoise.setFrequency(1.0 / DETAIL_WAVELENGTH);

        this.layerNoise = new Perlin();
        this.layerNoise.setSeed((int) seed + 423);
        this.layerNoise.setOctaveCount(1);
        this.layerNoise.setFrequency(1.0 / LAYER_WAVELENGTH);
    }

    /**
     * Applied by the generator rather than inside the noise: the density grid is
     * sampled every four blocks and spills into the neighbouring chunk, where
     * this column's surface height is not known.
     */
    public static double surfaceSlide(int depth) {
        if (depth >= SURFACE_SLIDE_FROM_DEPTH) {
            return 0.0;
        }

        if (depth <= SURFACE_SLIDE_TO_DEPTH) {
            return SURFACE_SLIDE_MAX;
        }

        return SURFACE_SLIDE_MAX * (SURFACE_SLIDE_FROM_DEPTH - depth)
                / (double) (SURFACE_SLIDE_FROM_DEPTH - SURFACE_SLIDE_TO_DEPTH);
    }

    public double density(double x, int y, double z) {
        double noiseX = x + this.offsetX;
        double noiseY = y + this.offsetY;
        double noiseZ = z + this.offsetZ;

        double stretchedY = noiseY * Y_SCALE;
        double cheese = (this.wideNoise.getValue(noiseX, stretchedY, noiseZ) * 0.5
                + this.mainNoise.getValue(noiseX, stretchedY, noiseZ) * 2.0
                + this.detailNoise.getValue(noiseX, stretchedY, noiseZ) * 2.0) * NOISE_NORMALIZER;

        double layer = this.layerNoise.getValue(noiseX, noiseY * LAYER_Y_SCALE, noiseZ) * LAYER_NOISE_SCALE;

        return Math.clamp(cheese + this.densityOffset, -1.0, 1.0) + LAYER_WEIGHT * layer * layer;
    }
}
