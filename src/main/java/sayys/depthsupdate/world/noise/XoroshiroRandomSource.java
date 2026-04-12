package sayys.depthsupdate.world.noise;

/**
 * RandomSource backed by Xoroshiro128PlusPlus. Primary RNG for all world generation.
 * Faithful backport of modern Minecraft's XoroshiroRandomSource.
 */
public class XoroshiroRandomSource implements RandomSource {
    private static final float FLOAT_UNIT = 5.9604645E-8F;
    private static final double DOUBLE_UNIT = 1.1102230246251565E-16;

    private Xoroshiro128PlusPlus randomNumberGenerator;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public XoroshiroRandomSource(long seed) {
        RandomSupport.Seed128bit upgraded = RandomSupport.upgradeSeedTo128bit(seed);
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(upgraded.seedLo(), upgraded.seedHi());
    }

    public XoroshiroRandomSource(RandomSupport.Seed128bit seed) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(seed.seedLo(), seed.seedHi());
    }

    public XoroshiroRandomSource(long seedLo, long seedHi) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(seedLo, seedHi);
    }

    @Override
    public RandomSource fork() {
        return new XoroshiroRandomSource(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new XoroshiroPositionalRandomFactory(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public void setSeed(long seed) {
        RandomSupport.Seed128bit upgraded = RandomSupport.upgradeSeedTo128bit(seed);
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(upgraded.seedLo(), upgraded.seedHi());
        this.gaussianSource.reset();
    }

    @Override
    public int nextInt() {
        return (int) this.randomNumberGenerator.nextLong();
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        long randomBits = Integer.toUnsignedLong(this.nextInt());
        long multipliedRandomBits = randomBits * (long) bound;
        long fractionalPart = multipliedRandomBits & 4294967295L;
        if (fractionalPart < (long) bound) {
            for (int unbiasedBucketsStartIndex = Integer.remainderUnsigned(~bound + 1, bound);
                 fractionalPart < (long) unbiasedBucketsStartIndex;
                 fractionalPart = multipliedRandomBits & 4294967295L) {
                randomBits = Integer.toUnsignedLong(this.nextInt());
                multipliedRandomBits = randomBits * (long) bound;
            }
        }
        long integerPart = multipliedRandomBits >> 32;
        return (int) integerPart;
    }

    @Override
    public long nextLong() {
        return this.randomNumberGenerator.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return (this.randomNumberGenerator.nextLong() & 1L) != 0L;
    }

    @Override
    public float nextFloat() {
        return (float) this.nextBits(24) * FLOAT_UNIT;
    }

    @Override
    public double nextDouble() {
        return (double) this.nextBits(53) * DOUBLE_UNIT;
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }

    @Override
    public void consumeCount(int rounds) {
        for (int i = 0; i < rounds; ++i) {
            this.randomNumberGenerator.nextLong();
        }
    }

    private long nextBits(int bits) {
        return this.randomNumberGenerator.nextLong() >>> (64 - bits);
    }

    public static class XoroshiroPositionalRandomFactory implements PositionalRandomFactory {
        private final long seedLo;
        private final long seedHi;

        public XoroshiroPositionalRandomFactory(long seedLo, long seedHi) {
            this.seedLo = seedLo;
            this.seedHi = seedHi;
        }

        @Override
        public RandomSource at(int x, int y, int z) {
            long positionalSeed = RandomSupport.getSeed(x, y, z);
            long randomSeed = positionalSeed ^ this.seedLo;
            return new XoroshiroRandomSource(randomSeed, this.seedHi);
        }

        @Override
        public RandomSource fromHashOf(String name) {
            RandomSupport.Seed128bit seed = RandomSupport.seedFromHashOf(name);
            return new XoroshiroRandomSource(seed.xor(this.seedLo, this.seedHi));
        }

        @Override
        public RandomSource fromSeed(long seed) {
            return new XoroshiroRandomSource(seed ^ this.seedLo, seed ^ this.seedHi);
        }
    }
}
