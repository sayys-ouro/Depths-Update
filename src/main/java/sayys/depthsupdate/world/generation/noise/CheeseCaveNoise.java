package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class CheeseCaveNoise {
    private static final double DENSITY_OFFSET = 0.27;
    private static final double LAYER_WEIGHT = 4.0;

    private static final double TOP_SLIDE_MAX = 1.5;
    private static final int TOP_SLIDE_RANGE = 20;

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

    private final int topSlideStart;
    private final double densityOffset;

    public CheeseCaveNoise(long seed, double offsetX, double offsetY, double offsetZ, int caveMaxY, double abundance) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.topSlideStart = caveMaxY - TOP_SLIDE_RANGE;
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

    public double density(double x, int y, double z) {
        double noiseX = x + this.offsetX;
        double noiseY = y + this.offsetY;
        double noiseZ = z + this.offsetZ;

        double stretchedY = noiseY * Y_SCALE;
        double cheese = (this.wideNoise.getValue(noiseX, stretchedY, noiseZ) * 0.5
                + this.mainNoise.getValue(noiseX, stretchedY, noiseZ) * 2.0
                + this.detailNoise.getValue(noiseX, stretchedY, noiseZ) * 2.0) * NOISE_NORMALIZER;

        double layer = this.layerNoise.getValue(noiseX, noiseY * LAYER_Y_SCALE, noiseZ) * LAYER_NOISE_SCALE;

        double density = Math.clamp(cheese + this.densityOffset, -1.0, 1.0) + LAYER_WEIGHT * layer * layer;

        if (y > this.topSlideStart) {
            density += Math.min(TOP_SLIDE_MAX, TOP_SLIDE_MAX * (y - this.topSlideStart) / (double) TOP_SLIDE_RANGE);
        }

        return density;
    }
}
