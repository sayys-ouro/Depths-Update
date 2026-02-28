package sayys.depthsupdate.world.generation.noise.sponge.module.source;

import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Spheres extends Module {
   public static final double DEFAULT_SPHERES_FREQUENCY = 1.0D;
   private double frequency = 1.0D;

   public Spheres() {
      super(0);
   }

   public double getFrequency() {
      return this.frequency;
   }

   public void setFrequency(double frequency) {
      this.frequency = frequency;
   }

   public int getSourceModuleCount() {
      return 0;
   }

   public double getValue(double x, double y, double z) {
      double x1 = x * this.frequency;
      double y1 = y * this.frequency;
      double z1 = z * this.frequency;
      double distFromCenter = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
      double distFromSmallerSphere = distFromCenter - (double)Utils.floor(distFromCenter);
      double distFromLargerSphere = 1.0D - distFromSmallerSphere;
      double nearestDist = Math.min(distFromSmallerSphere, distFromLargerSphere);
      return 1.0D - nearestDist * 2.0D;
   }
}
