package sayys.depthsupdate.world.noise;

import java.util.Arrays;

/**
 * Fractional Brownian Motion (fBm) octave combiner using ImprovedNoise layers.
 * Faithful backport of modern Minecraft's PerlinNoise.
 */
public class PerlinNoise {
    private static final int ROUND_OFF = 33554432;
    private final ImprovedNoise[] noiseLevels;
    private final int firstOctave;
    private final double[] amplitudes;
    private final double lowestFreqValueFactor;
    private final double lowestFreqInputFactor;
    private final double maxValue;

    /**
     * Creates PerlinNoise using positional random factory (modern initialization).
     *
     * @param random        the random source
     * @param firstOctave   starting octave index (can be negative for low frequencies)
     * @param amplitudes    per-octave amplitude weights
     */
    public static PerlinNoise create(RandomSource random, int firstOctave, double... amplitudes) {
        return new PerlinNoise(random, firstOctave, amplitudes, true);
    }

    /**
     * Creates PerlinNoise using legacy sequential initialization (for BlendedNoise compat).
     */
    public static PerlinNoise createLegacyForBlendedNoise(RandomSource random, int... octaveIndices) {
        Arrays.sort(octaveIndices);
        int firstOctave = octaveIndices[0];
        int lastOctave = octaveIndices[octaveIndices.length - 1];
        int totalOctaves = lastOctave - firstOctave + 1;
        double[] amplitudes = new double[totalOctaves];
        for (int octave : octaveIndices) {
            amplitudes[octave - firstOctave] = 1.0;
        }
        return new PerlinNoise(random, firstOctave, amplitudes, false);
    }

    private PerlinNoise(RandomSource random, int firstOctave, double[] amplitudes, boolean useNewInitialization) {
        this.firstOctave = firstOctave;
        this.amplitudes = amplitudes;
        int octaves = amplitudes.length;
        int zeroOctaveIndex = -firstOctave;
        this.noiseLevels = new ImprovedNoise[octaves];

        if (useNewInitialization) {
            PositionalRandomFactory positional = random.forkPositional();
            for (int i = 0; i < octaves; ++i) {
                if (amplitudes[i] != 0.0) {
                    int octave = firstOctave + i;
                    this.noiseLevels[i] = new ImprovedNoise(positional.fromHashOf("octave_" + octave));
                }
            }
        } else {
            ImprovedNoise zeroOctave = new ImprovedNoise(random);
            if (zeroOctaveIndex >= 0 && zeroOctaveIndex < octaves) {
                if (amplitudes[zeroOctaveIndex] != 0.0) {
                    this.noiseLevels[zeroOctaveIndex] = zeroOctave;
                }
            }

            for (int i = zeroOctaveIndex - 1; i >= 0; --i) {
                if (i < octaves) {
                    if (amplitudes[i] != 0.0) {
                        this.noiseLevels[i] = new ImprovedNoise(random);
                    } else {
                        skipOctave(random);
                    }
                } else {
                    skipOctave(random);
                }
            }
        }

        this.lowestFreqInputFactor = Math.pow(2.0, (double) (-zeroOctaveIndex));
        this.lowestFreqValueFactor = Math.pow(2.0, (double) (octaves - 1)) / (Math.pow(2.0, (double) octaves) - 1.0);
        this.maxValue = this.edgeValue(2.0);
    }

    public double maxValue() {
        return this.maxValue;
    }

    private static void skipOctave(RandomSource random) {
        random.consumeCount(262);
    }

    public double getValue(double x, double y, double z) {
        return this.getValue(x, y, z, 0.0, 0.0);
    }

    public double getValue(double x, double y, double z, double yScale, double yFudge) {
        double value = 0.0;
        double factor = this.lowestFreqInputFactor;
        double valueFactor = this.lowestFreqValueFactor;

        for (int i = 0; i < this.noiseLevels.length; ++i) {
            ImprovedNoise noise = this.noiseLevels[i];
            if (noise != null) {
                double noiseVal = noise.noise(
                        wrap(x * factor), wrap(y * factor), wrap(z * factor),
                        yScale * factor, yFudge * factor);
                value += this.amplitudes[i] * noiseVal * valueFactor;
            }
            factor *= 2.0;
            valueFactor /= 2.0;
        }

        return value;
    }

    private double edgeValue(double noiseValue) {
        double value = 0.0;
        double valueFactor = this.lowestFreqValueFactor;

        for (int i = 0; i < this.noiseLevels.length; ++i) {
            ImprovedNoise noise = this.noiseLevels[i];
            if (noise != null) {
                value += this.amplitudes[i] * noiseValue * valueFactor;
            }
            valueFactor /= 2.0;
        }

        return value;
    }

    public ImprovedNoise getOctaveNoise(int i) {
        return this.noiseLevels[this.noiseLevels.length - 1 - i];
    }

    public static double wrap(double x) {
        return x - (double) Math.floor(x / 3.3554432E7 + 0.5) * 3.3554432E7;
    }

    public int firstOctave() {
        return this.firstOctave;
    }

    public double[] amplitudes() {
        return this.amplitudes;
    }
}
