package sayys.depthsupdate.world.noise;

import sayys.depthsupdate.world.density.DensityFunction;

/**
 * 3-layer terrain blender: blends between minLimit and maxLimit noise using mainNoise as interpolant.
 * This is the raw terrain density before spline shaping.
 * Faithful backport of modern Minecraft's BlendedNoise.
 * Implements DensityFunction.SimpleFunction for use in the density function tree.
 */
public class BlendedNoise implements DensityFunction.SimpleFunction {
    private final PerlinNoise minLimitNoise;
    private final PerlinNoise maxLimitNoise;
    private final PerlinNoise mainNoise;
    private final double xzMultiplier;
    private final double yMultiplier;
    private final double xzFactor;
    private final double yFactor;
    private final double smearScaleMultiplier;
    private final double maxValue;
    private final double xzScale;
    private final double yScale;

    public BlendedNoise(RandomSource random, double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
        this(
            PerlinNoise.createLegacyForBlendedNoise(random, rangeArray(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(random, rangeArray(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(random, rangeArray(-7, 0)),
            xzScale, yScale, xzFactor, yFactor, smearScaleMultiplier
        );
    }

    private BlendedNoise(PerlinNoise minLimitNoise, PerlinNoise maxLimitNoise, PerlinNoise mainNoise,
                         double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
        this.minLimitNoise = minLimitNoise;
        this.maxLimitNoise = maxLimitNoise;
        this.mainNoise = mainNoise;
        this.xzScale = xzScale;
        this.yScale = yScale;
        this.xzFactor = xzFactor;
        this.yFactor = yFactor;
        this.smearScaleMultiplier = smearScaleMultiplier;
        this.xzMultiplier = 684.412 * this.xzScale;
        this.yMultiplier = 684.412 * this.yScale;
        this.maxValue = minLimitNoise.maxValue(); // Simplified from maxBrokenValue
    }

    public static BlendedNoise createUnseeded(double xzScale, double yScale, double xzFactor,
                                                double yFactor, double smearScaleMultiplier) {
        return new BlendedNoise(new XoroshiroRandomSource(0L), xzScale, yScale, xzFactor, yFactor, smearScaleMultiplier);
    }

    public BlendedNoise withNewRandom(RandomSource random) {
        return new BlendedNoise(random, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        int blockX = context.blockX();
        int blockY = context.blockY();
        int blockZ = context.blockZ();
        double limitX = (double) blockX * this.xzMultiplier;
        double limitY = (double) blockY * this.yMultiplier;
        double limitZ = (double) blockZ * this.xzMultiplier;
        double mainX = limitX / this.xzFactor;
        double mainY = limitY / this.yFactor;
        double mainZ = limitZ / this.xzFactor;
        double limitSmear = this.yMultiplier * this.smearScaleMultiplier;
        double mainSmear = limitSmear / this.yFactor;

        // Evaluate main noise (8 octaves) to get blend factor
        double mainNoiseValue = 0.0;
        double pow = 1.0;
        for (int i = 0; i < 8; ++i) {
            ImprovedNoise noise = this.mainNoise.getOctaveNoise(i);
            if (noise != null) {
                mainNoiseValue += noise.noise(
                        PerlinNoise.wrap(mainX * pow),
                        PerlinNoise.wrap(mainY * pow),
                        PerlinNoise.wrap(mainZ * pow),
                        mainSmear * pow, mainY * pow) / pow;
            }
            pow /= 2.0;
        }

        double factor = (mainNoiseValue / 10.0 + 1.0) / 2.0;
        boolean isMax = factor >= 1.0;
        boolean isMin = factor <= 0.0;

        // Evaluate min/max limit noises (16 octaves each), skipping if clamped
        double blendMin = 0.0;
        double blendMax = 0.0;
        pow = 1.0;
        for (int i = 0; i < 16; ++i) {
            double wx = PerlinNoise.wrap(limitX * pow);
            double wy = PerlinNoise.wrap(limitY * pow);
            double wz = PerlinNoise.wrap(limitZ * pow);
            double yScalePow = limitSmear * pow;

            if (!isMax) {
                ImprovedNoise minNoise = this.minLimitNoise.getOctaveNoise(i);
                if (minNoise != null) {
                    blendMin += minNoise.noise(wx, wy, wz, yScalePow, limitY * pow) / pow;
                }
            }

            if (!isMin) {
                ImprovedNoise maxNoise = this.maxLimitNoise.getOctaveNoise(i);
                if (maxNoise != null) {
                    blendMax += maxNoise.noise(wx, wy, wz, yScalePow, limitY * pow) / pow;
                }
            }

            pow /= 2.0;
        }

        return NoiseUtils.clampedLerp(factor, blendMin / 512.0, blendMax / 512.0) / 128.0;
    }

    @Override
    public double minValue() {
        return -this.maxValue;
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }

    public double xzScale() {
        return this.xzScale;
    }

    public double yScale() {
        return this.yScale;
    }

    public double xzFactor() {
        return this.xzFactor;
    }

    public double yFactor() {
        return this.yFactor;
    }

    public double smearScaleMultiplier() {
        return this.smearScaleMultiplier;
    }

    private static int[] rangeArray(int from, int to) {
        int[] result = new int[to - from + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = from + i;
        }
        return result;
    }
}
