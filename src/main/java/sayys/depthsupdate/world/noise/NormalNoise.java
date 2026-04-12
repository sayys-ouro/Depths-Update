package sayys.depthsupdate.world.noise;

/**
 * Dual-PerlinNoise combiner with frequency shift to reduce banding artifacts.
 * This is the primary noise type used by the NoiseRouter and most density functions.
 * Faithful backport of modern Minecraft's NormalNoise.
 */
public class NormalNoise {
    private static final double INPUT_FACTOR = 1.0181268882175227;
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;
    private final double maxValue;
    private final NoiseParameters parameters;

    public static NormalNoise create(RandomSource random, int firstOctave, double... amplitudes) {
        return create(random, new NoiseParameters(firstOctave, amplitudes));
    }

    public static NormalNoise create(RandomSource random, NoiseParameters parameters) {
        return new NormalNoise(random, parameters, true);
    }

    public static NormalNoise createLegacyNetherBiome(RandomSource random, NoiseParameters parameters) {
        return new NormalNoise(random, parameters, false);
    }

    private NormalNoise(RandomSource random, NoiseParameters parameters, boolean useNewInitialization) {
        int firstOctave = parameters.firstOctave();
        double[] amplitudes = parameters.amplitudes();
        this.parameters = parameters;

        if (useNewInitialization) {
            this.first = PerlinNoise.create(random, firstOctave, amplitudes);
            this.second = PerlinNoise.create(random, firstOctave, amplitudes);
        } else {
            this.first = PerlinNoise.createLegacyForBlendedNoise(random, makeOctaveIndices(firstOctave, amplitudes));
            this.second = PerlinNoise.createLegacyForBlendedNoise(random, makeOctaveIndices(firstOctave, amplitudes));
        }

        int minOctave = Integer.MAX_VALUE;
        int maxOctave = Integer.MIN_VALUE;
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] != 0.0) {
                minOctave = Math.min(minOctave, i);
                maxOctave = Math.max(maxOctave, i);
            }
        }

        this.valueFactor = (1.0 / 6.0) / expectedDeviation(maxOctave - minOctave);
        this.maxValue = (this.first.maxValue() + this.second.maxValue()) * this.valueFactor;
    }

    public double maxValue() {
        return this.maxValue;
    }

    private static double expectedDeviation(int octaveSpan) {
        return 0.1 * (1.0 + 1.0 / (double) (octaveSpan + 1));
    }

    public double getValue(double x, double y, double z) {
        double x2 = x * INPUT_FACTOR;
        double y2 = y * INPUT_FACTOR;
        double z2 = z * INPUT_FACTOR;
        return (this.first.getValue(x, y, z) + this.second.getValue(x2, y2, z2)) * this.valueFactor;
    }

    public NoiseParameters parameters() {
        return this.parameters;
    }

    private static int[] makeOctaveIndices(int firstOctave, double[] amplitudes) {
        int count = 0;
        for (double a : amplitudes) {
            if (a != 0.0) count++;
        }
        int[] indices = new int[count];
        int idx = 0;
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] != 0.0) {
                indices[idx++] = firstOctave + i;
            }
        }
        return indices;
    }

    /**
     * Noise parameters: defines which octaves to use and their amplitudes.
     */
    public static final class NoiseParameters {
        private final int firstOctave;
        private final double[] amplitudes;

        public NoiseParameters(int firstOctave, double... amplitudes) {
            this.firstOctave = firstOctave;
            this.amplitudes = amplitudes;
        }

        public int firstOctave() {
            return firstOctave;
        }

        public double[] amplitudes() {
            return amplitudes;
        }
    }
}
