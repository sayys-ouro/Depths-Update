package sayys.depthsupdate.world.generation.noise.sponge.module;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;

public abstract class Module {
   protected Module[] sourceModule = null;

   public Module(int sourceModuleCount) {
      if (sourceModuleCount > 0) {
         this.sourceModule = new Module[sourceModuleCount];

         for(int i = 0; i < sourceModuleCount; ++i) {
            this.sourceModule[i] = null;
         }
      } else {
         this.sourceModule = null;
      }

   }

   public Module getSourceModule(int index) {
      if (index < this.getSourceModuleCount() && index >= 0 && this.sourceModule[index] != null) {
         return this.sourceModule[index];
      } else {
         throw new NoModuleException();
      }
   }

   public void setSourceModule(int index, Module sourceModule) {
      if (this.sourceModule != null) {
         if (index < this.getSourceModuleCount() && index >= 0) {
            this.sourceModule[index] = sourceModule;
         } else {
            throw new IllegalArgumentException("Index must be between 0 and GetSourceModuleCount()");
         }
      }
   }

   public abstract int getSourceModuleCount();

   public abstract double getValue(double var1, double var3, double var5);
}
