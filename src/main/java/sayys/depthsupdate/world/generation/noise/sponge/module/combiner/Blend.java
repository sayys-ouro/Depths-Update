package sayys.depthsupdate.world.generation.noise.sponge.module.combiner;

import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Blend extends Module {
   public Blend() {
      super(3);
   }

   public Module getControlModule() {
      if (this.sourceModule[2] == null) {
         throw new NoModuleException();
      } else {
         return this.sourceModule[2];
      }
   }

   public void setControlModule(Module module) {
      if (module == null) {
         throw new IllegalArgumentException("Control Module cannot be null");
      } else {
         this.sourceModule[2] = module;
      }
   }

   public int getSourceModuleCount() {
      return 3;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else if (this.sourceModule[1] == null) {
         throw new NoModuleException();
      } else if (this.sourceModule[2] == null) {
         throw new NoModuleException();
      } else {
         double v0 = this.sourceModule[0].getValue(x, y, z);
         double v1 = this.sourceModule[1].getValue(x, y, z);
         double alpha = this.sourceModule[2].getValue(x, y, z);
         return Utils.linearInterp(v0, v1, alpha);
      }
   }
}
