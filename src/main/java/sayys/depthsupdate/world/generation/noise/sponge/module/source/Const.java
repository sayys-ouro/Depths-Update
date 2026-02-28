package sayys.depthsupdate.world.generation.noise.sponge.module.source;

import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Const extends Module {
   public static final double DEFAULT_VALUE = 0.0D;
   private double value = 0.0D;

   public Const() {
      super(0);
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
   }

   public int getSourceModuleCount() {
      return 0;
   }

   public double getValue(double x, double y, double z) {
      return this.value;
   }
}
