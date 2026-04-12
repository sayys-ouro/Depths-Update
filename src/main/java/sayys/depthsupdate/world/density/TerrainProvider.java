package sayys.depthsupdate.world.density;

import sayys.depthsupdate.world.noise.NoiseUtils;

/**
 * Builds the cubic splines that define terrain offset, factor, and jaggedness.
 * Faithful backport of modern Minecraft's TerrainProvider, specialized for our CubicSpline.
 */
public final class TerrainProvider {

    private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51f;
    private static final float OCEAN_CONTINENTALNESS = -0.4f;
    private static final float PLAINS_CONTINENTALNESS = 0.1f;
    private static final float BEACH_CONTINENTALNESS = -0.15f;

    private static final CubicSpline.ToFloatFunction<Float> NO_TRANSFORM = v -> v;
    private static final CubicSpline.ToFloatFunction<Float> AMPLIFIED_OFFSET =
            v -> v < 0.0f ? v : v * 2.0f;
    private static final CubicSpline.ToFloatFunction<Float> AMPLIFIED_FACTOR =
            v -> 1.25f - 6.25f / (v + 5.0f);
    private static final CubicSpline.ToFloatFunction<Float> AMPLIFIED_JAGGEDNESS =
            v -> v * 2.0f;

    private TerrainProvider() {}

    public static CubicSpline<DensityFunction.FunctionContext> overworldOffset(
            DensityFunction continents, DensityFunction erosion, DensityFunction ridges, boolean amplified) {
        CubicSpline.ToFloatFunction<Float> t = amplified ? AMPLIFIED_OFFSET : NO_TRANSFORM;
        CubicSpline<DensityFunction.FunctionContext> beachSpline =
                buildErosionOffsetSpline(erosion, ridges, -0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, t);
        CubicSpline<DensityFunction.FunctionContext> lowSpline =
                buildErosionOffsetSpline(erosion, ridges, -0.1f, 0.03f, 0.1f, 0.1f, 0.01f, -0.03f, false, false, t);
        CubicSpline<DensityFunction.FunctionContext> midSpline =
                buildErosionOffsetSpline(erosion, ridges, -0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true, t);
        CubicSpline<DensityFunction.FunctionContext> highSpline =
                buildErosionOffsetSpline(erosion, ridges, -0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true, t);
        return CubicSpline.<DensityFunction.FunctionContext>builder(continents, t)
                .addPoint(-1.1f, 0.044f)
                .addPoint(-1.02f, -0.2222f)
                .addPoint(-0.51f, -0.2222f)
                .addPoint(-0.44f, -0.12f)
                .addPoint(-0.18f, -0.12f)
                .addPoint(-0.16f, beachSpline)
                .addPoint(-0.15f, beachSpline)
                .addPoint(-0.1f, lowSpline)
                .addPoint(0.25f, midSpline)
                .addPoint(1.0f, highSpline)
                .build();
    }

    public static CubicSpline<DensityFunction.FunctionContext> overworldFactor(
            DensityFunction continents, DensityFunction erosion, DensityFunction weirdness,
            DensityFunction ridges, boolean amplified) {
        CubicSpline.ToFloatFunction<Float> t = amplified ? AMPLIFIED_FACTOR : NO_TRANSFORM;
        return CubicSpline.<DensityFunction.FunctionContext>builder(continents, NO_TRANSFORM)
                .addPoint(-0.19f, 3.95f)
                .addPoint(-0.15f, getErosionFactor(erosion, weirdness, ridges, 6.25f, true, NO_TRANSFORM))
                .addPoint(-0.1f, getErosionFactor(erosion, weirdness, ridges, 5.47f, true, t))
                .addPoint(0.03f, getErosionFactor(erosion, weirdness, ridges, 5.08f, true, t))
                .addPoint(0.06f, getErosionFactor(erosion, weirdness, ridges, 4.69f, false, t))
                .build();
    }

    public static CubicSpline<DensityFunction.FunctionContext> overworldJaggedness(
            DensityFunction continents, DensityFunction erosion, DensityFunction weirdness,
            DensityFunction ridges, boolean amplified) {
        CubicSpline.ToFloatFunction<Float> t = amplified ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
        return CubicSpline.<DensityFunction.FunctionContext>builder(continents, t)
                .addPoint(-0.11f, 0.0f)
                .addPoint(0.03f, buildErosionJaggednessSpline(erosion, weirdness, ridges, 1.0f, 0.5f, 0.0f, 0.0f, t))
                .addPoint(0.65f, buildErosionJaggednessSpline(erosion, weirdness, ridges, 1.0f, 1.0f, 1.0f, 0.0f, t))
                .build();
    }

