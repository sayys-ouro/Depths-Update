package sayys.depthsupdate.world.generation.noise.sponge.module.combiner;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Min extends Module {
   public Min() {
      super(2);
   }

   public int getSourceModuleCount() {
      return 2;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else if (this.sourceModule[1] == null) {
         throw new NoModuleException();
      } else {
         double v0 = this.sourceModule[0].getValue(x, y, z);
         double v1 = this.sourceModule[1].getValue(x, y, z);
         return Math.min(v0, v1);
      }
   }
}
