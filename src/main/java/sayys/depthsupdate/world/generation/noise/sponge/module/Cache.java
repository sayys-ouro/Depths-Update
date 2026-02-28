package sayys.depthsupdate.world.generation.noise.sponge.module;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;

public class Cache extends Module {
   private double cachedValue;
   private boolean isCached = false;
   private double xCache;
   private double yCache;
   private double zCache;

   public Cache() {
      super(1);
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public void setSourceModule(int index, Module sourceModule) {
      super.setSourceModule(index, sourceModule);
      this.isCached = false;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         if (!this.isCached || x != this.xCache || y != this.yCache || z != this.zCache) {
            this.cachedValue = this.sourceModule[0].getValue(x, y, z);
            this.xCache = x;
            this.yCache = y;
            this.zCache = z;
         }

         this.isCached = true;
         return this.cachedValue;
      }
   }
}
