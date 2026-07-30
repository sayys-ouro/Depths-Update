package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class PillarNoise {
    private static final double CUTOFF = 0.03;

    private static final double SHAPE_WAVELENGTH = 128.0;
    private static final double JITTER_WAVELENGTH = 32.0;
    private static final double JITTER_WEIGHT = 0.55;
    private static final double XZ_SCALE = 6.5;
    private static final double Y_SCALE = 1.0;
    private static final double REGION_WAVELENGTH = 256.0;

    private static final double PILLAR_NOISE_SCALE = 5.4;
    private static final double REGION_NOISE_SCALE = 5.0;

    private final Perlin shapeNoise;
    private final Perlin jitterNoise;
    private final Perlin rarenessNoise;
    private final Perlin thicknessNoise;

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    public PillarNoise(long seed, double offsetX, double offsetY, double offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;

        this.shapeNoise = new Perlin();
        this.shapeNoise.setSeed((int) seed + 510);
        this.shapeNoise.setOctaveCount(1);
        this.shapeNoise.setFrequency(1.0 / SHAPE_WAVELENGTH);

        this.jitterNoise = new Perlin();
        this.jitterNoise.setSeed((int) seed + 513);
        this.jitterNoise.setOctaveCount(1);
        this.jitterNoise.setFrequency(1.0 / JITTER_WAVELENGTH);

        this.rarenessNoise = new Perlin();
        this.rarenessNoise.setSeed((int) seed + 511);
        this.rarenessNoise.setOctaveCount(1);
        this.rarenessNoise.setFrequency(1.0 / REGION_WAVELENGTH);

        this.thicknessNoise = new Perlin();
        this.thicknessNoise.setSeed((int) seed + 512);
        this.thicknessNoise.setOctaveCount(1);
        this.thicknessNoise.setFrequency(1.0 / REGION_WAVELENGTH);
    }

    public double density(double x, int y, double z) {
        double noiseX = x + this.offsetX;
        double noiseY = y + this.offsetY;
        double noiseZ = z + this.offsetZ;

        double scaledX = noiseX * XZ_SCALE;
        double scaledY = noiseY * Y_SCALE;
        double scaledZ = noiseZ * XZ_SCALE;

        double pillar = (this.shapeNoise.getValue(scaledX, scaledY, scaledZ)
                + this.jitterNoise.getValue(scaledX, scaledY, scaledZ) * JITTER_WEIGHT) * PILLAR_NOISE_SCALE;
        double rareness = this.rarenessNoise.getValue(noiseX, noiseY, noiseZ) * REGION_NOISE_SCALE;
        double thickness = 0.55 + 0.55 * Math.clamp(
                this.thicknessNoise.getValue(noiseX, noiseY, noiseZ) * REGION_NOISE_SCALE, -1.0, 1.0);

        return (2.0 * pillar - 1.0 - rareness) * thickness * thickness * thickness;
    }

    public static boolean isSolid(double density) {
        return density > CUTOFF;
    }
}
