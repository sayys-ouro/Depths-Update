package sayys.depthsupdate.world.generation.noise.sponge;

public final class Noise {
   private static final int X_NOISE_GEN = 1619;
   private static final int Y_NOISE_GEN = 31337;
   private static final int Z_NOISE_GEN = 6971;
   private static final int SEED_NOISE_GEN = 1013;
   private static final int SHIFT_NOISE_GEN = 8;

   private Noise() {}

   public static double gradientCoherentNoise3D(double x, double y, double z, int seed, NoiseQuality quality) {
      int x0 = x > 0.0D ? (int) x : (int) x - 1;
      int x1 = x0 + 1;
      int y0 = y > 0.0D ? (int) y : (int) y - 1;
      int y1 = y0 + 1;
      int z0 = z > 0.0D ? (int) z : (int) z - 1;
      int z1 = z0 + 1;
      double xs;
      double ys;
      double zs;
      if (quality == NoiseQuality.FAST) {
         xs = x - (double) x0;
         ys = y - (double) y0;
         zs = z - (double) z0;
      } else if (quality == NoiseQuality.STANDARD) {
         xs = Utils.sCurve3(x - (double) x0);
         ys = Utils.sCurve3(y - (double) y0);
         zs = Utils.sCurve3(z - (double) z0);
      } else {
         xs = Utils.sCurve5(x - (double) x0);
         ys = Utils.sCurve5(y - (double) y0);
         zs = Utils.sCurve5(z - (double) z0);
      }

      double n0 = gradientNoise3D(x, y, z, x0, y0, z0, seed);
      double n1 = gradientNoise3D(x, y, z, x1, y0, z0, seed);
      double ix0 = Utils.linearInterp(n0, n1, xs);
      n0 = gradientNoise3D(x, y, z, x0, y1, z0, seed);
      n1 = gradientNoise3D(x, y, z, x1, y1, z0, seed);
      double ix1 = Utils.linearInterp(n0, n1, xs);
      double iy0 = Utils.linearInterp(ix0, ix1, ys);
      n0 = gradientNoise3D(x, y, z, x0, y0, z1, seed);
      n1 = gradientNoise3D(x, y, z, x1, y0, z1, seed);
      ix0 = Utils.linearInterp(n0, n1, xs);
      n0 = gradientNoise3D(x, y, z, x0, y1, z1, seed);
      n1 = gradientNoise3D(x, y, z, x1, y1, z1, seed);
      ix1 = Utils.linearInterp(n0, n1, xs);
      double iy1 = Utils.linearInterp(ix0, ix1, ys);
      return Utils.linearInterp(iy0, iy1, zs);
   }

   public static double gradientNoise3D(double fx, double fy, double fz, int ix, int iy, int iz, int seed) {
      int vectorIndex = 1619 * ix + 31337 * iy + 6971 * iz + 1013 * seed;
      vectorIndex ^= vectorIndex >> 8;
      vectorIndex &= 255;
      double xvGradient = Utils.RANDOM_VECTORS[vectorIndex << 2];
      double yvGradient = Utils.RANDOM_VECTORS[(vectorIndex << 2) + 1];
      double zvGradient = Utils.RANDOM_VECTORS[(vectorIndex << 2) + 2];
      double xvPoint = fx - (double) ix;
      double yvPoint = fy - (double) iy;
      double zvPoint = fz - (double) iz;
      return xvGradient * xvPoint + yvGradient * yvPoint + zvGradient * zvPoint + 0.5D;
   }

   public static int intValueNoise3D(int x, int y, int z, int seed) {
      int n = 1619 * x + 31337 * y + 6971 * z + 1013 * seed & Integer.MAX_VALUE;
      n ^= n >> 13;
      return n * (n * n * '\uec4d' + 19990303) + 1376312589 & Integer.MAX_VALUE;
   }

   public static double valueCoherentNoise3D(double x, double y, double z, int seed, NoiseQuality quality) {
      int x0 = x > 0.0D ? (int) x : (int) x - 1;
      int x1 = x0 + 1;
      int y0 = y > 0.0D ? (int) y : (int) y - 1;
      int y1 = y0 + 1;
      int z0 = z > 0.0D ? (int) z : (int) z - 1;
      int z1 = z0 + 1;
      double xs;
      double ys;
      double zs;
      if (quality == NoiseQuality.FAST) {
         xs = x - (double) x0;
         ys = y - (double) y0;
         zs = z - (double) z0;
      } else if (quality == NoiseQuality.STANDARD) {
         xs = Utils.sCurve3(x - (double) x0);
         ys = Utils.sCurve3(y - (double) y0);
         zs = Utils.sCurve3(z - (double) z0);
      } else {
         xs = Utils.sCurve5(x - (double) x0);
         ys = Utils.sCurve5(y - (double) y0);
         zs = Utils.sCurve5(z - (double) z0);
      }

      double n0 = valueNoise3D(x0, y0, z0, seed);
      double n1 = valueNoise3D(x1, y0, z0, seed);
      double ix0 = Utils.linearInterp(n0, n1, xs);
      n0 = valueNoise3D(x0, y1, z0, seed);
      n1 = valueNoise3D(x1, y1, z0, seed);
      double ix1 = Utils.linearInterp(n0, n1, xs);
      double iy0 = Utils.linearInterp(ix0, ix1, ys);
      n0 = valueNoise3D(x0, y0, z1, seed);
      n1 = valueNoise3D(x1, y0, z1, seed);
      ix0 = Utils.linearInterp(n0, n1, xs);
      n0 = valueNoise3D(x0, y1, z1, seed);
      n1 = valueNoise3D(x1, y1, z1, seed);
      ix1 = Utils.linearInterp(n0, n1, xs);
      double iy1 = Utils.linearInterp(ix0, ix1, ys);
      return Utils.linearInterp(iy0, iy1, zs);
   }

   public static double valueNoise3D(int x, int y, int z, int seed) {
      return (double) intValueNoise3D(x, y, z, seed) / 2.147483647E9D;
   }
}
