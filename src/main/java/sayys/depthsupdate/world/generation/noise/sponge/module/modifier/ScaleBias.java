package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class ScaleBias extends Module {
   public static final double DEFAULT_BIAS = 0.0D;
   public static final double DEFAULT_SCALE = 1.0D;
   private double bias = 0.0D;
   private double scale = 1.0D;

   public ScaleBias() {
      super(1);
   }

   public double getBias() {
      return this.bias;
   }

   public void setBias(double bias) {
      this.bias = bias;
   }

   public double getScale() {
      return this.scale;
   }

   public void setScale(double scale) {
      this.scale = scale;
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         return this.sourceModule[0].getValue(x, y, z) * this.scale + this.bias;
      }
   }
}
