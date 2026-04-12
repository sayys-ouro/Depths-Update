package sayys.depthsupdate.world.noise;

/**
 * Marsaglia polar method for generating Gaussian-distributed random doubles.
 * Produces two normal(0,1) deviates per iteration, caching the second.
 */
public class MarsagliaPolarGaussian {
    private final RandomSource randomSource;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public MarsagliaPolarGaussian(RandomSource randomSource) {
        this.randomSource = randomSource;
    }

    public void reset() {
        this.haveNextNextGaussian = false;
    }

    public double nextGaussian() {
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        } else {
            double x;
            double y;
            double radiusSquared;
            do {
                do {
                    x = 2.0 * this.randomSource.nextDouble() - 1.0;
                    y = 2.0 * this.randomSource.nextDouble() - 1.0;
                    radiusSquared = x * x + y * y;
                } while (radiusSquared >= 1.0);
            } while (radiusSquared == 0.0);

            double multiplier = Math.sqrt(-2.0 * Math.log(radiusSquared) / radiusSquared);
            this.nextNextGaussian = y * multiplier;
            this.haveNextNextGaussian = true;
            return x * multiplier;
        }
    }
}
