package sayys.depthsupdate.world.generation.noise.sponge.module.combiner;

import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Select extends Module {
   public static final double DEFAULT_SELECT_EDGE_FALLOFF = 0.0D;
   public static final double DEFAULT_SELECT_LOWER_BOUND = -1.0D;
   public static final double DEFAULT_SELECT_UPPER_BOUND = 1.0D;
   private double edgeFalloff = 0.0D;
   private double lowerBound = -1.0D;
   private double upperBound = 1.0D;

   public Select() {
      super(3);
   }

   public Module getControlModule() {
      if (this.sourceModule != null && this.sourceModule[2] != null) {
         return this.sourceModule[2];
      } else {
         throw new NoModuleException();
      }
   }

   public void setControlModule(Module m) {
      if (m == null) {
         throw new IllegalArgumentException("the module cannot be null");
      } else {
         this.sourceModule[2] = m;
      }
   }

   public double getEdgeFalloff() {
      return this.edgeFalloff;
   }

   public void setEdgeFalloff(double edgeFalloff) {
      double boundSize = this.upperBound - this.lowerBound;
      this.edgeFalloff = edgeFalloff > boundSize / 2.0D ? boundSize / 2.0D : edgeFalloff;
   }

   public double getLowerBound() {
      return this.lowerBound;
   }

   public double getUpperBound() {
      return this.upperBound;
   }

   public void setBounds(double upper, double lower) {
      if (lower > upper) {
         throw new IllegalArgumentException("lower must be less than upper");
      } else {
         this.lowerBound = lower;
         this.upperBound = upper;
         this.setEdgeFalloff(this.edgeFalloff);
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
         double controlValue = this.sourceModule[2].getValue(x, y, z);
         if (this.edgeFalloff > 0.0D) {
            if (controlValue < this.lowerBound - this.edgeFalloff) {
               return this.sourceModule[0].getValue(x, y, z);
            } else {
               double alpha;
               double lowerCurve;
               double upperCurve;
               if (controlValue < this.lowerBound + this.edgeFalloff) {
                  lowerCurve = this.lowerBound - this.edgeFalloff;
                  upperCurve = this.lowerBound + this.edgeFalloff;
                  alpha = Utils.sCurve3((controlValue - lowerCurve) / (upperCurve - lowerCurve));
                  return Utils.linearInterp(this.sourceModule[0].getValue(x, y, z), this.sourceModule[1].getValue(x, y, z), alpha);
               } else if (controlValue < this.upperBound - this.edgeFalloff) {
                  return this.sourceModule[1].getValue(x, y, z);
               } else if (controlValue < this.upperBound + this.edgeFalloff) {
                  lowerCurve = this.upperBound - this.edgeFalloff;
                  upperCurve = this.upperBound + this.edgeFalloff;
                  alpha = Utils.sCurve3((controlValue - lowerCurve) / (upperCurve - lowerCurve));
                  return Utils.linearInterp(this.sourceModule[1].getValue(x, y, z), this.sourceModule[0].getValue(x, y, z), alpha);
               } else {
                  return this.sourceModule[0].getValue(x, y, z);
               }
            }
         } else {
            return !(controlValue < this.lowerBound) && !(controlValue > this.upperBound) ? this.sourceModule[1].getValue(x, y, z) : this.sourceModule[0].getValue(x, y, z);
         }
      }
   }
}
