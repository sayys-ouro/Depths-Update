package sayys.depthsupdate.world.noise;

/**
 * Simplex noise implementation for 2D and 3D. Faithful backport of modern Minecraft's SimplexNoise.
 * Also provides the GRADIENT table and dot() used by ImprovedNoise.
 */
public class SimplexNoise {
    protected static final int[][] GRADIENT = {
        {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
        {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
        {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1},
        {1, 1, 0}, {0, -1, 1}, {-1, 1, 0}, {0, -1, -1}
    };

    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double F2 = 0.5 * (SQRT_3 - 1.0);
    private static final double G2 = (3.0 - SQRT_3) / 6.0;

    private final int[] p = new int[512];
    public final double xo;
    public final double yo;
    public final double zo;

    public SimplexNoise(RandomSource random) {
        this.xo = random.nextDouble() * 256.0;
        this.yo = random.nextDouble() * 256.0;
        this.zo = random.nextDouble() * 256.0;

        for (int i = 0; i < 256; this.p[i] = i++) {
        }

        for (int i = 0; i < 256; ++i) {
            int offset = random.nextInt(256 - i);
            int tmp = this.p[i];
            this.p[i] = this.p[offset + i];
            this.p[offset + i] = tmp;
        }
    }

    private int p(int x) {
        return this.p[x & 255];
    }

    protected static double dot(int[] g, double x, double y, double z) {
        return (double) g[0] * x + (double) g[1] * y + (double) g[2] * z;
    }

    private double getCornerNoise3D(int index, double x, double y, double z, double base) {
        double t0 = base - x * x - y * y - z * z;
        if (t0 < 0.0) {
            return 0.0;
        }
        t0 *= t0;
        return t0 * t0 * dot(GRADIENT[index], x, y, z);
    }

    public double getValue(double xin, double yin) {
        double s = (xin + yin) * F2;
        int i = NoiseUtils.floor(xin + s);
        int j = NoiseUtils.floor(yin + s);
        double t = (double) (i + j) * G2;
        double x0 = xin - ((double) i - t);
        double y0 = yin - ((double) j - t);

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - (double) i1 + G2;
        double y1 = y0 - (double) j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;

        int ii = i & 255;
        int jj = j & 255;
        int gi0 = this.p(ii + this.p(jj)) % 12;
        int gi1 = this.p(ii + i1 + this.p(jj + j1)) % 12;
        int gi2 = this.p(ii + 1 + this.p(jj + 1)) % 12;

        double n0 = this.getCornerNoise3D(gi0, x0, y0, 0.0, 0.5);
        double n1 = this.getCornerNoise3D(gi1, x1, y1, 0.0, 0.5);
        double n2 = this.getCornerNoise3D(gi2, x2, y2, 0.0, 0.5);
        return 70.0 * (n0 + n1 + n2);
    }

    public double getValue(double xin, double yin, double zin) {
        double s = (xin + yin + zin) * (1.0 / 3.0);
        int i = NoiseUtils.floor(xin + s);
        int j = NoiseUtils.floor(yin + s);
        int k = NoiseUtils.floor(zin + s);
        double t = (double) (i + j + k) * (1.0 / 6.0);
        double x0 = xin - ((double) i - t);
        double y0 = yin - ((double) j - t);
        double z0 = zin - ((double) k - t);

        int i1, j1, k1, i2, j2, k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1;
            } else {
                i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1;
            }
        } else if (y0 < z0) {
            i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1;
        } else if (x0 < z0) {
            i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1;
        } else {
            i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
        }

        double g3 = 1.0 / 6.0;
        double x1 = x0 - (double) i1 + g3;
        double y1 = y0 - (double) j1 + g3;
        double z1 = z0 - (double) k1 + g3;
        double x2 = x0 - (double) i2 + (1.0 / 3.0);
        double y2 = y0 - (double) j2 + (1.0 / 3.0);
        double z2 = z0 - (double) k2 + (1.0 / 3.0);
        double x3 = x0 - 1.0 + 0.5;
        double y3 = y0 - 1.0 + 0.5;
        double z3 = z0 - 1.0 + 0.5;

        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int gi0 = this.p(ii + this.p(jj + this.p(kk))) % 12;
        int gi1 = this.p(ii + i1 + this.p(jj + j1 + this.p(kk + k1))) % 12;
        int gi2 = this.p(ii + i2 + this.p(jj + j2 + this.p(kk + k2))) % 12;
        int gi3 = this.p(ii + 1 + this.p(jj + 1 + this.p(kk + 1))) % 12;

        double n0 = this.getCornerNoise3D(gi0, x0, y0, z0, 0.6);
        double n1 = this.getCornerNoise3D(gi1, x1, y1, z1, 0.6);
        double n2 = this.getCornerNoise3D(gi2, x2, y2, z2, 0.6);
        double n3 = this.getCornerNoise3D(gi3, x3, y3, z3, 0.6);
        return 32.0 * (n0 + n1 + n2 + n3);
    }
}