    private static CubicSpline<DensityFunction.FunctionContext> buildErosionJaggednessSpline(
            DensityFunction erosion, DensityFunction weirdness, DensityFunction ridges,
            float jagFactorPeakE0, float jagFactorPeakE1,
            float jagFactorHighE0, float jagFactorHighE1,
            CubicSpline.ToFloatFunction<Float> t) {
        CubicSpline<DensityFunction.FunctionContext> ridgeJagE0 =
                buildRidgeJaggednessSpline(weirdness, ridges, jagFactorPeakE0, jagFactorHighE0, t);
        CubicSpline<DensityFunction.FunctionContext> ridgeJagE1 =
                buildRidgeJaggednessSpline(weirdness, ridges, jagFactorPeakE1, jagFactorHighE1, t);
        return CubicSpline.<DensityFunction.FunctionContext>builder(erosion, t)
                .addPoint(-1.0f, ridgeJagE0)
                .addPoint(-0.78f, ridgeJagE1)
                .addPoint(-0.5775f, ridgeJagE1)
                .addPoint(-0.375f, 0.0f)
                .build();
    }

    private static CubicSpline<DensityFunction.FunctionContext> buildRidgeJaggednessSpline(
            DensityFunction weirdness, DensityFunction ridges,
            float jagFactorPeak, float jagFactorHigh,
            CubicSpline.ToFloatFunction<Float> t) {
        float highSliceStart = NoiseRouterData.peaksAndValleys(0.4f);
        float highSliceEnd = NoiseRouterData.peaksAndValleys(0.56666666f);
        float highSliceMiddle = (highSliceStart + highSliceEnd) / 2.0f;
        CubicSpline.Builder<DensityFunction.FunctionContext> b = CubicSpline.builder(ridges, t);
        b.addPoint(highSliceStart, 0.0f);
        if (jagFactorHigh > 0.0f) {
            b.addPoint(highSliceMiddle, buildWeirdnessJaggednessSpline(weirdness, jagFactorHigh, t));
        } else {
            b.addPoint(highSliceMiddle, 0.0f);
        }
        if (jagFactorPeak > 0.0f) {
            b.addPoint(1.0f, buildWeirdnessJaggednessSpline(weirdness, jagFactorPeak, t));
        } else {
            b.addPoint(1.0f, 0.0f);
        }
        return b.build();
    }

    private static CubicSpline<DensityFunction.FunctionContext> buildWeirdnessJaggednessSpline(
            DensityFunction weirdness, float jagFactor, CubicSpline.ToFloatFunction<Float> t) {
        float maxJagNeg = 0.63f * jagFactor;
        float maxJagPos = 0.3f * jagFactor;
        return CubicSpline.<DensityFunction.FunctionContext>builder(weirdness, t)
                .addPoint(-0.01f, maxJagNeg)
                .addPoint(0.01f, maxJagPos)
                .build();
    }

    private static CubicSpline<DensityFunction.FunctionContext> getErosionFactor(
            DensityFunction erosion, DensityFunction weirdness, DensityFunction ridges,
            float baseValue, boolean shatteredTerrain, CubicSpline.ToFloatFunction<Float> t) {
        CubicSpline<DensityFunction.FunctionContext> baseSpline =
                CubicSpline.<DensityFunction.FunctionContext>builder(weirdness, t)
                        .addPoint(-0.2f, 6.3f)
                        .addPoint(0.2f, baseValue)
                        .build();

        CubicSpline.Builder<DensityFunction.FunctionContext> erosionPoints =
                CubicSpline.<DensityFunction.FunctionContext>builder(erosion, t)
                        .addPoint(-0.6f, baseSpline)
                        .addPoint(-0.5f, CubicSpline.<DensityFunction.FunctionContext>builder(weirdness, t)
                                .addPoint(-0.05f, 6.3f).addPoint(0.05f, 2.67f).build())
                        .addPoint(-0.35f, baseSpline)
                        .addPoint(-0.25f, baseSpline)
                        .addPoint(-0.1f, CubicSpline.<DensityFunction.FunctionContext>builder(weirdness, t)
                                .addPoint(-0.05f, 2.67f).addPoint(0.05f, 6.3f).build())
                        .addPoint(0.03f, baseSpline);

        if (shatteredTerrain) {
            CubicSpline<DensityFunction.FunctionContext> weirdnessShattered =
                    CubicSpline.<DensityFunction.FunctionContext>builder(weirdness, t)
                            .addPoint(0.0f, baseValue).addPoint(0.1f, 0.625f).build();
            CubicSpline<DensityFunction.FunctionContext> ridgesShattered =
                    CubicSpline.<DensityFunction.FunctionContext>builder(ridges, t)
                            .addPoint(-0.9f, baseValue).addPoint(-0.69f, weirdnessShattered).build();
            erosionPoints.addPoint(0.35f, baseValue)
                    .addPoint(0.45f, ridgesShattered)
                    .addPoint(0.55f, ridgesShattered)
                    .addPoint(0.62f, baseValue);
        } else {
            CubicSpline<DensityFunction.FunctionContext> wShattered =
                    CubicSpline.<DensityFunction.FunctionContext>builder(ridges, t)
                            .addPoint(-0.7f, baseSpline).addPoint(-0.15f, 1.37f).build();
            CubicSpline<DensityFunction.FunctionContext> rShattered =
                    CubicSpline.<DensityFunction.FunctionContext>builder(ridges, t)
                            .addPoint(0.45f, baseSpline).addPoint(0.7f, 1.56f).build();
            erosionPoints.addPoint(0.05f, rShattered)
                    .addPoint(0.4f, rShattered)
                    .addPoint(0.45f, wShattered)
                    .addPoint(0.55f, wShattered)
                    .addPoint(0.58f, baseValue);
        }

        return erosionPoints.build();
    }

