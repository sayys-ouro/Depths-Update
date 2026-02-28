package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class TranslatePoint extends Module {
   public static final double DEFAULT_TRANSLATE_POINT_X = 0.0D;
   public static final double DEFAULT_TRANSLATE_POINT_Y = 0.0D;
   public static final double DEFAULT_TRANSLATE_POINT_Z = 0.0D;
   private double xTranslation = 0.0D;
   private double yTranslation = 0.0D;
   private double zTranslation = 0.0D;

   public TranslatePoint() {
      super(1);
   }

   public double getXTranslation() {
      return this.xTranslation;
   }

   public void setXTranslation(double xTranslation) {
      this.xTranslation = xTranslation;
   }

   public double getYTranslation() {
      return this.yTranslation;
   }

   public void setYTranslation(double yTranslation) {
      this.yTranslation = yTranslation;
   }

   public double getZTranslation() {
      return this.zTranslation;
   }

   public void setZTranslation(double zTranslation) {
      this.zTranslation = zTranslation;
   }

   public void setTranslations(double x, double y, double z) {
      this.setXTranslation(x);
      this.setYTranslation(y);
      this.setZTranslation(z);
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         return this.sourceModule[0].getValue(x + this.xTranslation, y + this.yTranslation, z + this.zTranslation);
      }
   }
}
