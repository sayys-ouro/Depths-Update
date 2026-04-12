package sayys.depthsupdate.world.surface;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import sayys.depthsupdate.world.density.NoiseChunk;
import sayys.depthsupdate.world.noise.NormalNoise;
import sayys.depthsupdate.world.noise.NoiseUtils;
import sayys.depthsupdate.world.noise.PositionalRandomFactory;
import sayys.depthsupdate.world.noise.RandomSource;

/**
 * Composable surface rule system. Replaces hardcoded per-biome surface generation.
 * Faithful backport of vanilla SurfaceRules, adapted for 1.12.2 (IBlockState, no codecs).
 */
public class SurfaceRules {

    // ===== Pre-built stone depth conditions =====
    public static final ConditionSource ON_FLOOR = stoneDepthCheck(0, false, CaveSurface.FLOOR);
    public static final ConditionSource UNDER_FLOOR = stoneDepthCheck(0, true, CaveSurface.FLOOR);
    public static final ConditionSource DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
    public static final ConditionSource VERY_DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
    public static final ConditionSource ON_CEILING = stoneDepthCheck(0, false, CaveSurface.CEILING);
    public static final ConditionSource UNDER_CEILING = stoneDepthCheck(0, true, CaveSurface.CEILING);

    // ===== Condition factories =====

    public static ConditionSource stoneDepthCheck(int offset, boolean addSurfaceDepth, CaveSurface surfaceType) {
        return stoneDepthCheck(offset, addSurfaceDepth, 0, surfaceType);
    }

