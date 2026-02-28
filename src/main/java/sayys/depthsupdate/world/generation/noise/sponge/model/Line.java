package sayys.depthsupdate.world.generation.noise.sponge.model;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Line {
   private boolean attenuate = false;
   private Module module;
   private double x0 = 0.0D;
   private double x1 = 1.0D;
   private double y0 = 0.0D;
   private double y1 = 1.0D;
   private double z0 = 0.0D;
   private double z1 = 1.0D;

   public Line(Module module) {
      if (module == null) {
         throw new IllegalArgumentException("module cannot be null");
      } else {
         this.module = module;
      }
   }

   public boolean attenuate() {
      return this.attenuate;
   }

   public void setAttenuate(boolean att) {
      this.attenuate = att;
   }

   public void setStartPoint(double x, double y, double z) {
      this.x0 = x;
      this.y0 = y;
      this.z0 = z;
   }

   public void setEndPoint(double x, double y, double z) {
      this.x1 = x;
      this.y1 = y;
      this.z1 = z;
   }

   public Module getModule() {
      return this.module;
   }

   public void setModule(Module module) {
      if (module == null) {
         throw new IllegalArgumentException("module cannot be null");
      } else {
         this.module = module;
      }
   }

   public double getValue(double p) {
      if (this.module == null) {
         throw new NoModuleException();
      } else {
         double x = (this.x1 - this.x0) * p + this.x0;
         double y = (this.y1 - this.y0) * p + this.y0;
         double z = (this.z1 - this.z0) * p + this.z0;
         double value = this.module.getValue(x, y, z);
         return this.attenuate ? p * (1.0D - p) * 4.0D * value : value;
      }
   }
}
