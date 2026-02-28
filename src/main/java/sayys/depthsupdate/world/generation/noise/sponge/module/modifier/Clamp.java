package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Clamp extends Module {
   public static final double DEFAULT_LOWER_BOUND = 0.0D;
   public static final double DEFAULT_UPPER_BOUND = 1.0D;
   private double lowerBound = 0.0D;
   private double upperBound = 1.0D;

   public Clamp() {
      super(1);
   }

   public double getLowerBound() {
      return this.lowerBound;
   }

   public void setLowerBound(double lowerBound) {
      this.lowerBound = lowerBound;
   }

   public double getUpperBound() {
      return this.upperBound;
   }

   public void setUpperBound(double upperBound) {
      this.upperBound = upperBound;
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         double value = this.sourceModule[0].getValue(x, y, z);
         if (value < this.lowerBound) {
            return this.lowerBound;
         } else {
            return value > this.upperBound ? this.upperBound : value;
         }
      }
   }
}
