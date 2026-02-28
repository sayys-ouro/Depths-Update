package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Abs extends Module {
   public Abs() {
      super(1);
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule == null) {
         throw new NoModuleException();
      } else {
         return Math.abs(this.sourceModule[0].getValue(x, y, z));
      }
   }
}
