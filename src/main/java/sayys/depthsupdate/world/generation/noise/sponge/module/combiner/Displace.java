package sayys.depthsupdate.world.generation.noise.sponge.module.combiner;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Displace extends Module {
   public Displace() {
      super(4);
   }

   public int getSourceModuleCount() {
      return 4;
   }

   public Module getXDisplaceModule() {
      if (this.sourceModule != null && this.sourceModule[1] != null) {
         return this.sourceModule[1];
      } else {
         throw new NoModuleException();
      }
   }

   public Module getYDisplaceModule() {
      if (this.sourceModule != null && this.sourceModule[2] != null) {
         return this.sourceModule[2];
      } else {
         throw new NoModuleException();
      }
   }

   public Module getZDisplaceModule() {
      if (this.sourceModule != null && this.sourceModule[3] != null) {
         return this.sourceModule[3];
      } else {
         throw new NoModuleException();
      }
   }

   public void setXDisplaceModule(Module x) {
      if (x == null) {
         throw new IllegalArgumentException("x cannot be null");
      } else {
         this.sourceModule[1] = x;
      }
   }

   public void setYDisplaceModule(Module y) {
      if (y == null) {
         throw new IllegalArgumentException("y cannot be null");
      } else {
         this.sourceModule[2] = y;
      }
   }

   public void setZDisplaceModule(Module z) {
      if (z == null) {
         throw new IllegalArgumentException("z cannot be null");
      } else {
         this.sourceModule[3] = z;
      }
   }

   public void setDisplaceModules(Module x, Module y, Module z) {
      this.setXDisplaceModule(x);
      this.setYDisplaceModule(y);
      this.setZDisplaceModule(z);
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else if (this.sourceModule[1] == null) {
         throw new NoModuleException();
      } else if (this.sourceModule[2] == null) {
         throw new NoModuleException();
      } else if (this.sourceModule[3] == null) {
         throw new NoModuleException();
      } else {
         double xDisplace = x + this.sourceModule[1].getValue(x, y, z);
         double yDisplace = y + this.sourceModule[2].getValue(x, y, z);
         double zDisplace = z + this.sourceModule[3].getValue(x, y, z);
         return this.sourceModule[0].getValue(xDisplace, yDisplace, zDisplace);
      }
   }
}
