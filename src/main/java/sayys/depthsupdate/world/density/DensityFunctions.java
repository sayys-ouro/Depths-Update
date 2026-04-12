package sayys.depthsupdate.world.density;

import java.util.Arrays;

import sayys.depthsupdate.world.noise.NoiseUtils;

/**
 * Factory class and all built-in DensityFunction implementations.
 * Faithful backport of modern Minecraft's DensityFunctions.
 */
public final class DensityFunctions {

    private DensityFunctions() {}

    // ===== Factory Methods =====

    public static DensityFunction constant(double value) {
        return new Constant(value);
    }

    public static DensityFunction zero() {
        return Constant.ZERO;
    }

    public static DensityFunction yClampedGradient(int fromY, int toY, double fromValue, double toValue) {
        return new YClampedGradient(fromY, toY, fromValue, toValue);
    }

    public static DensityFunction noise(DensityFunction.NoiseHolder noise, double xzScale, double yScale) {
        return new Noise(noise, xzScale, yScale);
    }

    public static DensityFunction noise(DensityFunction.NoiseHolder noise) {
        return noise(noise, 1.0, 1.0);
    }

    public static DensityFunction noise(DensityFunction.NoiseHolder noise, double yScale) {
        return noise(noise, 1.0, yScale);
    }

    public static DensityFunction shiftedNoise2d(DensityFunction shiftX, DensityFunction shiftZ,
                                                  double xzScale, DensityFunction.NoiseHolder noise) {
        return new ShiftedNoise(shiftX, zero(), shiftZ, xzScale, 0.0, noise);
    }

    public static DensityFunction rangeChoice(DensityFunction input, double minInclusive, double maxExclusive,
                                               DensityFunction whenInRange, DensityFunction whenOutOfRange) {
        return new RangeChoice(input, minInclusive, maxExclusive, whenInRange, whenOutOfRange);
    }

