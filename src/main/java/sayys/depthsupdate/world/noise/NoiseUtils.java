package sayys.depthsupdate.world.noise;

/**
 * Math utilities for noise generation. Provides smoothstep, interpolation, and other
 * functions needed by the noise pipeline, equivalent to the relevant parts of Mth in modern MC.
 */
public final class NoiseUtils {

    private NoiseUtils() {}

    public static int floor(double v) {
        return (int) Math.floor(v);
    }

    public static double lerp(double alpha, double p0, double p1) {
        return p0 + alpha * (p1 - p0);
    }

    public static double lerp2(double alpha1, double alpha2, double x00, double x10, double x01, double x11) {
        return lerp(alpha2, lerp(alpha1, x00, x10), lerp(alpha1, x01, x11));
    }

    public static double lerp3(double alpha1, double alpha2, double alpha3,
                                double x000, double x100, double x010, double x110,
                                double x001, double x101, double x011, double x111) {
        return lerp(alpha3, lerp2(alpha1, alpha2, x000, x100, x010, x110),
                            lerp2(alpha1, alpha2, x001, x101, x011, x111));
    }

    /**
     * Smootherstep function: 6t^5 - 15t^4 + 10t^3 (Ken Perlin's improved smoothstep).
     */
    public static double smoothstep(double x) {
        return x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
    }

    public static double smoothstepDerivative(double x) {
        return 30.0 * x * x * (x - 1.0) * (x - 1.0);
    }

    public static double clampedLerp(double factor, double min, double max) {
        if (factor < 0.0) {
            return min;
        } else {
            return factor > 1.0 ? max : lerp(factor, min, max);
        }
    }

    public static double clampedMap(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return clampedLerp(inverseLerp(value, fromMin, fromMax), toMin, toMax);
    }

    public static double inverseLerp(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    public static double map(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return lerp(inverseLerp(value, fromMin, fromMax), toMin, toMax);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double square(double x) {
        return x * x;
    }

    /**
     * Pushes noise values toward extremes (-1 or +1).
     * Used by some density functions for biasing terrain features.
     */
    public static double biasTowardsExtreme(double noise, double factor) {
        return noise + Math.sin(Math.PI * noise) * factor / Math.PI;
    }
}