    private static float calculateSlope(float y1, float y2, float x1, float x2) {
        return (y2 - y1) / (x2 - x1);
    }

    public static CubicSpline<DensityFunction.FunctionContext> buildErosionOffsetSpline(
            DensityFunction erosion, DensityFunction ridges,
            float lowValley, float hill, float tallHill, float mountainFactor,
            float plain, float swamp, boolean includeExtremeHills, boolean saddle,
            CubicSpline.ToFloatFunction<Float> t) {
        CubicSpline<DensityFunction.FunctionContext> veryLowErosionMountains =
                buildMountainRidgeSplineWithPoints(ridges,
                        (float) NoiseUtils.lerp(mountainFactor, 0.6f, 1.5f), saddle, t);
        CubicSpline<DensityFunction.FunctionContext> lowErosionMountains =
                buildMountainRidgeSplineWithPoints(ridges,
                        (float) NoiseUtils.lerp(mountainFactor, 0.6f, 1.0f), saddle, t);
        CubicSpline<DensityFunction.FunctionContext> mountains =
                buildMountainRidgeSplineWithPoints(ridges, mountainFactor, saddle, t);
        CubicSpline<DensityFunction.FunctionContext> widePlateau =
                ridgeSpline(ridges, lowValley - 0.15f, 0.5f * mountainFactor,
                        (float) NoiseUtils.lerp(0.5, 0.5f, 0.5f) * mountainFactor,
                        0.5f * mountainFactor, 0.6f * mountainFactor, 0.5f, t);
        CubicSpline<DensityFunction.FunctionContext> narrowPlateau =
                ridgeSpline(ridges, lowValley, plain * mountainFactor, hill * mountainFactor,
                        0.5f * mountainFactor, 0.6f * mountainFactor, 0.5f, t);
        CubicSpline<DensityFunction.FunctionContext> plains =
                ridgeSpline(ridges, lowValley, plain, plain, hill, tallHill, 0.5f, t);
        CubicSpline<DensityFunction.FunctionContext> plainsFarInland =
                ridgeSpline(ridges, lowValley, plain, plain, hill, tallHill, 0.5f, t);
        CubicSpline<DensityFunction.FunctionContext> extremeHills =
                CubicSpline.<DensityFunction.FunctionContext>builder(ridges, t)
                        .addPoint(-1.0f, lowValley)
                        .addPoint(-0.4f, plains)
                        .addPoint(0.0f, tallHill + 0.07f)
                        .build();
        CubicSpline<DensityFunction.FunctionContext> swamps =
                ridgeSpline(ridges, -0.02f, swamp, swamp, hill, tallHill, 0.0f, t);

        CubicSpline.Builder<DensityFunction.FunctionContext> builder =
                CubicSpline.<DensityFunction.FunctionContext>builder(erosion, t)
                        .addPoint(-0.85f, veryLowErosionMountains)
                        .addPoint(-0.7f, lowErosionMountains)
                        .addPoint(-0.4f, mountains)
                        .addPoint(-0.35f, widePlateau)
                        .addPoint(-0.1f, narrowPlateau)
                        .addPoint(0.2f, plains);
        if (includeExtremeHills) {
            builder.addPoint(0.4f, plainsFarInland)
                    .addPoint(0.45f, extremeHills)
                    .addPoint(0.55f, extremeHills)
                    .addPoint(0.58f, plainsFarInland);
        }
        builder.addPoint(0.7f, swamps);
        return builder.build();
    }

