package sayys.depthsupdate.world.density;

/**
 * Core interface for composable density functions that drive terrain generation.
 * Every function in the generation pipeline implements this interface.
 * Faithful backport of modern Minecraft's DensityFunction.
 */
public interface DensityFunction {

    double compute(FunctionContext context);

    void fillArray(double[] output, ContextProvider contextProvider);

    DensityFunction mapAll(Visitor visitor);

    double minValue();

    double maxValue();

    default DensityFunction clamp(double min, double max) {
        return new DensityFunctions.Clamp(this, min, max);
    }

    default DensityFunction abs() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
    }

    default DensityFunction square() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
    }

    default DensityFunction cube() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
    }

    default DensityFunction halfNegative() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
    }

    default DensityFunction quarterNegative() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
    }

    default DensityFunction invert() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.INVERT);
    }

    default DensityFunction squeeze() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
    }

    /**
     * Provides block coordinates for a single point evaluation.
     */
    interface FunctionContext {
        int blockX();
        int blockY();
        int blockZ();
    }

    /**
     * Immutable single-point context implementation.
     */
    final class SinglePointContext implements FunctionContext {
        private final int blockX;
        private final int blockY;
        private final int blockZ;

        public SinglePointContext(int blockX, int blockY, int blockZ) {
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
        }

        @Override
        public int blockX() { return blockX; }

        @Override
        public int blockY() { return blockY; }

        @Override
        public int blockZ() { return blockZ; }
    }

    /**
     * A DensityFunction that doesn't need special fillArray or mapAll behavior.
     */
    interface SimpleFunction extends DensityFunction {
        @Override
        default void fillArray(double[] output, ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        default DensityFunction mapAll(Visitor visitor) {
            return visitor.apply(this);
        }
    }

    /**
     * Visitor for tree transformations (cache injection, etc.).
     */
    interface Visitor extends CubicSpline.CoordinateVisitor {
        DensityFunction apply(DensityFunction input);

        @Override
        default DensityFunction visit(DensityFunction input) {
            return apply(input);
        }
    }

    /**
     * Provides FunctionContext for array-based bulk evaluation.
     */
    interface ContextProvider {
        FunctionContext forIndex(int index);
        void fillAllDirectly(double[] output, DensityFunction function);
    }

    /**
     * Wraps a NormalNoise instance for use in density functions.
     */
    final class NoiseHolder {
        private final sayys.depthsupdate.world.noise.NormalNoise noise;

        public NoiseHolder(sayys.depthsupdate.world.noise.NormalNoise noise) {
            this.noise = noise;
        }

        public double getValue(double x, double y, double z) {
            return this.noise == null ? 0.0 : this.noise.getValue(x, y, z);
        }

        public double maxValue() {
            return this.noise == null ? 2.0 : this.noise.maxValue();
        }

        public sayys.depthsupdate.world.noise.NormalNoise noise() {
            return this.noise;
        }
    }
}
