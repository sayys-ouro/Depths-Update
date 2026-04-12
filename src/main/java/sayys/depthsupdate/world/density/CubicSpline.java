package sayys.depthsupdate.world.density;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sayys.depthsupdate.world.noise.NoiseUtils;

/**
 * Hermite cubic spline interpolation parameterized by DensityFunctions.
 * Each knot's value can itself be a nested CubicSpline, enabling complex terrain shaping.
 * Faithful backport of modern Minecraft's CubicSpline (specialized for DensityFunction).
 *
 * @param <C> the context type (DensityFunction.FunctionContext)
 */
public interface CubicSpline<C> {

    float apply(C context);

    float minValue();

    float maxValue();

    CubicSpline<C> mapAll(CoordinateVisitor visitor);

    static <C> CubicSpline<C> constant(float value) {
        return new Constant<>(value);
    }

    static <C> Builder<C> builder(DensityFunction coordinate) {
        return new Builder<>(coordinate);
    }

    static <C> Builder<C> builder(DensityFunction coordinate, ToFloatFunction<Float> valueTransformer) {
        return new Builder<>(coordinate, valueTransformer);
    }

    // ===== Constant =====

    final class Constant<C> implements CubicSpline<C> {
        private final float value;

        public Constant(float value) {
            this.value = value;
        }

        @Override
        public float apply(C context) {
            return this.value;
        }

        @Override
        public float minValue() {
            return this.value;
        }

        @Override
        public float maxValue() {
            return this.value;
        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor visitor) {
            return this;
        }

        public float value() {
            return this.value;
        }
    }

    // ===== Builder =====

    final class Builder<C> {
        private final DensityFunction coordinate;
        private final ToFloatFunction<Float> valueTransformer;
        private final List<Float> locations = new ArrayList<>();
        private final List<CubicSpline<C>> values = new ArrayList<>();
        private final List<Float> derivatives = new ArrayList<>();

        Builder(DensityFunction coordinate) {
            this(coordinate, v -> v);
        }

        Builder(DensityFunction coordinate, ToFloatFunction<Float> valueTransformer) {
            this.coordinate = coordinate;
            this.valueTransformer = valueTransformer;
        }

        public Builder<C> addPoint(float location, float value) {
            return this.addPoint(location, new Constant<>(this.valueTransformer.apply(value)), 0.0f);
        }

        public Builder<C> addPoint(float location, float value, float derivative) {
            return this.addPoint(location, new Constant<>(this.valueTransformer.apply(value)), derivative);
        }

        public Builder<C> addPoint(float location, CubicSpline<C> value) {
            return this.addPoint(location, value, 0.0f);
        }

