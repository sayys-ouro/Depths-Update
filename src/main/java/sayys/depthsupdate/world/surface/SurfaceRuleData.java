package sayys.depthsupdate.world.surface;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;

import sayys.depthsupdate.registry.DeepslateRegistry;
import sayys.depthsupdate.world.noise.NormalNoise;
import sayys.depthsupdate.world.noise.PositionalRandomFactory;

/**
 * Defines the overworld surface rule tree for 1.12.2.
 * Backport of vanilla SurfaceRuleData, adapted for available biomes and blocks.
 */
public class SurfaceRuleData {

    // Block state constants
    private static final SurfaceRules.RuleSource AIR = SurfaceRules.state(Blocks.AIR.getDefaultState());
    private static final SurfaceRules.RuleSource BEDROCK = SurfaceRules.state(Blocks.BEDROCK.getDefaultState());
    private static final SurfaceRules.RuleSource STONE = SurfaceRules.state(Blocks.STONE.getDefaultState());
    private static final SurfaceRules.RuleSource DIRT = SurfaceRules.state(Blocks.DIRT.getDefaultState());
    private static final SurfaceRules.RuleSource GRASS_BLOCK = SurfaceRules.state(Blocks.GRASS.getDefaultState());
    private static final SurfaceRules.RuleSource COARSE_DIRT = SurfaceRules.state(
            Blocks.DIRT.getStateFromMeta(1)); // coarse_dirt
    private static final SurfaceRules.RuleSource PODZOL = SurfaceRules.state(
            Blocks.DIRT.getStateFromMeta(2)); // podzol
    private static final SurfaceRules.RuleSource MYCELIUM = SurfaceRules.state(Blocks.MYCELIUM.getDefaultState());
    private static final SurfaceRules.RuleSource SAND = SurfaceRules.state(Blocks.SAND.getDefaultState());
    private static final SurfaceRules.RuleSource SANDSTONE = SurfaceRules.state(Blocks.SANDSTONE.getDefaultState());
    private static final SurfaceRules.RuleSource RED_SAND = SurfaceRules.state(
            Blocks.SAND.getStateFromMeta(1)); // red_sand
    private static final SurfaceRules.RuleSource RED_SANDSTONE = SurfaceRules.state(
            Blocks.RED_SANDSTONE.getDefaultState());
    private static final SurfaceRules.RuleSource GRAVEL = SurfaceRules.state(Blocks.GRAVEL.getDefaultState());
    private static final SurfaceRules.RuleSource PACKED_ICE = SurfaceRules.state(Blocks.PACKED_ICE.getDefaultState());
    private static final SurfaceRules.RuleSource SNOW_BLOCK = SurfaceRules.state(Blocks.SNOW.getDefaultState());
    private static final SurfaceRules.RuleSource ICE = SurfaceRules.state(Blocks.ICE.getDefaultState());
    private static final SurfaceRules.RuleSource WATER = SurfaceRules.state(Blocks.WATER.getDefaultState());
    private static final SurfaceRules.RuleSource WHITE_TERRACOTTA = SurfaceRules.state(
            Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(0));
    private static final SurfaceRules.RuleSource ORANGE_TERRACOTTA = SurfaceRules.state(
            Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(1));
    private static final SurfaceRules.RuleSource TERRACOTTA = SurfaceRules.state(
            Blocks.HARDENED_CLAY.getDefaultState());

    /**
     * Builds the overworld surface rule tree.
     *
     * @param surfaceNoise the surface noise for noise conditions
     * @param gravelNoise noise for gravel patches
     * @param randomFactory positional random factory for gradients
     * @param surfaceSystem the surface system (for bandlands)
     */
    public static SurfaceRules.RuleSource overworld(NormalNoise surfaceNoise, NormalNoise gravelNoise,
                                                     PositionalRandomFactory randomFactory,
                                                     SurfaceSystem surfaceSystem) {
        return overworldLike(true, true, surfaceNoise, gravelNoise, randomFactory, surfaceSystem);
    }

