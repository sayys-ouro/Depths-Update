package sayys.depthsupdate.world.noise;

/**
 * Abstract random number source interface. Faithful backport of modern Minecraft's RandomSource.
 * Supports forking for independent streams and positional factories for deterministic generation.
 */
public interface RandomSource {

    RandomSource fork();

    PositionalRandomFactory forkPositional();

    void setSeed(long seed);

    int nextInt();

    int nextInt(int bound);

    default int nextIntBetweenInclusive(int min, int maxInclusive) {
        return this.nextInt(maxInclusive - min + 1) + min;
    }

    long nextLong();

    boolean nextBoolean();

    float nextFloat();

    double nextDouble();

    double nextGaussian();

    default double triangle(double mean, double spread) {
        return mean + spread * (this.nextDouble() - this.nextDouble());
    }

    default float triangle(float mean, float spread) {
        return mean + spread * (this.nextFloat() - this.nextFloat());
    }

    default void consumeCount(int rounds) {
        for (int i = 0; i < rounds; ++i) {
            this.nextInt();
        }
    }

    default int nextInt(int origin, int bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("bound - origin is non positive");
        }
        return origin + this.nextInt(bound - origin);
    }
}