    public static DensityFunction add(DensityFunction f1, DensityFunction f2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.ADD, f1, f2);
    }

    public static DensityFunction mul(DensityFunction f1, DensityFunction f2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.MUL, f1, f2);
    }

    public static DensityFunction min(DensityFunction f1, DensityFunction f2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.MIN, f1, f2);
    }

    public static DensityFunction max(DensityFunction f1, DensityFunction f2) {
        return TwoArgumentSimpleFunction.create(TwoArgumentSimpleFunction.Type.MAX, f1, f2);
    }

    public static DensityFunction map(DensityFunction function, Mapped.Type type) {
        return Mapped.create(type, function);
    }

    public static DensityFunction spline(CubicSpline<DensityFunction.FunctionContext> spline) {
        return new Spline(spline);
    }

    public static DensityFunction interpolated(DensityFunction function) {
        return new Marker(Marker.Type.Interpolated, function);
    }

    public static DensityFunction flatCache(DensityFunction function) {
        return new Marker(Marker.Type.FlatCache, function);
    }

    public static DensityFunction cache2d(DensityFunction function) {
        return new Marker(Marker.Type.Cache2D, function);
    }

    public static DensityFunction cacheOnce(DensityFunction function) {
        return new Marker(Marker.Type.CacheOnce, function);
    }

    public static DensityFunction cacheAllInCell(DensityFunction function) {
        return new Marker(Marker.Type.CacheAllInCell, function);
    }

    public static DensityFunction weirdScaledSampler(DensityFunction input, DensityFunction.NoiseHolder noise,
                                                      WeirdScaledSampler.RarityValueMapper mapper) {
        return new WeirdScaledSampler(input, noise, mapper);
    }

    public static DensityFunction shiftA(DensityFunction.NoiseHolder noise) {
        return new ShiftA(noise);
    }

    public static DensityFunction shiftB(DensityFunction.NoiseHolder noise) {
        return new ShiftB(noise);
    }

    public static DensityFunction blendAlpha() {
        return BlendAlpha.INSTANCE;
    }

    public static DensityFunction blendOffset() {
        return BlendOffset.INSTANCE;
    }

    public static DensityFunction blendDensity(DensityFunction input) {
        return new BlendDensity(input);
    }

    public static DensityFunction findTopSurface(DensityFunction density, DensityFunction upperBound,
                                                  int lowerBound, int cellHeight) {
        return new FindTopSurface(density, upperBound, lowerBound, cellHeight);
    }

    public static DensityFunction lerp(DensityFunction alpha, DensityFunction first, DensityFunction second) {
        if (first instanceof Constant c) {
            return lerp(alpha, c.value, second);
        }
        DensityFunction alphaCached = cacheOnce(alpha);
        DensityFunction oneMinusAlpha = add(mul(alphaCached, constant(-1.0)), constant(1.0));
        return add(mul(first, oneMinusAlpha), mul(second, alphaCached));
    }

    public static DensityFunction lerp(DensityFunction factor, double first, DensityFunction second) {
        return add(mul(factor, add(second, constant(-first))), constant(first));
    }

    static DensityFunction mapFromUnitTo(DensityFunction function, double min, double max) {
        double middle = (min + max) * 0.5;
        double factor = (max - min) * 0.5;
        return add(constant(middle), mul(constant(factor), function));
    }

    public static DensityFunction mappedNoise(DensityFunction.NoiseHolder noise, double xzScale, double yScale,
                                                double minTarget, double maxTarget) {
        return mapFromUnitTo(new Noise(noise, xzScale, yScale), minTarget, maxTarget);
    }

    public static DensityFunction mappedNoise(DensityFunction.NoiseHolder noise, double yScale,
                                                double minTarget, double maxTarget) {
        return mappedNoise(noise, 1.0, yScale, minTarget, maxTarget);
    }

    public static DensityFunction mappedNoise(DensityFunction.NoiseHolder noise, double minTarget, double maxTarget) {
        return mappedNoise(noise, 1.0, 1.0, minTarget, maxTarget);
    }

    public static DensityFunction endIslands(long seed) {
        return new EndIslandDensityFunction(seed);
    }

    // ===== Implementation Classes =====

    // --- Constant ---
    public static final class Constant implements DensityFunction.SimpleFunction {
        static final Constant ZERO = new Constant(0.0);
        final double value;

        public Constant(double value) {
            this.value = value;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return this.value;
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            Arrays.fill(output, this.value);
        }

        @Override
        public double minValue() { return this.value; }

        @Override
        public double maxValue() { return this.value; }

        public double value() { return this.value; }
    }

    // --- YClampedGradient ---
    public static final class YClampedGradient implements DensityFunction.SimpleFunction {
        private final int fromY;
        private final int toY;
        private final double fromValue;
        private final double toValue;

        public YClampedGradient(int fromY, int toY, double fromValue, double toValue) {
            this.fromY = fromY;
            this.toY = toY;
            this.fromValue = fromValue;
            this.toValue = toValue;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return NoiseUtils.clampedMap((double) context.blockY(), (double) this.fromY, (double) this.toY, this.fromValue, this.toValue);
        }

        @Override
        public double minValue() { return Math.min(this.fromValue, this.toValue); }

        @Override
        public double maxValue() { return Math.max(this.fromValue, this.toValue); }
    }

    // --- Noise ---
    public static final class Noise implements DensityFunction {
        private final DensityFunction.NoiseHolder noise;
        private final double xzScale;
        private final double yScale;

        public Noise(DensityFunction.NoiseHolder noise, double xzScale, double yScale) {
            this.noise = noise;
            this.xzScale = xzScale;
            this.yScale = yScale;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return this.noise.getValue(
                    (double) context.blockX() * this.xzScale,
                    (double) context.blockY() * this.yScale,
                    (double) context.blockZ() * this.xzScale);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new Noise(this.noise, this.xzScale, this.yScale));
        }

        @Override
        public double minValue() { return -this.maxValue(); }

        @Override
        public double maxValue() { return this.noise.maxValue(); }

        public DensityFunction.NoiseHolder noise() { return noise; }
        public double xzScale() { return xzScale; }
        public double yScale() { return yScale; }
    }

    // --- ShiftedNoise ---
    public static final class ShiftedNoise implements DensityFunction {
        private final DensityFunction shiftX;
        private final DensityFunction shiftY;
        private final DensityFunction shiftZ;
        private final double xzScale;
        private final double yScale;
        private final DensityFunction.NoiseHolder noise;

        public ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ,
                            double xzScale, double yScale, DensityFunction.NoiseHolder noise) {
            this.shiftX = shiftX;
            this.shiftY = shiftY;
            this.shiftZ = shiftZ;
            this.xzScale = xzScale;
            this.yScale = yScale;
            this.noise = noise;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            double x = (double) context.blockX() * this.xzScale + this.shiftX.compute(context);
            double y = (double) context.blockY() * this.yScale + this.shiftY.compute(context);
            double z = (double) context.blockZ() * this.xzScale + this.shiftZ.compute(context);
            return this.noise.getValue(x, y, z);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new ShiftedNoise(
                    this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor),
                    this.xzScale, this.yScale, this.noise));
        }

        @Override
        public double minValue() { return -this.maxValue(); }

        @Override
        public double maxValue() { return this.noise.maxValue(); }
    }

    // --- RangeChoice ---
    public static final class RangeChoice implements DensityFunction {
        private final DensityFunction input;
        private final double minInclusive;
        private final double maxExclusive;
        private final DensityFunction whenInRange;
        private final DensityFunction whenOutOfRange;

        public RangeChoice(DensityFunction input, double minInclusive, double maxExclusive,
                           DensityFunction whenInRange, DensityFunction whenOutOfRange) {
            this.input = input;
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
            this.whenInRange = whenInRange;
            this.whenOutOfRange = whenOutOfRange;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            double inputValue = this.input.compute(context);
            return inputValue >= this.minInclusive && inputValue < this.maxExclusive
                    ? this.whenInRange.compute(context)
                    : this.whenOutOfRange.compute(context);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(output, contextProvider);
            for (int i = 0; i < output.length; ++i) {
                double v = output[i];
                if (v >= this.minInclusive && v < this.maxExclusive) {
                    output[i] = this.whenInRange.compute(contextProvider.forIndex(i));
                } else {
                    output[i] = this.whenOutOfRange.compute(contextProvider.forIndex(i));
                }
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new RangeChoice(
                    this.input.mapAll(visitor), this.minInclusive, this.maxExclusive,
                    this.whenInRange.mapAll(visitor), this.whenOutOfRange.mapAll(visitor)));
        }

        @Override
        public double minValue() { return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue()); }

        @Override
        public double maxValue() { return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue()); }
    }

    // --- Clamp ---
    public static final class Clamp implements DensityFunction {
        private final DensityFunction input;
        private final double min;
        private final double max;

        public Clamp(DensityFunction input, double min, double max) {
            this.input = input;
            this.min = min;
            this.max = max;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return NoiseUtils.clamp(this.input.compute(context), this.min, this.max);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(output, contextProvider);
            for (int i = 0; i < output.length; ++i) {
                output[i] = NoiseUtils.clamp(output[i], this.min, this.max);
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return new Clamp(this.input.mapAll(visitor), this.min, this.max);
        }

        @Override
        public double minValue() { return this.min; }

        @Override
        public double maxValue() { return this.max; }
    }

    // --- Mapped (unary transforms) ---
    public static final class Mapped implements DensityFunction {
        private final Type type;
        private final DensityFunction input;
        private final double minValue;
        private final double maxValue;

        private Mapped(Type type, DensityFunction input, double minValue, double maxValue) {
            this.type = type;
            this.input = input;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public static Mapped create(Type type, DensityFunction input) {
            double minValue = input.minValue();
            double maxValue = input.maxValue();
            double minImage = transform(type, minValue);
            double maxImage = transform(type, maxValue);

            if (type == Type.INVERT) {
                return minValue < 0.0 && maxValue > 0.0
                        ? new Mapped(type, input, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                        : new Mapped(type, input, maxImage, minImage);
            } else if (type == Type.ABS || type == Type.SQUARE) {
                return new Mapped(type, input, Math.max(0.0, minValue), Math.max(minImage, maxImage));
            } else {
                return new Mapped(type, input, minImage, maxImage);
            }
        }

        static double transform(Type type, double input) {
            return switch (type) {
                case ABS -> Math.abs(input);
                case SQUARE -> input * input;
                case CUBE -> input * input * input;
                case HALF_NEGATIVE -> input > 0.0 ? input : input * 0.5;
                case QUARTER_NEGATIVE -> input > 0.0 ? input : input * 0.25;
                case INVERT -> 1.0 / input;
                case SQUEEZE -> {
                    double c = NoiseUtils.clamp(input, -1.0, 1.0);
                    yield c / 2.0 - c * c * c / 24.0;
                }
            };
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return transform(this.type, this.input.compute(context));
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(output, contextProvider);
            for (int i = 0; i < output.length; ++i) {
                output[i] = transform(this.type, output[i]);
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return create(this.type, this.input.mapAll(visitor));
        }

        @Override
        public double minValue() { return this.minValue; }

        @Override
        public double maxValue() { return this.maxValue; }

        public Type type() { return type; }
        public DensityFunction input() { return input; }

        public enum Type {
            ABS, SQUARE, CUBE, HALF_NEGATIVE, QUARTER_NEGATIVE, INVERT, SQUEEZE
        }
    }

    // --- Marker (cache optimization hints) ---
    public static final class Marker implements DensityFunction {
        private final Type type;
        private final DensityFunction wrapped;

        public Marker(Type type, DensityFunction wrapped) {
            this.type = type;
            this.wrapped = wrapped;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return this.wrapped.compute(context);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.wrapped.fillArray(output, contextProvider);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new Marker(this.type, this.wrapped.mapAll(visitor)));
        }

        @Override
        public double minValue() { return this.wrapped.minValue(); }

        @Override
        public double maxValue() { return this.wrapped.maxValue(); }

        public Type type() { return type; }
        public DensityFunction wrapped() { return wrapped; }

        public enum Type {
            Interpolated, FlatCache, Cache2D, CacheOnce, CacheAllInCell
        }
    }

    // --- TwoArgumentSimpleFunction (binary operations) ---
    public interface TwoArgumentSimpleFunction extends DensityFunction {

        static DensityFunction create(Type type, DensityFunction argument1, DensityFunction argument2) {
            double min1 = argument1.minValue();
            double min2 = argument2.minValue();
            double max1 = argument1.maxValue();
            double max2 = argument2.maxValue();

            double minValue = switch (type) {
                case ADD -> min1 + min2;
                case MUL -> min1 > 0 && min2 > 0 ? min1 * min2
                        : (max1 < 0 && max2 < 0 ? max1 * max2 : Math.min(min1 * max2, max1 * min2));
                case MIN -> Math.min(min1, min2);
                case MAX -> Math.max(min1, min2);
            };

            double maxValue = switch (type) {
                case ADD -> max1 + max2;
                case MUL -> min1 > 0 && min2 > 0 ? max1 * max2
                        : (max1 < 0 && max2 < 0 ? min1 * min2 : Math.max(min1 * min2, max1 * max2));
                case MIN -> Math.min(max1, max2);
                case MAX -> Math.max(max1, max2);
            };

            if (type == Type.MUL || type == Type.ADD) {
                if (argument1 instanceof Constant c) {
                    return new MulOrAdd(
                            type == Type.ADD ? MulOrAdd.SpecificType.ADD : MulOrAdd.SpecificType.MUL,
                            argument2, minValue, maxValue, c.value);
                }
                if (argument2 instanceof Constant c) {
                    return new MulOrAdd(
                            type == Type.ADD ? MulOrAdd.SpecificType.ADD : MulOrAdd.SpecificType.MUL,
                            argument1, minValue, maxValue, c.value);
                }
            }

            return new Ap2(type, argument1, argument2, minValue, maxValue);
        }

        Type type();
        DensityFunction argument1();
        DensityFunction argument2();

        enum Type {
            ADD, MUL, MIN, MAX
        }
    }

    // --- Ap2 (general two-argument implementation) ---
    static final class Ap2 implements TwoArgumentSimpleFunction {
        private final TwoArgumentSimpleFunction.Type type;
        private final DensityFunction argument1;
        private final DensityFunction argument2;
        private final double minValue;
        private final double maxValue;

        Ap2(TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2,
            double minValue, double maxValue) {
            this.type = type;
            this.argument1 = argument1;
            this.argument2 = argument2;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            double v1 = this.argument1.compute(context);
            return switch (this.type) {
                case ADD -> v1 + this.argument2.compute(context);
                case MUL -> v1 == 0.0 ? 0.0 : v1 * this.argument2.compute(context);
                case MIN -> v1 < this.argument2.minValue() ? v1 : Math.min(v1, this.argument2.compute(context));
                case MAX -> v1 > this.argument2.maxValue() ? v1 : Math.max(v1, this.argument2.compute(context));
            };
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.argument1.fillArray(output, contextProvider);
            switch (this.type) {
                case ADD -> {
                    double[] v2 = new double[output.length];
                    this.argument2.fillArray(v2, contextProvider);
                    for (int i = 0; i < output.length; ++i) {
                        output[i] += v2[i];
                    }
                }
                case MUL -> {
                    for (int i = 0; i < output.length; ++i) {
                        double v = output[i];
                        output[i] = v == 0.0 ? 0.0 : v * this.argument2.compute(contextProvider.forIndex(i));
                    }
                }
                case MIN -> {
                    double min2 = this.argument2.minValue();
                    for (int i = 0; i < output.length; ++i) {
                        double v = output[i];
                        output[i] = v < min2 ? v : Math.min(v, this.argument2.compute(contextProvider.forIndex(i)));
                    }
                }
                case MAX -> {
                    double max2 = this.argument2.maxValue();
                    for (int i = 0; i < output.length; ++i) {
                        double v = output[i];
                        output[i] = v > max2 ? v : Math.max(v, this.argument2.compute(contextProvider.forIndex(i)));
                    }
                }
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(TwoArgumentSimpleFunction.create(
                    this.type, this.argument1.mapAll(visitor), this.argument2.mapAll(visitor)));
        }

        @Override
        public double minValue() { return this.minValue; }

        @Override
        public double maxValue() { return this.maxValue; }

        @Override
        public TwoArgumentSimpleFunction.Type type() { return this.type; }

        @Override
        public DensityFunction argument1() { return this.argument1; }

        @Override
        public DensityFunction argument2() { return this.argument2; }
    }

    // --- MulOrAdd (optimized when one argument is constant) ---
    static final class MulOrAdd implements TwoArgumentSimpleFunction {
        private final SpecificType specificType;
        private final DensityFunction input;
        private final double minValue;
        private final double maxValue;
        private final double argument;

        MulOrAdd(SpecificType specificType, DensityFunction input, double minValue, double maxValue, double argument) {
            this.specificType = specificType;
            this.input = input;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.argument = argument;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            double inputVal = this.input.compute(context);
            return switch (this.specificType) {
                case MUL -> inputVal * this.argument;
                case ADD -> inputVal + this.argument;
            };
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(output, contextProvider);
            for (int i = 0; i < output.length; ++i) {
                output[i] = switch (this.specificType) {
                    case MUL -> output[i] * this.argument;
                    case ADD -> output[i] + this.argument;
                };
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            DensityFunction function = this.input.mapAll(visitor);
            double min = function.minValue();
            double max = function.maxValue();
            double newMin, newMax;
            if (this.specificType == SpecificType.ADD) {
                newMin = min + this.argument;
                newMax = max + this.argument;
            } else if (this.argument >= 0.0) {
                newMin = min * this.argument;
                newMax = max * this.argument;
            } else {
                newMin = max * this.argument;
                newMax = min * this.argument;
            }
            return new MulOrAdd(this.specificType, function, newMin, newMax, this.argument);
        }

        @Override
        public double minValue() { return this.minValue; }

        @Override
        public double maxValue() { return this.maxValue; }

        @Override
        public TwoArgumentSimpleFunction.Type type() {
            return this.specificType == SpecificType.MUL
                    ? TwoArgumentSimpleFunction.Type.MUL
                    : TwoArgumentSimpleFunction.Type.ADD;
        }

        @Override
        public DensityFunction argument1() { return constant(this.argument); }

        @Override
        public DensityFunction argument2() { return this.input; }

        enum SpecificType { MUL, ADD }
    }

    // --- Spline ---
    public static final class Spline implements DensityFunction {
        private final CubicSpline<DensityFunction.FunctionContext> spline;

        public Spline(CubicSpline<DensityFunction.FunctionContext> spline) {
            this.spline = spline;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return (double) this.spline.apply(context);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new Spline(this.spline.mapAll(visitor)));
        }

        @Override
        public double minValue() { return (double) this.spline.minValue(); }

        @Override
        public double maxValue() { return (double) this.spline.maxValue(); }

        public CubicSpline<DensityFunction.FunctionContext> spline() { return spline; }
    }

    // --- WeirdScaledSampler ---
    public static final class WeirdScaledSampler implements DensityFunction {
        private final DensityFunction input;
        private final DensityFunction.NoiseHolder noise;
        private final RarityValueMapper rarityValueMapper;

        public WeirdScaledSampler(DensityFunction input, DensityFunction.NoiseHolder noise,
                                  RarityValueMapper rarityValueMapper) {
            this.input = input;
            this.noise = noise;
            this.rarityValueMapper = rarityValueMapper;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            double inputVal = this.input.compute(context);
            double rarity = this.rarityValueMapper.mapper.applyAsDouble(inputVal);
            return rarity * Math.abs(this.noise.getValue(
                    (double) context.blockX() / rarity,
                    (double) context.blockY() / rarity,
                    (double) context.blockZ() / rarity));
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(output, contextProvider);
            for (int i = 0; i < output.length; ++i) {
                DensityFunction.FunctionContext ctx = contextProvider.forIndex(i);
                double rarity = this.rarityValueMapper.mapper.applyAsDouble(output[i]);
                output[i] = rarity * Math.abs(this.noise.getValue(
                        (double) ctx.blockX() / rarity,
                        (double) ctx.blockY() / rarity,
                        (double) ctx.blockZ() / rarity));
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new WeirdScaledSampler(
                    this.input.mapAll(visitor), this.noise, this.rarityValueMapper));
        }

        @Override
        public double minValue() { return 0.0; }

        @Override
        public double maxValue() { return this.rarityValueMapper.maxRarity * this.noise.maxValue(); }

        public enum RarityValueMapper {
            TYPE1(DensityFunctions::getSpaghettiRarity3D, 2.0),
            TYPE2(DensityFunctions::getSpaghettiRarity2D, 3.0);

            final java.util.function.DoubleUnaryOperator mapper;
            final double maxRarity;

            RarityValueMapper(java.util.function.DoubleUnaryOperator mapper, double maxRarity) {
                this.mapper = mapper;
                this.maxRarity = maxRarity;
            }
        }
    }

    // --- ShiftNoise base ---
    private static double computeShift(DensityFunction.NoiseHolder noise, double x, double y, double z) {
        return noise.getValue(x * 0.25, y * 0.25, z * 0.25) * 4.0;
    }

    private static double shiftMaxValue(DensityFunction.NoiseHolder noise) {
        return noise.maxValue() * 4.0;
    }

    // --- ShiftA ---
    public static final class ShiftA implements DensityFunction.SimpleFunction {
        private final DensityFunction.NoiseHolder offsetNoise;

        public ShiftA(DensityFunction.NoiseHolder offsetNoise) {
            this.offsetNoise = offsetNoise;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return computeShift(this.offsetNoise, (double) context.blockX(), 0.0, (double) context.blockZ());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new ShiftA(this.offsetNoise));
        }

        @Override
        public double minValue() { return -shiftMaxValue(this.offsetNoise); }

        @Override
        public double maxValue() { return shiftMaxValue(this.offsetNoise); }
    }

    // --- ShiftB ---
    public static final class ShiftB implements DensityFunction.SimpleFunction {
        private final DensityFunction.NoiseHolder offsetNoise;

        public ShiftB(DensityFunction.NoiseHolder offsetNoise) {
            this.offsetNoise = offsetNoise;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return computeShift(this.offsetNoise, (double) context.blockZ(), (double) context.blockX(), 0.0);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new ShiftB(this.offsetNoise));
        }

        @Override
        public double minValue() { return -shiftMaxValue(this.offsetNoise); }

        @Override
        public double maxValue() { return shiftMaxValue(this.offsetNoise); }
    }

    // --- BlendAlpha ---
    public enum BlendAlpha implements DensityFunction.SimpleFunction {
        INSTANCE;

        @Override
        public double compute(DensityFunction.FunctionContext context) { return 1.0; }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            Arrays.fill(output, 1.0);
        }

        @Override
        public double minValue() { return 1.0; }

        @Override
        public double maxValue() { return 1.0; }
    }

    // --- BlendOffset ---
    public enum BlendOffset implements DensityFunction.SimpleFunction {
        INSTANCE;

        @Override
        public double compute(DensityFunction.FunctionContext context) { return 0.0; }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            Arrays.fill(output, 0.0);
        }

        @Override
        public double minValue() { return 0.0; }

        @Override
        public double maxValue() { return 0.0; }
    }

    // --- BlendDensity ---
    public static final class BlendDensity implements DensityFunction {
        private final DensityFunction input;

        public BlendDensity(DensityFunction input) {
            this.input = input;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            // In 1.12.2 we have no world blending, so pass through
            return this.input.compute(context);
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(output, contextProvider);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new BlendDensity(this.input.mapAll(visitor)));
        }

        @Override
        public double minValue() { return this.input.minValue(); }

        @Override
        public double maxValue() { return this.input.maxValue(); }

        public DensityFunction input() { return input; }
    }

    // --- FindTopSurface ---
    public static final class FindTopSurface implements DensityFunction {
        private final DensityFunction density;
        private final DensityFunction upperBound;
        private final int lowerBound;
        private final int cellHeight;

        public FindTopSurface(DensityFunction density, DensityFunction upperBound, int lowerBound, int cellHeight) {
            this.density = density;
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
            this.cellHeight = cellHeight;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            int topY = (int) Math.floor(this.upperBound.compute(context) / (double) this.cellHeight) * this.cellHeight;
            if (topY <= this.lowerBound) {
                return (double) this.lowerBound;
            }
            for (int blockY = topY; blockY >= this.lowerBound; blockY -= this.cellHeight) {
                if (this.density.compute(new DensityFunction.SinglePointContext(
                        context.blockX(), blockY, context.blockZ())) > 0.0) {
                    return (double) blockY;
                }
            }
            return (double) this.lowerBound;
        }

        @Override
        public void fillArray(double[] output, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return visitor.apply(new FindTopSurface(
                    this.density.mapAll(visitor), this.upperBound.mapAll(visitor),
                    this.lowerBound, this.cellHeight));
        }

        @Override
        public double minValue() { return (double) this.lowerBound; }

        @Override
        public double maxValue() { return Math.max((double) this.lowerBound, this.upperBound.maxValue()); }
    }

    // ===== Rarity functions for WeirdScaledSampler =====

    static double getSpaghettiRarity3D(double weirdness) {
        if (weirdness < -0.75) return 0.5;
        if (weirdness < -0.5) return 0.75;
        if (weirdness < 0.5) return 1.0;
        if (weirdness < 0.75) return 2.0;
        return 3.0;
    }

    static double getSpaghettiRarity2D(double weirdness) {
        if (weirdness < -0.75) return 0.5;
        if (weirdness < -0.5) return 0.75;
        if (weirdness < 0.5) return 1.0;
        if (weirdness < 0.75) return 1.5;
        return 2.0;
    }

    // --- EndIslandDensityFunction ---
    public static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
        private final sayys.depthsupdate.world.noise.SimplexNoise islandNoise;

        public EndIslandDensityFunction(long seed) {
            sayys.depthsupdate.world.noise.RandomSource random =
                    new sayys.depthsupdate.world.noise.LegacyRandomSource(seed);
            random.consumeCount(17292);
            this.islandNoise = new sayys.depthsupdate.world.noise.SimplexNoise(random);
        }

        private static float getHeightValue(sayys.depthsupdate.world.noise.SimplexNoise islandNoise,
                                             int sectionX, int sectionZ) {
            int chunkX = sectionX / 2;
            int chunkZ = sectionZ / 2;
            int subSectionX = sectionX % 2;
            int subSectionZ = sectionZ % 2;
            float doffs = 100.0f - (float) Math.sqrt(sectionX * sectionX + sectionZ * sectionZ) * 8.0f;
            doffs = Math.max(-100.0f, Math.min(80.0f, doffs));

            for (int xo = -12; xo <= 12; ++xo) {
                for (int zo = -12; zo <= 12; ++zo) {
                    long totalChunkX = (long) (chunkX + xo);
                    long totalChunkZ = (long) (chunkZ + zo);
                    if (totalChunkX * totalChunkX + totalChunkZ * totalChunkZ > 4096L
                            && islandNoise.getValue((double) totalChunkX, (double) totalChunkZ) < -0.9f) {
                        float islandSize = (Math.abs((float) totalChunkX) * 3439.0f
                                + Math.abs((float) totalChunkZ) * 147.0f) % 13.0f + 9.0f;
                        float xd = (float) (subSectionX - xo * 2);
                        float zd = (float) (subSectionZ - zo * 2);
                        float newDoffs = 100.0f - (float) Math.sqrt(xd * xd + zd * zd) * islandSize;
                        newDoffs = Math.max(-100.0f, Math.min(80.0f, newDoffs));
                        doffs = Math.max(doffs, newDoffs);
                    }
                }
            }

            return doffs;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return ((double) getHeightValue(this.islandNoise, context.blockX() / 8, context.blockZ() / 8) - 8.0) / 128.0;
        }

        @Override
        public double minValue() {
            return -0.84375;
        }

        @Override
        public double maxValue() {
            return 0.5625;
        }
    }
}
