package sayys.depthsupdate.world.noise;

import java.util.Random;

/**
 * RandomSource backed by java.util.Random. Used for legacy random sequences
 * that must match vanilla Minecraft's LCG-based random behavior (e.g. End islands).
 */
public class LegacyRandomSource implements RandomSource {
    private final Random random;

    public LegacyRandomSource(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public RandomSource fork() {
        return new LegacyRandomSource(this.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new XoroshiroRandomSource(this.nextLong()).forkPositional();
    }

    @Override
    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    @Override
    public int nextInt() {
        return this.random.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return this.random.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return this.random.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return this.random.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return this.random.nextFloat();
    }

    @Override
    public double nextDouble() {
        return this.random.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return this.random.nextGaussian();
    }
}
