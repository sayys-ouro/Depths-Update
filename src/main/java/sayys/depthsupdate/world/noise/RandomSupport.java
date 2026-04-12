package sayys.depthsupdate.world.noise;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Seed mixing and upgrade utilities. Faithful backport of modern Minecraft's RandomSupport.
 */
public final class RandomSupport {
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);

    private RandomSupport() {}

    public static long mixStafford13(long z) {
        z = (z ^ (z >>> 30)) * -4658895280553007687L;
        z = (z ^ (z >>> 27)) * -7723592293110705685L;
        return z ^ (z >>> 31);
    }

    public static Seed128bit upgradeSeedTo128bitUnmixed(long legacySeed) {
        long lowBits = legacySeed ^ SILVER_RATIO_64;
        long highBits = lowBits + GOLDEN_RATIO_64;
        return new Seed128bit(lowBits, highBits);
    }

    public static Seed128bit upgradeSeedTo128bit(long legacySeed) {
        return upgradeSeedTo128bitUnmixed(legacySeed).mixed();
    }

    public static Seed128bit seedFromHashOf(String input) {
        byte[] hashCode = md5Hash(input);
        long hashLo = bytesToLong(hashCode, 0);
        long hashHi = bytesToLong(hashCode, 8);
        return new Seed128bit(hashLo, hashHi);
    }

    public static long generateUniqueSeed() {
        return SEED_UNIQUIFIER.updateAndGet(current -> current * 1181783497276652981L) ^ System.nanoTime();
    }

    /**
     * Computes the positional seed hash for block coordinates.
     * Equivalent to Mth.getSeed(x, y, z) in modern Minecraft.
     */
    public static long getSeed(int x, int y, int z) {
        long seed = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
        seed = seed * seed * 42317861L + seed * 11L;
        return seed >> 16;
    }

    private static byte[] md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    private static long bytesToLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 8) | (bytes[offset + i] & 0xFFL);
        }
        return result;
    }

    public static final class Seed128bit {
        private final long seedLo;
        private final long seedHi;

        public Seed128bit(long seedLo, long seedHi) {
            this.seedLo = seedLo;
            this.seedHi = seedHi;
        }

        public long seedLo() {
            return seedLo;
        }

        public long seedHi() {
            return seedHi;
        }

        public Seed128bit xor(long lo, long hi) {
            return new Seed128bit(this.seedLo ^ lo, this.seedHi ^ hi);
        }

        public Seed128bit xor(Seed128bit other) {
            return xor(other.seedLo, other.seedHi);
        }

        public Seed128bit mixed() {
            return new Seed128bit(RandomSupport.mixStafford13(this.seedLo), RandomSupport.mixStafford13(this.seedHi));
        }
    }
}