    public static ConditionSource stoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) {
        return new StoneDepthCheck(offset, addSurfaceDepth, secondaryDepthRange, surfaceType);
    }

    public static ConditionSource not(ConditionSource target) {
        return ctx -> {
            Condition inner = target.apply(ctx);
            return () -> !inner.test();
        };
    }

    public static ConditionSource yBlockCheck(VerticalAnchor anchor, int surfaceDepthMultiplier) {
        return new YConditionSource(anchor, surfaceDepthMultiplier, false);
    }

    public static ConditionSource yStartCheck(VerticalAnchor anchor, int surfaceDepthMultiplier) {
        return new YConditionSource(anchor, surfaceDepthMultiplier, true);
    }

    public static ConditionSource waterBlockCheck(int offset, int surfaceDepthMultiplier) {
        return new WaterConditionSource(offset, surfaceDepthMultiplier, false);
    }

    public static ConditionSource waterStartCheck(int offset, int surfaceDepthMultiplier) {
        return new WaterConditionSource(offset, surfaceDepthMultiplier, true);
    }

    public static ConditionSource isBiome(Biome... biomes) {
        Set<Biome> biomeSet = Set.of(biomes);
        return ctx -> new LazyXZCondition(ctx) {
            @Override
            protected boolean compute() {
                Biome b = this.context.getBiome();
                return b != null && biomeSet.contains(b);
            }
        };
    }

    public static ConditionSource noiseCondition(NormalNoise noise, double minThreshold) {
        return noiseCondition(noise, minThreshold, Double.MAX_VALUE);
    }

    public static ConditionSource noiseCondition(NormalNoise noise, double minThreshold, double maxThreshold) {
        return ctx -> new LazyXZCondition(ctx) {
            @Override
            protected boolean compute() {
                double val = noise.getValue(this.context.blockX, 0.0, this.context.blockZ);
                return val >= minThreshold && val <= maxThreshold;
            }
        };
    }

    public static ConditionSource verticalGradient(String randomName, VerticalAnchor trueAtAndBelow,
                                                    VerticalAnchor falseAtAndAbove, PositionalRandomFactory randomFactory) {
        return ctx -> {
            int trueAt = trueAtAndBelow.resolveY(ctx.minY, ctx.maxY);
            int falseAt = falseAtAndAbove.resolveY(ctx.minY, ctx.maxY);
            return new LazyYCondition(ctx) {
                @Override
                protected boolean compute() {
                    int y = this.context.blockY;
                    if (y <= trueAt) return true;
                    if (y >= falseAt) return false;
                    double gradient = NoiseUtils.map(y, trueAt, falseAt, 1.0, 0.0);
                    RandomSource random = randomFactory.at(this.context.blockX, y, this.context.blockZ);
                    return random.nextDouble() < gradient;
                }
            };
        };
    }

    public static ConditionSource steep() {
        return ctx -> ctx.steep;
    }

    public static ConditionSource hole() {
        return ctx -> ctx.hole;
    }

    public static ConditionSource abovePreliminarySurface() {
        return ctx -> () -> ctx.blockY >= ctx.getMinSurfaceLevel();
    }

    public static ConditionSource temperature() {
        return ctx -> ctx.temperature;
    }

    // ===== Rule factories =====

    public static RuleSource ifTrue(ConditionSource condition, RuleSource next) {
        return ctx -> {
            Condition cond = condition.apply(ctx);
            SurfaceRule rule = next.apply(ctx);
            return (x, y, z) -> cond.test() ? rule.tryApply(x, y, z) : null;
        };
    }

    public static RuleSource sequence(RuleSource... rules) {
        if (rules.length == 0) throw new IllegalArgumentException("Need at least 1 rule for a sequence");
        if (rules.length == 1) return rules[0];
        List<RuleSource> ruleList = Arrays.asList(rules);
        return ctx -> {
            SurfaceRule[] compiled = ruleList.stream()
                    .map(r -> r.apply(ctx))
                    .toArray(SurfaceRule[]::new);
            return (x, y, z) -> {
                for (SurfaceRule rule : compiled) {
                    IBlockState result = rule.tryApply(x, y, z);
                    if (result != null) return result;
                }
                return null;
            };
        };
    }

    public static RuleSource state(IBlockState blockState) {
        return ctx -> (x, y, z) -> blockState;
    }

    public static RuleSource bandlands(SurfaceSystem system) {
        return ctx -> system::getBand;
    }

    // ===== Core interfaces =====

    @FunctionalInterface
    public interface ConditionSource {
        Condition apply(Context context);
    }

    @FunctionalInterface
    public interface Condition {
        boolean test();
    }

    @FunctionalInterface
    public interface RuleSource {
        SurfaceRule apply(Context context);
    }

    @FunctionalInterface
    public interface SurfaceRule {
        IBlockState tryApply(int blockX, int blockY, int blockZ);
    }

    // ===== CaveSurface enum =====

    public enum CaveSurface {
        FLOOR, CEILING
    }

    // ===== Condition implementations =====

    private static class StoneDepthCheck implements ConditionSource {
        private final int offset;
        private final boolean addSurfaceDepth;
        private final int secondaryDepthRange;
        private final CaveSurface surfaceType;

        StoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) {
            this.offset = offset;
            this.addSurfaceDepth = addSurfaceDepth;
            this.secondaryDepthRange = secondaryDepthRange;
            this.surfaceType = surfaceType;
        }

        @Override
        public Condition apply(Context ctx) {
            boolean ceiling = this.surfaceType == CaveSurface.CEILING;
            return new LazyYCondition(ctx) {
                @Override
                protected boolean compute() {
                    int depth = ceiling ? context.stoneDepthBelow : context.stoneDepthAbove;
                    int surfAdd = addSurfaceDepth ? context.surfaceDepth : 0;
                    int secondAdd = secondaryDepthRange == 0 ? 0 :
                            (int) NoiseUtils.map(context.getSurfaceSecondary(), -1.0, 1.0, 0, secondaryDepthRange);
                    return depth <= 1 + offset + surfAdd + secondAdd;
                }
            };
        }
    }

    private static class YConditionSource implements ConditionSource {
        private final VerticalAnchor anchor;
        private final int surfaceDepthMultiplier;
        private final boolean addStoneDepth;

        YConditionSource(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) {
            this.anchor = anchor;
            this.surfaceDepthMultiplier = surfaceDepthMultiplier;
            this.addStoneDepth = addStoneDepth;
        }

        @Override
        public Condition apply(Context ctx) {
            int anchorY = this.anchor.resolveY(ctx.minY, ctx.maxY);
            return new LazyYCondition(ctx) {
                @Override
                protected boolean compute() {
                    int y = anchorY + surfaceDepthMultiplier * context.surfaceDepth;
                    if (addStoneDepth) {
                        y += context.stoneDepthAbove;
                    }
                    return context.blockY >= y;
                }
            };
        }
    }

    private static class WaterConditionSource implements ConditionSource {
        private final int offset;
        private final int surfaceDepthMultiplier;
        private final boolean addStoneDepth;

        WaterConditionSource(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
            this.offset = offset;
            this.surfaceDepthMultiplier = surfaceDepthMultiplier;
            this.addStoneDepth = addStoneDepth;
        }

        @Override
        public Condition apply(Context ctx) {
            return new LazyYCondition(ctx) {
                @Override
                protected boolean compute() {
                    if (context.waterHeight == Integer.MIN_VALUE) return true;
                    int y = context.waterHeight + offset + surfaceDepthMultiplier * context.surfaceDepth;
                    if (addStoneDepth) y += context.stoneDepthAbove;
                    return context.blockY >= y;
                }
            };
        }
    }

    // ===== Lazy caching base classes =====

    private abstract static class LazyCondition implements Condition {
        protected final Context context;
        private long lastUpdate;
        private Boolean result;

        protected LazyCondition(Context context) {
            this.context = context;
            this.lastUpdate = getContextLastUpdate() - 1L;
        }

        @Override
        public boolean test() {
            long contextUpdate = getContextLastUpdate();
            if (contextUpdate == this.lastUpdate) {
                if (this.result == null) throw new IllegalStateException("Update triggered but result is null");
                return this.result;
            }
            this.lastUpdate = contextUpdate;
            this.result = compute();
            return this.result;
        }

        protected abstract long getContextLastUpdate();
        protected abstract boolean compute();
    }

    private abstract static class LazyYCondition extends LazyCondition {
        protected LazyYCondition(Context context) { super(context); }
        @Override
        protected long getContextLastUpdate() { return context.lastUpdateY; }
    }

    private abstract static class LazyXZCondition extends LazyCondition {
        protected LazyXZCondition(Context context) { super(context); }
        @Override
        protected long getContextLastUpdate() { return context.lastUpdateXZ; }
    }

    // ===== Context =====

    public static final class Context {
        private final SurfaceSystem system;
        private final NoiseChunk noiseChunk;
        private final SurfaceSystem.BiomeGetter biomeGetter;
        final int minY;
        final int maxY;

        // Update counters
        long lastUpdateXZ = Long.MIN_VALUE + 1;
        long lastUpdateY = Long.MIN_VALUE + 1;

        // Current position
        int blockX, blockY, blockZ;

        // Stone depth tracking
        int surfaceDepth;
        int stoneDepthAbove;
        int stoneDepthBelow;
        int waterHeight;

        // Cached conditions
        final Condition temperature;
        final Condition steep;
        final Condition hole;

        // Surface secondary cache
        private long lastSurfaceSecondaryUpdate;
        private double surfaceSecondary;

        // Preliminary surface cache
        private long lastPrelimSurfaceCellOrigin = Long.MAX_VALUE;
        private final int[] preliminarySurfaceCache = new int[4];
        private long lastMinSurfaceLevelUpdate;
        private int minSurfaceLevel;

        // Current biome
        private Biome currentBiome;

        public Context(SurfaceSystem system, NoiseChunk noiseChunk, SurfaceSystem.BiomeGetter biomeGetter,
                       int minY, int maxY) {
            this.system = system;
            this.noiseChunk = noiseChunk;
            this.biomeGetter = biomeGetter;
            this.minY = minY;
            this.maxY = maxY;
            this.lastSurfaceSecondaryUpdate = this.lastUpdateXZ - 1L;
            this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ - 1L;

            this.temperature = new LazyYCondition(this) {
                @Override
                protected boolean compute() {
                    Biome b = Context.this.getBiome();
                    if (b == null) return false;
                    return b.getTemperature(new BlockPos(Context.this.blockX, Context.this.blockY, Context.this.blockZ)) < 0.15f;
                }
            };

            this.steep = new LazyXZCondition(this) {
                @Override
                protected boolean compute() {
                    // Simplified steep check - would need heightmap in full implementation
                    return false;
                }
            };

            this.hole = new LazyXZCondition(this) {
                @Override
                protected boolean compute() {
                    return Context.this.surfaceDepth <= 0;
                }
            };
        }

        public void updateXZ(int blockX, int blockZ) {
            ++this.lastUpdateXZ;
            ++this.lastUpdateY;
            this.blockX = blockX;
            this.blockZ = blockZ;
            this.surfaceDepth = this.system.getSurfaceDepth(blockX, blockZ);
        }

        public void updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight,
                             int blockX, int blockY, int blockZ) {
            ++this.lastUpdateY;
            this.blockY = blockY;
            this.waterHeight = waterHeight;
            this.stoneDepthAbove = stoneDepthAbove;
            this.stoneDepthBelow = stoneDepthBelow;
            this.currentBiome = null; // invalidate biome cache
        }

        public Biome getBiome() {
            if (this.currentBiome == null) {
                this.currentBiome = this.biomeGetter.getBiome(this.blockX, this.blockZ);
            }
            return this.currentBiome;
        }

        public double getSurfaceSecondary() {
            if (this.lastSurfaceSecondaryUpdate != this.lastUpdateXZ) {
                this.lastSurfaceSecondaryUpdate = this.lastUpdateXZ;
                this.surfaceSecondary = this.system.getSurfaceSecondary(this.blockX, this.blockZ);
            }
            return this.surfaceSecondary;
        }

        public int getMinSurfaceLevel() {
            if (this.lastMinSurfaceLevelUpdate != this.lastUpdateXZ) {
                this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ;
                int cellX = this.blockX >> 4;
                int cellZ = this.blockZ >> 4;
                long cellOrigin = ((long) cellX << 32) | (cellZ & 0xFFFFFFFFL);
                if (this.lastPrelimSurfaceCellOrigin != cellOrigin) {
                    this.lastPrelimSurfaceCellOrigin = cellOrigin;
                    this.preliminarySurfaceCache[0] = this.noiseChunk.preliminarySurfaceLevel(cellX << 4, cellZ << 4);
                    this.preliminarySurfaceCache[1] = this.noiseChunk.preliminarySurfaceLevel((cellX + 1) << 4, cellZ << 4);
                    this.preliminarySurfaceCache[2] = this.noiseChunk.preliminarySurfaceLevel(cellX << 4, (cellZ + 1) << 4);
                    this.preliminarySurfaceCache[3] = this.noiseChunk.preliminarySurfaceLevel((cellX + 1) << 4, (cellZ + 1) << 4);
                }
                double xFrac = (double) (this.blockX & 15) / 16.0;
                double zFrac = (double) (this.blockZ & 15) / 16.0;
                double lerped = NoiseUtils.lerp(zFrac,
                        NoiseUtils.lerp(xFrac,
                                this.preliminarySurfaceCache[0], this.preliminarySurfaceCache[1]),
                        NoiseUtils.lerp(xFrac,
                                this.preliminarySurfaceCache[2], this.preliminarySurfaceCache[3]));
                this.minSurfaceLevel = (int) Math.floor(lerped) + this.surfaceDepth - 8;
            }
            return this.minSurfaceLevel;
        }
    }
}
