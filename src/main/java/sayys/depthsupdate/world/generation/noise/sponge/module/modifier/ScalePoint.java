package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class ScalePoint extends Module {
   public static final double DEFAULT_SCALE_POINT_X = 1.0D;
   public static final double DEFAULT_SCALE_POINT_Y = 1.0D;
   public static final double DEFAULT_SCALE_POINT_Z = 1.0D;
   private double xScale = 1.0D;
   private double yScale = 1.0D;
   private double zScale = 1.0D;

   public ScalePoint() {
      super(1);
   }

   public double getXScale() {
      return this.xScale;
   }

   public void setXScale(double xScale) {
      this.xScale = xScale;
   }

   public double getYScale() {
      return this.yScale;
   }

   public void setYScale(double yScale) {
      this.yScale = yScale;
   }

   public double getZScale() {
      return this.zScale;
   }

   public void setZScale(double zScale) {
      this.zScale = zScale;
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         return this.sourceModule[0].getValue(x * this.xScale, y * this.yScale, z * this.zScale);
      }
   }
}
