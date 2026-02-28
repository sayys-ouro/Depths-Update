package sayys.depthsupdate.world.generation.noise.sponge.model;

import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Sphere {
   private Module module;

   public Sphere(Module module) {
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

   public double getValue(double lat, double lon) {
      if (this.module == null) {
         throw new NoModuleException();
      } else {
         double[] vec = Utils.latLonToXYZ(lat, lon);
         return this.module.getValue(vec[0], vec[1], vec[2]);
      }
   }
}
