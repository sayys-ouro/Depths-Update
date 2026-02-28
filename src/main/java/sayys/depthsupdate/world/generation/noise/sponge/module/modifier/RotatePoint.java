package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class RotatePoint extends Module {
   public static final double DEFAULT_ROTATE_X = 0.0D;
   public static final double DEFAULT_ROTATE_Y = 0.0D;
   public static final double DEFAULT_ROTATE_Z = 0.0D;
   private double xAngle = 0.0D;
   private double yAngle = 0.0D;
   private double zAngle = 0.0D;
   private double x1Matrix;
   private double x2Matrix;
   private double x3Matrix;
   private double y1Matrix;
   private double y2Matrix;
   private double y3Matrix;
   private double z1Matrix;
   private double z2Matrix;
   private double z3Matrix;

   public RotatePoint() {
      super(1);
      this.setAngles(0.0D, 0.0D, 0.0D);
   }

   public void setAngles(double x, double y, double z) {
      double xCos = Math.cos(Math.toRadians(x));
      double yCos = Math.cos(Math.toRadians(y));
      double zCos = Math.cos(Math.toRadians(z));
      double xSin = Math.sin(Math.toRadians(x));
      double ySin = Math.sin(Math.toRadians(y));
      double zSin = Math.sin(Math.toRadians(z));
      this.x1Matrix = ySin * xSin * zSin + yCos * zCos;
      this.y1Matrix = xCos * zSin;
      this.z1Matrix = ySin * zCos - yCos * xSin * zSin;
      this.x2Matrix = ySin * xSin * zCos - yCos * zSin;
      this.y2Matrix = xCos * zCos;
      this.z2Matrix = -yCos * xSin * zCos - ySin * zSin;
      this.x3Matrix = -ySin * xCos;
      this.y3Matrix = xSin;
      this.z3Matrix = yCos * xCos;
      this.xAngle = x;
      this.yAngle = y;
      this.zAngle = z;
   }

   public double getXAngle() {
      return this.xAngle;
   }

   public void setXAngle(double xAngle) {
      this.setAngles(xAngle, this.yAngle, this.zAngle);
   }

   public double getYAngle() {
      return this.yAngle;
   }

   public void setYAngle(double yAngle) {
      this.setAngles(this.xAngle, yAngle, this.zAngle);
   }

   public double getZAngle() {
      return this.zAngle;
   }

   public void setZAngle(double zAngle) {
      this.setAngles(this.xAngle, this.yAngle, zAngle);
   }

   public int getSourceModuleCount() {
      return 1;
   }

   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         double nx = this.x1Matrix * x + this.y1Matrix * y + this.z1Matrix * z;
         double ny = this.x2Matrix * x + this.y2Matrix * y + this.z2Matrix * z;
         double nz = this.x3Matrix * x + this.y3Matrix * y + this.z3Matrix * z;
         return this.sourceModule[0].getValue(nx, ny, nz);
      }
   }
}
