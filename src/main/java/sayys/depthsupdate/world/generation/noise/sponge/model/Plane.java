package sayys.depthsupdate.world.generation.noise.sponge.model;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Plane {
   private Module module;

   public Plane(Module module) {
      if (module == null) {
         throw new IllegalArgumentException("module cannot be null");
      } else {
         this.module = module;
      }
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

   public double getValue(double x, double z) {
      if (this.module == null) {
         throw new NoModuleException();
      } else {
         return this.module.getValue(x, 0.0D, z);
      }
   }
}
