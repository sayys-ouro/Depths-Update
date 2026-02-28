package sayys.depthsupdate.world.generation.noise.sponge.module.source;

import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Checkerboard extends Module {
   public Checkerboard() {
      super(0);
   }

   public int getSourceModuleCount() {
      return 0;
   }

   public double getValue(double x, double y, double z) {
      int ix = Utils.floor(Utils.makeInt32Range(x));
      int iy = Utils.floor(Utils.makeInt32Range(y));
      int iz = Utils.floor(Utils.makeInt32Range(z));
      return (ix & 1 ^ iy & 1 ^ iz & 1) != 0 ? 0.0D : 1.0D;
   }
}
