package sayys.depthsupdate.world.noise;

/**
 * 128-bit xoroshiro++ PRNG. Faithful backport of modern Minecraft's core RNG.
 * Period: ~2^128 - 1. Produces 64-bit outputs with excellent statistical properties.
 */
public class Xoroshiro128PlusPlus {
    private long seedLo;
    private long seedHi;

    public Xoroshiro128PlusPlus(long seedLo, long seedHi) {
        this.seedLo = seedLo;
        this.seedHi = seedHi;
        if ((this.seedLo | this.seedHi) == 0L) {
            this.seedLo = RandomSupport.GOLDEN_RATIO_64;
            this.seedHi = RandomSupport.SILVER_RATIO_64;
        }
    }

    public long nextLong() {
        long s0 = this.seedLo;
        long s1 = this.seedHi;
        long result = Long.rotateLeft(s0 + s1, 17) + s0;
        s1 ^= s0;
        this.seedLo = Long.rotateLeft(s0, 49) ^ s1 ^ (s1 << 21);
        this.seedHi = Long.rotateLeft(s1, 28);
        return result;
    }
}