    private static CubicSpline<DensityFunction.FunctionContext> buildMountainRidgeSplineWithPoints(
            DensityFunction ridges, float modulation, boolean saddle,
            CubicSpline.ToFloatFunction<Float> t) {
        CubicSpline.Builder<DensityFunction.FunctionContext> b = CubicSpline.builder(ridges, t);
        float allowRiversBelow = -0.7f;
        float minPointContinentalness = mountainContinentalness(-1.0f, modulation, allowRiversBelow);
        float maxPointContinentalness = mountainContinentalness(1.0f, modulation, allowRiversBelow);
        float ridgeZeroPoint = calculateMountainRidgeZeroContinentalnessPoint(modulation);

        if (-0.65f < ridgeZeroPoint && ridgeZeroPoint < 1.0f) {
            float afterRiverContinentalness = mountainContinentalness(-0.65f, modulation, allowRiversBelow);
            float beforeRiverContinentalness = mountainContinentalness(-0.75f, modulation, allowRiversBelow);
            float minPointDerivative = calculateSlope(minPointContinentalness, beforeRiverContinentalness, -1.0f, -0.75f);
            b.addPoint(-1.0f, minPointContinentalness, minPointDerivative);
            b.addPoint(-0.75f, beforeRiverContinentalness);
            b.addPoint(-0.65f, afterRiverContinentalness);
            float ridgeZeroContinentalness = mountainContinentalness(ridgeZeroPoint, modulation, allowRiversBelow);
            float maxPointDerivative = calculateSlope(ridgeZeroContinentalness, maxPointContinentalness, ridgeZeroPoint, 1.0f);
            b.addPoint(ridgeZeroPoint - 0.01f, ridgeZeroContinentalness);
            b.addPoint(ridgeZeroPoint, ridgeZeroContinentalness, maxPointDerivative);
            b.addPoint(1.0f, maxPointContinentalness, maxPointDerivative);
        } else {
            float simpleDerivative = calculateSlope(minPointContinentalness, maxPointContinentalness, -1.0f, 1.0f);
            if (saddle) {
                b.addPoint(-1.0f, Math.max(0.2f, minPointContinentalness));
                b.addPoint(0.0f, (float) NoiseUtils.lerp(0.5, minPointContinentalness, maxPointContinentalness), simpleDerivative);
            } else {
                b.addPoint(-1.0f, minPointContinentalness, simpleDerivative);
            }
            b.addPoint(1.0f, maxPointContinentalness, simpleDerivative);
        }

        return b.build();
    }

    private static float mountainContinentalness(float ridge, float modulation, float allowRiversBelow) {
        float ridgeOffset = 1.17f;
        float ridgeAmplitude = 0.46082947f;
        float ridgeSlope = 1.0f - (1.0f - modulation) * 0.5f;
        float ridgeIntersect = 0.5f * (1.0f - modulation);
        float adjustedRidgeHeight = (ridge + ridgeOffset) * ridgeAmplitude;
        float continentalness = adjustedRidgeHeight * ridgeSlope - ridgeIntersect;
        return ridge < allowRiversBelow ? Math.max(continentalness, -0.2222f) : Math.max(continentalness, 0.0f);
    }

    private static float calculateMountainRidgeZeroContinentalnessPoint(float modulation) {
        float ridgeAmplitude = 0.46082947f;
        float ridgeSlope = 1.0f - (1.0f - modulation) * 0.5f;
        float ridgeIntersect = 0.5f * (1.0f - modulation);
        return ridgeIntersect / (ridgeAmplitude * ridgeSlope) - 1.17f;
    }

    private static CubicSpline<DensityFunction.FunctionContext> ridgeSpline(
            DensityFunction ridges, float valley, float low, float mid, float high, float peaks,
            float minValleySteepness, CubicSpline.ToFloatFunction<Float> t) {
        float d1 = Math.max(0.5f * (low - valley), minValleySteepness);
        float d2 = 5.0f * (mid - low);
        return CubicSpline.<DensityFunction.FunctionContext>builder(ridges, t)
                .addPoint(-1.0f, valley, d1)
                .addPoint(-0.4f, low, Math.min(d1, d2))
                .addPoint(0.0f, mid, d2)
                .addPoint(0.4f, high, 2.0f * (high - mid))
                .addPoint(1.0f, peaks, 0.7f * (peaks - high))
                .build();
    }
}
