package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;
import java.util.ArrayList;
import java.util.List;

public class Curve extends Module {
   private final List<Curve.ControlPoint> controlPoints = new ArrayList();

   public Curve() {
      super(1);
   }

   public void addControlPoint(double inputValue, double outputValue) {
      int index = this.findInsertionPos(inputValue);
      this.insertAtPos(index, inputValue, outputValue);
   }

   public Curve.ControlPoint[] getControlPoints() {
      return (Curve.ControlPoint[])((Curve.ControlPoint[])this.controlPoints.toArray());
   }

   public void clearAllControlPoints() {
      this.controlPoints.clear();
   }

   private int findInsertionPos(double inputValue) {
      int insertionPos;
      for(insertionPos = 0; insertionPos < this.controlPoints.size() && !(inputValue < ((Curve.ControlPoint)this.controlPoints.get(insertionPos)).inputValue); ++insertionPos) {
         if (inputValue == ((Curve.ControlPoint)this.controlPoints.get(insertionPos)).inputValue) {
            throw new IllegalArgumentException("inputValue must be unique");
         }
      }

      return insertionPos;
   }

   private void insertAtPos(int insertionPos, double inputValue, double outputValue) {
      Curve.ControlPoint newPoint = new Curve.ControlPoint();
      newPoint.inputValue = inputValue;
      newPoint.outputValue = outputValue;
      this.controlPoints.add(insertionPos, newPoint);
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         int size = this.controlPoints.size();
         if (size < 4) {
            throw new RuntimeException("Curve module must have at least 4 control points");
         } else {
            double sourceModuleValue = this.sourceModule[0].getValue(x, y, z);

            int indexPos;
            for(indexPos = 0; indexPos < size && !(sourceModuleValue < ((Curve.ControlPoint)this.controlPoints.get(indexPos)).inputValue); ++indexPos) {
            }

            int lastIndex = size - 1;
            int index0 = Utils.clamp(indexPos - 2, 0, lastIndex);
            int index1 = Utils.clamp(indexPos - 1, 0, lastIndex);
            int index2 = Utils.clamp(indexPos, 0, lastIndex);
            int index3 = Utils.clamp(indexPos + 1, 0, lastIndex);
            if (index1 == index2) {
               return ((Curve.ControlPoint)this.controlPoints.get(index1)).outputValue;
            } else {
               double input0 = ((Curve.ControlPoint)this.controlPoints.get(index1)).inputValue;
               double input1 = ((Curve.ControlPoint)this.controlPoints.get(index2)).inputValue;
               double alpha = (sourceModuleValue - input0) / (input1 - input0);
               return Utils.cubicInterp(((Curve.ControlPoint)this.controlPoints.get(index0)).outputValue, ((Curve.ControlPoint)this.controlPoints.get(index1)).outputValue, ((Curve.ControlPoint)this.controlPoints.get(index2)).outputValue, ((Curve.ControlPoint)this.controlPoints.get(index3)).outputValue, alpha);
            }
         }
      }
   }

   public static class ControlPoint {
      private double inputValue;
      private double outputValue;
   }
}