        public Builder<C> addPoint(float location, CubicSpline<C> value, float derivative) {
            if (!this.locations.isEmpty() && location <= this.locations.get(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            }
            this.locations.add(location);
            this.values.add(value);
            this.derivatives.add(derivative);
            return this;
        }

        public CubicSpline<C> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            }
            float[] locs = new float[this.locations.size()];
            float[] derivs = new float[this.derivatives.size()];
            for (int i = 0; i < locs.length; i++) {
                locs[i] = this.locations.get(i);
                derivs[i] = this.derivatives.get(i);
            }
            return Multipoint.create(this.coordinate, locs, List.copyOf(this.values), derivs);
        }
    }

    // ===== Multipoint =====

    final class Multipoint<C> implements CubicSpline<C> {
        private final DensityFunction coordinate;
        private final float[] locations;
        private final List<CubicSpline<C>> values;
        private final float[] derivatives;
        private final float minValue;
        private final float maxValue;

        Multipoint(DensityFunction coordinate, float[] locations, List<CubicSpline<C>> values,
                   float[] derivatives, float minValue, float maxValue) {
            validateSizes(locations, values, derivatives);
            this.coordinate = coordinate;
            this.locations = locations;
            this.values = values;
            this.derivatives = derivatives;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        static <C> Multipoint<C> create(DensityFunction coordinate, float[] locations,
                                         List<CubicSpline<C>> values, float[] derivatives) {
            validateSizes(locations, values, derivatives);
            int lastIndex = locations.length - 1;
            float minValue = Float.POSITIVE_INFINITY;
            float maxValue = Float.NEGATIVE_INFINITY;

            float minInput = (float) coordinate.minValue();
            float maxInput = (float) coordinate.maxValue();

            // Linear extension bounds before first knot
            if (minInput < locations[0]) {
                float edge1 = linearExtend(minInput, locations, values.get(0).minValue(), derivatives, 0);
                float edge2 = linearExtend(minInput, locations, values.get(0).maxValue(), derivatives, 0);
                minValue = Math.min(minValue, Math.min(edge1, edge2));
                maxValue = Math.max(maxValue, Math.max(edge1, edge2));
            }

            // Linear extension bounds after last knot
            if (maxInput > locations[lastIndex]) {
                float edge1 = linearExtend(maxInput, locations, values.get(lastIndex).minValue(), derivatives, lastIndex);
                float edge2 = linearExtend(maxInput, locations, values.get(lastIndex).maxValue(), derivatives, lastIndex);
                minValue = Math.min(minValue, Math.min(edge1, edge2));
                maxValue = Math.max(maxValue, Math.max(edge1, edge2));
            }

            // Value bounds from all knot values
            for (CubicSpline<C> value : values) {
                minValue = Math.min(minValue, value.minValue());
                maxValue = Math.max(maxValue, value.maxValue());
            }

            // Cubic interpolation bounds between knots
            for (int i = 0; i < lastIndex; i++) {
                float x1 = locations[i];
                float x2 = locations[i + 1];
                float xDiff = x2 - x1;
                CubicSpline<C> v1 = values.get(i);
                CubicSpline<C> v2 = values.get(i + 1);
                float min1 = v1.minValue();
                float max1 = v1.maxValue();
                float min2 = v2.minValue();
                float max2 = v2.maxValue();
                float d1 = derivatives[i];
                float d2 = derivatives[i + 1];
                if (d1 != 0.0f || d2 != 0.0f) {
                    float p1 = d1 * xDiff;
                    float p2 = d2 * xDiff;
                    float minA = p1 - max2 + min1;
                    float maxA = p1 - min2 + max1;
                    float minB = -p2 + min2 - max1;
                    float maxB = -p2 + max2 - min1;
                    float minLerp2 = Math.min(minA, minB);
                    float maxLerp2 = Math.max(maxA, maxB);
                    minValue = Math.min(minValue, Math.min(min1, min2) + 0.25f * minLerp2);
                    maxValue = Math.max(maxValue, Math.max(max1, max2) + 0.25f * maxLerp2);
                }
            }

            return new Multipoint<>(coordinate, locations, values, derivatives, minValue, maxValue);
        }

        private static float linearExtend(float input, float[] locations, float value,
                                           float[] derivatives, int index) {
            float derivative = derivatives[index];
            return derivative == 0.0f ? value : value + derivative * (input - locations[index]);
        }

        private static <C> void validateSizes(float[] locations, List<CubicSpline<C>> values, float[] derivatives) {
            if (locations.length != values.size() || locations.length != derivatives.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: "
                        + locations.length + " " + values.size() + " " + derivatives.length);
            }
            if (locations.length == 0) {
                throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
            }
        }

        @Override
        public float apply(C context) {
            float input = (float) this.coordinate.compute((DensityFunction.FunctionContext) context);
            int start = findIntervalStart(this.locations, input);
            int lastIndex = this.locations.length - 1;

            if (start < 0) {
                return linearExtend(input, this.locations, this.values.get(0).apply(context),
                        this.derivatives, 0);
            } else if (start == lastIndex) {
                return linearExtend(input, this.locations, this.values.get(lastIndex).apply(context),
                        this.derivatives, lastIndex);
            } else {
                float x1 = this.locations[start];
                float x2 = this.locations[start + 1];
                float t = (input - x1) / (x2 - x1);
                float y1 = this.values.get(start).apply(context);
                float y2 = this.values.get(start + 1).apply(context);
                float d1 = this.derivatives[start];
                float d2 = this.derivatives[start + 1];
                float a = d1 * (x2 - x1) - (y2 - y1);
                float b = -d2 * (x2 - x1) + (y2 - y1);
                return (float) NoiseUtils.lerp(t, y1, y2) + t * (1.0f - t) * (float) NoiseUtils.lerp(t, a, b);
            }
        }

        private static int findIntervalStart(float[] locations, float input) {
            return binarySearch(0, locations.length, i -> input < locations[i]) - 1;
        }

        /**
         * Binary search finding the first index where predicate is true.
         * Returns high if predicate is never true.
         */
        private static int binarySearch(int low, int high, java.util.function.IntPredicate predicate) {
            int range = high - low;
            while (range > 0) {
                int half = range / 2;
                int mid = low + half;
                if (predicate.test(mid)) {
                    range = half;
                } else {
                    low = mid + 1;
                    range -= half + 1;
                }
            }
            return low;
        }

        @Override
        public float minValue() {
            return this.minValue;
        }

        @Override
        public float maxValue() {
            return this.maxValue;
        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor visitor) {
            DensityFunction mappedCoordinate = visitor.visit(this.coordinate);
            List<CubicSpline<C>> mappedValues = this.values.stream()
                    .map(v -> v.mapAll(visitor))
                    .toList();
            return create(mappedCoordinate, this.locations, mappedValues, this.derivatives);
        }

        public DensityFunction coordinate() {
            return this.coordinate;
        }

        public float[] locations() {
            return this.locations;
        }

        public List<CubicSpline<C>> values() {
            return this.values;
        }

        public float[] derivatives() {
            return this.derivatives;
        }
    }

    // ===== CoordinateVisitor =====

    interface CoordinateVisitor {
        DensityFunction visit(DensityFunction input);
    }

    // ===== ToFloatFunction =====

    @FunctionalInterface
    interface ToFloatFunction<T> {
        float apply(T value);
    }
}