    public static SurfaceRules.RuleSource overworldLike(boolean doPreliminarySurfaceCheck,
                                                         boolean bedrockFloor,
                                                         NormalNoise surfaceNoise,
                                                         NormalNoise gravelNoise,
                                                         PositionalRandomFactory randomFactory,
                                                         SurfaceSystem surfaceSystem) {
        // Y-level conditions
        SurfaceRules.ConditionSource aboveOverworldSeaLevel = SurfaceRules.yBlockCheck(
                VerticalAnchor.absolute(63), 0);
        SurfaceRules.ConditionSource notUnderwater = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource aboveWater = SurfaceRules.waterBlockCheck(0, 0);
        SurfaceRules.ConditionSource notUnderDeepWater = SurfaceRules.waterStartCheck(-6, -1);

        SurfaceRules.ConditionSource hole = SurfaceRules.hole();

        // Biome conditions (using 1.12.2 biome instances)
        SurfaceRules.ConditionSource frozenOcean = SurfaceRules.isBiome(
                Biomes.FROZEN_OCEAN, Biomes.DEEP_OCEAN); // 1.12.2 has no deep frozen ocean
        SurfaceRules.ConditionSource steep = SurfaceRules.steep();

        // Common block rules
        SurfaceRules.RuleSource grassOrDirtIfUnderwater = SurfaceRules.sequence(
                SurfaceRules.ifTrue(aboveWater, GRASS_BLOCK), DIRT);
        SurfaceRules.RuleSource sandOrSandstoneIfCeiling = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SANDSTONE), SAND);
        SurfaceRules.RuleSource gravelOrStoneIfCeiling = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, STONE), GRAVEL);

        // Biome groups
        SurfaceRules.ConditionSource sandBiomes = SurfaceRules.isBiome(
                Biomes.BEACH, Biomes.COLD_BEACH);
        SurfaceRules.ConditionSource desertBiomes = SurfaceRules.isBiome(Biomes.DESERT, Biomes.DESERT_HILLS);

        // Noise-based surface conditions
        SurfaceRules.ConditionSource surfaceNoiseHigh = SurfaceRules.noiseCondition(
                surfaceNoise, 1.0 / 8.25, Double.MAX_VALUE);
        SurfaceRules.ConditionSource surfaceNoiseVeryHigh = SurfaceRules.noiseCondition(
                surfaceNoise, 1.75 / 8.25, Double.MAX_VALUE);

        // Common surface and under rules (shared between surface and under-surface)
        SurfaceRules.RuleSource commonSurfaceAndUnderRules = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.STONE_BEACH), STONE),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE),
                        SurfaceRules.ifTrue(surfaceNoiseHigh, STONE)),
                SurfaceRules.ifTrue(sandBiomes, sandOrSandstoneIfCeiling),
                SurfaceRules.ifTrue(desertBiomes, sandOrSandstoneIfCeiling)
        );

        // Under-surface biome rules
        SurfaceRules.RuleSource biomeUnderSurfaceRule = SurfaceRules.sequence(
                commonSurfaceAndUnderRules,
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.EXTREME_HILLS_WITH_TREES),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(surfaceNoiseVeryHigh, gravelOrStoneIfCeiling),
                                SurfaceRules.ifTrue(surfaceNoiseHigh, STONE),
                                DIRT)),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MUTATED_SAVANNA),
                        SurfaceRules.ifTrue(surfaceNoiseVeryHigh, STONE)),
                DIRT
        );

        // Surface biome rules
        SurfaceRules.RuleSource biomeSurfaceRule = SurfaceRules.sequence(
                commonSurfaceAndUnderRules,
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.EXTREME_HILLS_WITH_TREES),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(surfaceNoiseVeryHigh, gravelOrStoneIfCeiling),
                                SurfaceRules.ifTrue(surfaceNoiseHigh, STONE),
                                grassOrDirtIfUnderwater)),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MUTATED_SAVANNA),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(surfaceNoiseVeryHigh, STONE),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(surfaceNoise, -0.5 / 8.25, Double.MAX_VALUE),
                                        COARSE_DIRT))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.REDWOOD_TAIGA, Biomes.REDWOOD_TAIGA_HILLS,
                                Biomes.MUTATED_REDWOOD_TAIGA, Biomes.MUTATED_REDWOOD_TAIGA_HILLS),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(surfaceNoiseVeryHigh, COARSE_DIRT),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(surfaceNoise, -0.95 / 8.25, Double.MAX_VALUE),
                                        PODZOL))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.ICE_MOUNTAINS),
                        SurfaceRules.ifTrue(aboveWater, SNOW_BLOCK)),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MUSHROOM_ISLAND, Biomes.MUSHROOM_ISLAND_SHORE),
                        MYCELIUM),
                grassOrDirtIfUnderwater
        );

        // Badlands noise conditions for clay band placement
        SurfaceRules.ConditionSource clayBand1 = SurfaceRules.noiseCondition(surfaceNoise, -0.909, -0.5454);
        SurfaceRules.ConditionSource clayBand2 = SurfaceRules.noiseCondition(surfaceNoise, -0.1818, 0.1818);
        SurfaceRules.ConditionSource clayBand3 = SurfaceRules.noiseCondition(surfaceNoise, 0.5454, 0.909);

        SurfaceRules.ConditionSource badlandsTop = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 0);
        SurfaceRules.ConditionSource badlandsHeightCondition = SurfaceRules.yStartCheck(
                VerticalAnchor.absolute(63), -1);
        SurfaceRules.ConditionSource badlandsMid = SurfaceRules.yStartCheck(VerticalAnchor.absolute(74), 1);
        SurfaceRules.ConditionSource woodedBadlandsTop = SurfaceRules.yBlockCheck(
                VerticalAnchor.absolute(97), 2);

        // Main rule: close to surface
        SurfaceRules.RuleSource mainRuleCloseToSurface = SurfaceRules.sequence(
                // Wooded badlands top layer
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MUTATED_MESA_ROCK),
                                SurfaceRules.ifTrue(woodedBadlandsTop,
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(clayBand1, COARSE_DIRT),
                                                SurfaceRules.ifTrue(clayBand2, COARSE_DIRT),
                                                SurfaceRules.ifTrue(clayBand3, COARSE_DIRT),
                                                grassOrDirtIfUnderwater)))),
                // Badlands
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MESA, Biomes.MESA_ROCK,
                                Biomes.MESA_CLEAR_ROCK, Biomes.MUTATED_MESA, Biomes.MUTATED_MESA_ROCK,
                                Biomes.MUTATED_MESA_CLEAR_ROCK),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(badlandsTop, ORANGE_TERRACOTTA),
                                                SurfaceRules.ifTrue(badlandsMid,
                                                        SurfaceRules.sequence(
                                                                SurfaceRules.ifTrue(clayBand1, TERRACOTTA),
                                                                SurfaceRules.ifTrue(clayBand2, TERRACOTTA),
                                                                SurfaceRules.ifTrue(clayBand3, TERRACOTTA),
                                                                SurfaceRules.bandlands(surfaceSystem))),
                                                SurfaceRules.ifTrue(notUnderwater,
                                                        SurfaceRules.sequence(
                                                                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING,
                                                                        RED_SANDSTONE),
                                                                RED_SAND)),
                                                SurfaceRules.ifTrue(SurfaceRules.not(hole), ORANGE_TERRACOTTA),
                                                SurfaceRules.ifTrue(notUnderDeepWater, WHITE_TERRACOTTA),
                                                gravelOrStoneIfCeiling)),
                                SurfaceRules.ifTrue(badlandsHeightCondition,
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(aboveOverworldSeaLevel,
                                                        SurfaceRules.ifTrue(SurfaceRules.not(badlandsMid),
                                                                ORANGE_TERRACOTTA)),
                                                SurfaceRules.bandlands(surfaceSystem))),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR,
                                        SurfaceRules.ifTrue(notUnderDeepWater, WHITE_TERRACOTTA)))),
                // Standard biome surfaces
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(notUnderwater,
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(frozenOcean,
                                                SurfaceRules.ifTrue(hole,
                                                        SurfaceRules.sequence(
                                                                SurfaceRules.ifTrue(aboveWater, AIR),
                                                                SurfaceRules.ifTrue(SurfaceRules.temperature(), ICE),
                                                                WATER))),
                                        biomeSurfaceRule))),
                // Under-surface rules
                SurfaceRules.ifTrue(notUnderDeepWater,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.ifTrue(frozenOcean,
                                                SurfaceRules.ifTrue(hole, WATER))),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, biomeUnderSurfaceRule),
                                SurfaceRules.ifTrue(sandBiomes,
                                        SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, SANDSTONE)),
                                SurfaceRules.ifTrue(desertBiomes,
                                        SurfaceRules.ifTrue(SurfaceRules.VERY_DEEP_UNDER_FLOOR, SANDSTONE)))),
                // Fallback floor rules
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, gravelOrStoneIfCeiling)
        );

        // Build final sequence
        SurfaceRules.RuleSource ruleAbovePreliminarySurface = doPreliminarySurfaceCheck
                ? SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), mainRuleCloseToSurface)
                : mainRuleCloseToSurface;

        // Deepslate gradient
        IBlockState deepslate = getDeepslateState();
        SurfaceRules.RuleSource deepslateRule = SurfaceRules.ifTrue(
                SurfaceRules.verticalGradient("deepslate",
                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(8), randomFactory),
                SurfaceRules.state(deepslate));

        if (bedrockFloor) {
            SurfaceRules.RuleSource bedrockRule = SurfaceRules.ifTrue(
                    SurfaceRules.verticalGradient("bedrock_floor",
                            VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5), randomFactory),
                    BEDROCK);
            return SurfaceRules.sequence(bedrockRule, ruleAbovePreliminarySurface, deepslateRule);
        }

        return SurfaceRules.sequence(ruleAbovePreliminarySurface, deepslateRule);
    }

    /**
     * Gets the deepslate block state. Uses the mod's registered deepslate if available,
     * otherwise falls back to stone.
     */
    private static IBlockState getDeepslateState() {
        try {
            return DeepslateRegistry.deepslate.getDefaultState();
        } catch (Exception e) {
            // Fallback if mod blocks haven't been registered yet
            return Blocks.STONE.getDefaultState();
        }
    }
}
