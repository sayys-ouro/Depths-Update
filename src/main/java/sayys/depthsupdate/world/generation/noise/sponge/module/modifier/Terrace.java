package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Terrace extends Module {
   private int controlPointCount = 0;
   private boolean invertTerraces = false;
   private double[] controlPoints = new double[0];

   public Terrace() {
      super(1);
   }

   public boolean isInvertTerraces() {
      return this.invertTerraces;
   }

   public void setInvertTerraces(boolean invertTerraces) {
      this.invertTerraces = invertTerraces;
   }

   public int getControlPointCount() {
      return this.controlPointCount;
   }

   public double[] getControlPoints() {
      return this.controlPoints;
   }

   public void addControlPoint(double value) {
      int insertionPos = this.findInsertionPos(value);
      this.insertAtPos(insertionPos, value);
   }

   public void clearAllControlPoints() {
      this.controlPoints = null;
      this.controlPointCount = 0;
   }

   public void makeControlPoints(int controlPointCount) {
      if (controlPointCount < 2) {
         throw new IllegalArgumentException("Must have more than 2 control points");
      } else {
         this.clearAllControlPoints();
         double terraceStep = 2.0D / ((double)controlPointCount - 1.0D);
         double curValue = -1.0D;

         for(int i = 0; i < controlPointCount; ++i) {
            this.addControlPoint(curValue);
            curValue += terraceStep;
         }

      }
   }

   private int findInsertionPos(double value) {
      int insertionPos;
      for(insertionPos = 0; insertionPos < this.controlPointCount && !(value < this.controlPoints[insertionPos]); ++insertionPos) {
         if (value == this.controlPoints[insertionPos]) {
            throw new IllegalArgumentException("Value must be unique");
         }
      }

      return insertionPos;
   }

   private void insertAtPos(int insertionPos, double value) {
      double[] newControlPoints = new double[this.controlPointCount + 1];

      for(int i = 0; i < this.controlPointCount; ++i) {
         if (i < insertionPos) {
            newControlPoints[i] = this.controlPoints[i];
         } else {
            newControlPoints[i + 1] = this.controlPoints[i];
         }
      }

      this.controlPoints = newControlPoints;
      ++this.controlPointCount;
      this.controlPoints[insertionPos] = value;
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         double sourceModuleValue = this.sourceModule[0].getValue(x, y, z);

         int indexPos;
         for(indexPos = 0; indexPos < this.controlPointCount && !(sourceModuleValue < this.controlPoints[indexPos]); ++indexPos) {
         }

         int index0 = Utils.clamp(indexPos - 1, 0, this.controlPointCount - 1);
         int index1 = Utils.clamp(indexPos, 0, this.controlPointCount - 1);
         if (index0 == index1) {
            return this.controlPoints[index1];
         } else {
            double value0 = this.controlPoints[index0];
            double value1 = this.controlPoints[index1];
            double alpha = (sourceModuleValue - value0) / (value1 - value0);
            if (this.invertTerraces) {
               alpha = 1.0D - alpha;
               double temp = value0;
               value0 = value1;
               value1 = temp;
            }

            alpha *= alpha;
            return Utils.linearInterp(value0, value1, alpha);
         }
      }
   }
}
