package sayys.depthsupdate.world.density;

import sayys.depthsupdate.world.noise.BlendedNoise;
import sayys.depthsupdate.world.noise.NormalNoise;
import sayys.depthsupdate.world.noise.PositionalRandomFactory;

/**
 * Assembles the complete density function tree for terrain generation.
 * Backport of modern Minecraft's NoiseRouterData.
 *
 * In vanilla, noise parameters are registry entries loaded from data packs.
 * Here we hardcode them directly since 1.12.2 has no datapack/registry system for these.
 */
public final class NoiseRouterData {
    public static final float GLOBAL_OFFSET = -0.50375f;
    private static final double CHEESE_NOISE_TARGET = -0.703125;
    private static final double SURFACE_DENSITY_THRESHOLD = 1.5625;

    private NoiseRouterData() {}

    private static NormalNoise.NoiseParameters shift() {
        return new NormalNoise.NoiseParameters(-3, new double[]{1.0, 1.0, 0.0});
    }

    private static NormalNoise.NoiseParameters temperature() {
        return new NormalNoise.NoiseParameters(-10, new double[]{1.5, 0.0, 1.0, 0.0, 0.0, 0.0});
    }

    private static NormalNoise.NoiseParameters vegetation() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0, 1.0, 0.0, 0.0, 0.0, 0.0});
    }

    private static NormalNoise.NoiseParameters continentalness() {
        return new NormalNoise.NoiseParameters(-9, new double[]{1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0});
    }

    private static NormalNoise.NoiseParameters erosion() {
        return new NormalNoise.NoiseParameters(-9, new double[]{1.0, 1.0, 0.0, 1.0, 1.0});
    }

    private static NormalNoise.NoiseParameters ridge() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0, 2.0, 1.0, 0.0, 0.0, 0.0});
    }

    private static NormalNoise.NoiseParameters jagged() {
        return new NormalNoise.NoiseParameters(-16, new double[]{
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0});
    }

    private static NormalNoise.NoiseParameters aquiferBarrier() {
        return new NormalNoise.NoiseParameters(-3, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters aquiferFloodedness() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters aquiferSpread() {
        return new NormalNoise.NoiseParameters(-5, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters aquiferLava() {
        return new NormalNoise.NoiseParameters(-1, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters pillar() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0, 1.0});
    }

    private static NormalNoise.NoiseParameters pillarRareness() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters pillarThickness() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti2d() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti2dElevation() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti2dModulator() {
        return new NormalNoise.NoiseParameters(-11, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti2dThickness() {
        return new NormalNoise.NoiseParameters(-11, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti3d1() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti3d2() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti3dRarity() {
        return new NormalNoise.NoiseParameters(-11, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghetti3dThickness() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghettiRoughness() {
        return new NormalNoise.NoiseParameters(-5, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters spaghettiRoughnessModulator() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters caveEntrance() {
        return new NormalNoise.NoiseParameters(-7, new double[]{0.4, 0.5, 1.0});
    }

    private static NormalNoise.NoiseParameters caveLayer() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters caveCheese() {
        return new NormalNoise.NoiseParameters(-8, new double[]{0.5, 1.0, 2.0, 1.0, 2.0, 1.0, 0.0, 2.0, 0.0});
    }

    private static NormalNoise.NoiseParameters oreVeininess() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters oreVeinA() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters oreVeinB() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters oreGap() {
        return new NormalNoise.NoiseParameters(-5, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters noodle() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters noodleThickness() {
        return new NormalNoise.NoiseParameters(-8, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters noodleRidgeA() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise.NoiseParameters noodleRidgeB() {
        return new NormalNoise.NoiseParameters(-7, new double[]{1.0});
    }

    private static NormalNoise createNoise(PositionalRandomFactory randomFactory, NormalNoise.NoiseParameters params, String id) {
        return NormalNoise.create(randomFactory.fromHashOf(id), params);
    }

    private static DensityFunction.NoiseHolder noiseHolder(PositionalRandomFactory randomFactory, NormalNoise.NoiseParameters params, String id) {
        return new DensityFunction.NoiseHolder(createNoise(randomFactory, params, id));
    }

    public static float peaksAndValleys(float weirdness) {
        return -(Math.abs(Math.abs(weirdness) - 0.6666667f) - 0.33333334f) * 3.0f;
    }

    private static DensityFunction peaksAndValleys(DensityFunction weirdness) {
        return DensityFunctions.mul(
                DensityFunctions.add(
                        DensityFunctions.add(weirdness.abs(), DensityFunctions.constant(-0.6666666666666666)).abs(),
                        DensityFunctions.constant(-0.3333333333333333)),
                DensityFunctions.constant(-3.0));
    }

    private static DensityFunction slide(DensityFunction caves, int minY, int height,
                                          int topStartY, int topEndY, double topTarget,
                                          int bottomStartY, int bottomEndY, double bottomTarget) {
        DensityFunction topFactor = DensityFunctions.yClampedGradient(
                minY + height - topStartY, minY + height - topEndY, 1.0, 0.0);
        DensityFunction noiseValue = DensityFunctions.lerp(topFactor, topTarget, caves);
        DensityFunction bottomFactor = DensityFunctions.yClampedGradient(
                minY + bottomStartY, minY + bottomEndY, 0.0, 1.0);
        return DensityFunctions.lerp(bottomFactor, bottomTarget, noiseValue);
    }

    private static DensityFunction slideOverworld(boolean amplified, DensityFunction caves, NoiseSettings settings) {
        return slide(caves, settings.minY(), settings.height(),
                amplified ? 16 : 80, amplified ? 0 : 64, -0.078125,
                0, 24, amplified ? 0.4 : 0.1171875);
    }

    private static DensityFunction slideNetherLike(DensityFunction base3dNoise, int minY, int height) {
        return slide(base3dNoise, minY, height, 24, 0, 0.9375, -8, 24, 2.5);
    }

    private static DensityFunction slideEndLike(DensityFunction caves, int minY, int height) {
        return slide(caves, minY, height, 72, -184, -23.4375, 4, 32, -0.234375);
    }

    // Noise Gradient Density

    private static DensityFunction noiseGradientDensity(DensityFunction factor, DensityFunction depthWithJaggedness) {
        DensityFunction gradientUnscaled = DensityFunctions.mul(depthWithJaggedness, factor);
        return DensityFunctions.mul(DensityFunctions.constant(4.0), gradientUnscaled.quarterNegative());
    }

    private static DensityFunction offsetToDepth(DensityFunction offset, NoiseSettings settings) {
        return DensityFunctions.add(DensityFunctions.yClampedGradient(
                settings.minY(), settings.minY() + settings.height(), 1.5, -1.5), offset);
    }

    // Spline With Blending

    private static DensityFunction splineWithBlending(DensityFunction spline, DensityFunction blendingTarget) {
        DensityFunction blended = DensityFunctions.lerp(DensityFunctions.blendAlpha(), blendingTarget, spline);
        return DensityFunctions.flatCache(DensityFunctions.cache2d(blended));
    }

    // Post-processing

    private static DensityFunction postProcess(DensityFunction slide) {
        DensityFunction blended = DensityFunctions.blendDensity(slide);
        return DensityFunctions.mul(DensityFunctions.interpolated(blended), DensityFunctions.constant(0.64)).squeeze();
    }

    private static DensityFunction remap(DensityFunction input, double fromMin, double fromMax,
                                          double toMin, double toMax) {
        double factor = (toMax - toMin) / (fromMax - fromMin);
        double offset = toMin - fromMin * factor;
        return DensityFunctions.add(DensityFunctions.mul(input, DensityFunctions.constant(factor)),
                DensityFunctions.constant(offset));
    }

    // Y-limited interpolatable

    private static DensityFunction yLimitedInterpolatable(DensityFunction y, DensityFunction whenInRange,
                                                           int minYInclusive, int maxYInclusive, int whenOutOfRange) {
        return DensityFunctions.interpolated(DensityFunctions.rangeChoice(
                y, (double) minYInclusive, (double) (maxYInclusive + 1),
                whenInRange, DensityFunctions.constant((double) whenOutOfRange)));
    }

    // Cave Functions

    private static DensityFunction spaghettiRoughnessFunction(PositionalRandomFactory randomFactory) {
        DensityFunction.NoiseHolder roughnessNoise = noiseHolder(randomFactory, spaghettiRoughness(), "spaghetti_roughness");
        DensityFunction.NoiseHolder roughnessModulatorNoise = noiseHolder(randomFactory, spaghettiRoughnessModulator(), "spaghetti_roughness_modulator");
        DensityFunction modulator = DensityFunctions.mappedNoise(roughnessModulatorNoise, 0.0, -0.1);
        return DensityFunctions.cacheOnce(
                DensityFunctions.mul(modulator,
                        DensityFunctions.add(DensityFunctions.noise(roughnessNoise).abs(),
                                DensityFunctions.constant(-0.4))));
    }

    private static DensityFunction entrances(PositionalRandomFactory randomFactory,
                                              DensityFunction spaghettiRoughnessFunc) {
        DensityFunction.NoiseHolder rarity3dNoise = noiseHolder(randomFactory, spaghetti3dRarity(), "spaghetti_3d_rarity");
        DensityFunction.NoiseHolder thickness3dNoise = noiseHolder(randomFactory, spaghetti3dThickness(), "spaghetti_3d_thickness");
        DensityFunction.NoiseHolder noise3d1 = noiseHolder(randomFactory, spaghetti3d1(), "spaghetti_3d_1");
        DensityFunction.NoiseHolder noise3d2 = noiseHolder(randomFactory, spaghetti3d2(), "spaghetti_3d_2");
        DensityFunction.NoiseHolder entranceNoise = noiseHolder(randomFactory, caveEntrance(), "cave_entrance");

        DensityFunction rarityModulator = DensityFunctions.cacheOnce(DensityFunctions.noise(rarity3dNoise, 2.0, 1.0));
        DensityFunction thicknessModulator = DensityFunctions.mappedNoise(thickness3dNoise, -0.065, -0.088);
        DensityFunction cave1 = DensityFunctions.weirdScaledSampler(rarityModulator, noise3d1,
                DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1);
        DensityFunction cave2 = DensityFunctions.weirdScaledSampler(rarityModulator, noise3d2,
                DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1);
        DensityFunction spaghetti3dFunction = DensityFunctions.add(
                DensityFunctions.max(cave1, cave2), thicknessModulator).clamp(-1.0, 1.0);

        DensityFunction bigEntranceNoiseSource = DensityFunctions.noise(entranceNoise, 0.75, 0.5);
        DensityFunction bigEntrancesFunction = DensityFunctions.add(
                DensityFunctions.add(bigEntranceNoiseSource, DensityFunctions.constant(0.37)),
                DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0));

        return DensityFunctions.cacheOnce(DensityFunctions.min(
                bigEntrancesFunction,
                DensityFunctions.add(spaghettiRoughnessFunc, spaghetti3dFunction)));
    }

    private static DensityFunction noodleCaves(PositionalRandomFactory randomFactory, DensityFunction y,
                                                NoiseSettings settings) {
        int minY = settings.minY();
        int maxY = minY + settings.height();
        // Noodle caves start slightly above world bottom (vanilla: -60 with minY=-64, so +4)
        int noodleMinY = minY + 4;

        DensityFunction.NoiseHolder noodleNoise = noiseHolder(randomFactory, noodle(), "noodle");
        DensityFunction.NoiseHolder noodleThicknessNoise = noiseHolder(randomFactory, noodleThickness(), "noodle_thickness");
        DensityFunction.NoiseHolder noodleRidgeANoise = noiseHolder(randomFactory, noodleRidgeA(), "noodle_ridge_a");
        DensityFunction.NoiseHolder noodleRidgeBNoise = noiseHolder(randomFactory, noodleRidgeB(), "noodle_ridge_b");

        DensityFunction noodleToggle = yLimitedInterpolatable(y,
                DensityFunctions.noise(noodleNoise, 1.0, 1.0), noodleMinY, maxY, -1);
        DensityFunction noodleThick = yLimitedInterpolatable(y,
                DensityFunctions.mappedNoise(noodleThicknessNoise, 1.0, 1.0, -0.05, -0.1), noodleMinY, maxY, 0);
        DensityFunction noodleRidgeA = yLimitedInterpolatable(y,
                DensityFunctions.noise(noodleRidgeANoise, 2.6666666666666665, 2.6666666666666665), noodleMinY, maxY, 0);
        DensityFunction noodleRidgeB = yLimitedInterpolatable(y,
                DensityFunctions.noise(noodleRidgeBNoise, 2.6666666666666665, 2.6666666666666665), noodleMinY, maxY, 0);
        DensityFunction noodleRidged = DensityFunctions.mul(DensityFunctions.constant(1.5),
                DensityFunctions.max(noodleRidgeA.abs(), noodleRidgeB.abs()));
        return DensityFunctions.rangeChoice(noodleToggle, -1000000.0, 0.0,
                DensityFunctions.constant(64.0), DensityFunctions.add(noodleThick, noodleRidged));
    }

    private static DensityFunction pillars(PositionalRandomFactory randomFactory) {
        DensityFunction.NoiseHolder pillarNoise = noiseHolder(randomFactory, pillar(), "pillar");
        DensityFunction.NoiseHolder pillarRarenessNoise = noiseHolder(randomFactory, pillarRareness(), "pillar_rareness");
        DensityFunction.NoiseHolder pillarThicknessNoise = noiseHolder(randomFactory, pillarThickness(), "pillar_thickness");

        DensityFunction pillarNoiseSource = DensityFunctions.noise(pillarNoise, 25.0, 0.3);
        DensityFunction pillarRarenessModulator = DensityFunctions.mappedNoise(pillarRarenessNoise, 0.0, -2.0);
        DensityFunction pillarThicknessModulator = DensityFunctions.mappedNoise(pillarThicknessNoise, 0.0, 1.1);
        DensityFunction pillarsWithRareness = DensityFunctions.add(
                DensityFunctions.mul(pillarNoiseSource, DensityFunctions.constant(2.0)), pillarRarenessModulator);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(pillarsWithRareness, pillarThicknessModulator.cube()));
    }

    private static DensityFunction spaghetti2D(PositionalRandomFactory randomFactory,
                                                DensityFunction spaghetti2dThicknessModulator,
                                                NoiseSettings settings) {
        int minY = settings.minY();
        int maxY = minY + settings.height();

        DensityFunction.NoiseHolder s2dModulatorNoise = noiseHolder(randomFactory, spaghetti2dModulator(), "spaghetti_2d_modulator");
        DensityFunction.NoiseHolder s2dNoise = noiseHolder(randomFactory, spaghetti2d(), "spaghetti_2d");
        DensityFunction.NoiseHolder s2dElevationNoise = noiseHolder(randomFactory, spaghetti2dElevation(), "spaghetti_2d_elevation");

        DensityFunction modulatorSource = DensityFunctions.noise(s2dModulatorNoise, 2.0, 1.0);
        DensityFunction cave = DensityFunctions.weirdScaledSampler(modulatorSource, s2dNoise,
                DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE2);
        DensityFunction elevationModulator = DensityFunctions.mappedNoise(s2dElevationNoise, 0.0, (double) Math.floorDiv(minY, 8), 8.0);
        DensityFunction slopedSpaghetti = DensityFunctions.add(elevationModulator,
                DensityFunctions.yClampedGradient(minY, maxY, 8.0, -40.0)).abs();
        DensityFunction layerRidged = DensityFunctions.add(slopedSpaghetti, spaghetti2dThicknessModulator).cube();
        DensityFunction caveNoise = DensityFunctions.add(cave,
                DensityFunctions.mul(DensityFunctions.constant(0.083), spaghetti2dThicknessModulator));
        return DensityFunctions.max(caveNoise, layerRidged).clamp(-1.0, 1.0);
    }

    private static DensityFunction underground(PositionalRandomFactory randomFactory,
                                                DensityFunction slopedCheese,
                                                DensityFunction spaghetti2dFunc,
                                                DensityFunction spaghettiRoughnessFunc,
                                                DensityFunction entrancesFunc,
                                                DensityFunction pillarsFunc,
                                                boolean cheeseCaves) {
        DensityFunction baseCaveDensity;
        if (cheeseCaves) {
            DensityFunction.NoiseHolder layerNoise = noiseHolder(randomFactory, caveLayer(), "cave_layer");
            DensityFunction.NoiseHolder cheeseNoise = noiseHolder(randomFactory, caveCheese(), "cave_cheese");

            DensityFunction layerNoiseSource = DensityFunctions.noise(layerNoise, 8.0);
            DensityFunction layerizedCaverns = DensityFunctions.mul(DensityFunctions.constant(4.0), layerNoiseSource.square());
            DensityFunction cheese = DensityFunctions.noise(cheeseNoise, 0.6666666666666666);
            DensityFunction solidifiedCheeseWithTopSlide = DensityFunctions.add(
                    DensityFunctions.add(DensityFunctions.constant(0.27), cheese).clamp(-1.0, 1.0),
                    DensityFunctions.add(DensityFunctions.constant(1.5),
                            DensityFunctions.mul(DensityFunctions.constant(-0.64), slopedCheese)).clamp(0.0, 0.5));
            baseCaveDensity = DensityFunctions.add(layerizedCaverns, solidifiedCheeseWithTopSlide);
        } else {
            baseCaveDensity = DensityFunctions.constant(1000000.0);
        }
        DensityFunction undergroundSubtractions = DensityFunctions.min(
                DensityFunctions.min(baseCaveDensity, entrancesFunc),
                DensityFunctions.add(spaghetti2dFunc, spaghettiRoughnessFunc));
        DensityFunction pillarsWithCutoff = DensityFunctions.rangeChoice(pillarsFunc, -1000000.0, 0.03,
                DensityFunctions.constant(-1000000.0), pillarsFunc);
        return DensityFunctions.max(undergroundSubtractions, pillarsWithCutoff);
    }

    // Preliminary Surface Level

    private static DensityFunction preliminarySurfaceLevel(DensityFunction offset, DensityFunction factor,
                                                            boolean amplified, NoiseSettings settings) {
        int minY = settings.minY();
        int maxY = minY + settings.height();

        DensityFunction cachedFactor = DensityFunctions.cache2d(factor);
        DensityFunction cachedOffset = DensityFunctions.cache2d(offset);
        DensityFunction upperBound = remap(
                DensityFunctions.add(
                        DensityFunctions.mul(DensityFunctions.constant(0.2734375), cachedFactor.invert()),
                        DensityFunctions.mul(DensityFunctions.constant(-1.0), cachedOffset)),
                1.5, -1.5, (double) minY, (double) maxY);
        upperBound = upperBound.clamp(-40.0, (double) maxY);
        DensityFunction density = DensityFunctions.add(
                slideOverworld(amplified,
                        DensityFunctions.add(
                                noiseGradientDensity(cachedFactor, offsetToDepth(cachedOffset, settings)),
                                DensityFunctions.constant(CHEESE_NOISE_TARGET)).clamp((double) minY, 64.0),
                        settings),
                DensityFunctions.constant(-0.390625));
        return DensityFunctions.findTopSurface(density, upperBound, minY, settings.getCellHeight());
    }

    // Main Public API

    /**
     * Creates the complete overworld NoiseRouter. This is the main entry point.
     *
     * @param randomFactory positional random factory seeded with world seed
     * @param largeBiomes whether to use large biome noise parameters
     * @param amplified whether to use amplified terrain
     * @param cheeseCaves whether to generate cheese caves
     * @param spaghettiCaves whether to generate spaghetti caves
     * @param noodleCaves whether to generate noodle caves
     * @return fully assembled NoiseRouter for the overworld
     */
    public static NoiseRouter overworld(PositionalRandomFactory randomFactory, boolean largeBiomes, boolean amplified, boolean cheeseCaves, boolean spaghettiCaves, boolean noodleCaves) {
        NoiseSettings settings = NoiseSettings.overworld();
        // Aquifer noises
        DensityFunction.NoiseHolder barrierHolder = noiseHolder(randomFactory, aquiferBarrier(), "aquifer_barrier");
        DensityFunction.NoiseHolder floodednessHolder = noiseHolder(randomFactory, aquiferFloodedness(), "aquifer_fluid_level_floodedness");
        DensityFunction.NoiseHolder spreadHolder = noiseHolder(randomFactory, aquiferSpread(), "aquifer_fluid_level_spread");
        DensityFunction.NoiseHolder lavaHolder = noiseHolder(randomFactory, aquiferLava(), "aquifer_lava");

        DensityFunction barrierNoise = DensityFunctions.noise(barrierHolder, 0.5);
        DensityFunction fluidLevelFloodedness = DensityFunctions.noise(floodednessHolder, 0.67);
        DensityFunction fluidLevelSpread = DensityFunctions.noise(spreadHolder, 0.7142857142857143);
        DensityFunction lavaNoise = DensityFunctions.noise(lavaHolder);

        // Shift noises
        DensityFunction.NoiseHolder shiftHolder = noiseHolder(randomFactory, shift(), "offset");
        DensityFunction shiftX = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(shiftHolder)));
        DensityFunction shiftZ = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(shiftHolder)));

        // Climate noises
        DensityFunction.NoiseHolder tempHolder = noiseHolder(randomFactory, temperature(), "temperature");
        DensityFunction.NoiseHolder vegHolder = noiseHolder(randomFactory, vegetation(), "vegetation");
        DensityFunction.NoiseHolder contHolder = noiseHolder(randomFactory, continentalness(), "continentalness");
        DensityFunction.NoiseHolder erosHolder = noiseHolder(randomFactory, erosion(), "erosion");
        DensityFunction.NoiseHolder ridgeHolder = noiseHolder(randomFactory, ridge(), "ridge");

        DensityFunction temperatureFunc = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, tempHolder);
        DensityFunction vegetationFunc = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, vegHolder);

        DensityFunction continentsFunc = DensityFunctions.flatCache(
                DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, contHolder));
        DensityFunction erosionFunc = DensityFunctions.flatCache(
                DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, erosHolder));
        DensityFunction ridgesFunc = DensityFunctions.flatCache(
                DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, ridgeHolder));
        DensityFunction ridgesFolded = peaksAndValleys(ridgesFunc);

        // Y function
        DensityFunction y = DensityFunctions.yClampedGradient(-2048, 2048, -2048.0, 2048.0);

        // Base 3D noise — must be seeded with world seed (vanilla does this via RandomState visitor)
        DensityFunction base3dNoise = new BlendedNoise(
                randomFactory.fromHashOf("terrain"), 0.25, 0.125, 80.0, 160.0, 8.0);

        // Jagged noise
        DensityFunction.NoiseHolder jaggedHolder = noiseHolder(randomFactory, jagged(), "jagged");
        DensityFunction jaggedNoise = DensityFunctions.noise(jaggedHolder, 1500.0, 0.0);

        // Terrain splines
        DensityFunction offset = splineWithBlending(
                DensityFunctions.add(
                        DensityFunctions.constant((double) GLOBAL_OFFSET),
                        DensityFunctions.spline(TerrainProvider.overworldOffset(continentsFunc, erosionFunc, ridgesFolded, amplified))),
                DensityFunctions.blendOffset());
        DensityFunction factor = splineWithBlending(
                DensityFunctions.spline(TerrainProvider.overworldFactor(continentsFunc, erosionFunc, ridgesFunc, ridgesFolded, amplified)),
                DensityFunctions.constant(10.0));
        DensityFunction depth = offsetToDepth(offset, settings);
        DensityFunction unscaledJaggedness = splineWithBlending(
                DensityFunctions.spline(TerrainProvider.overworldJaggedness(continentsFunc, erosionFunc, ridgesFunc, ridgesFolded, amplified)),
                DensityFunctions.zero());
        DensityFunction jaggedness = DensityFunctions.mul(unscaledJaggedness, jaggedNoise.halfNegative());
        DensityFunction initialDensity = noiseGradientDensity(factor, DensityFunctions.add(depth, jaggedness));
        DensityFunction slopedCheese = DensityFunctions.add(initialDensity, base3dNoise);

        // Preliminary surface level
        DensityFunction prelimSurface = preliminarySurfaceLevel(offset, factor, amplified, settings);

        // Cave functions (conditionally disabled via config)
        DensityFunction spaghettiRoughnessFunc;
        DensityFunction spaghetti2dFunc;
        if (spaghettiCaves) {
            spaghettiRoughnessFunc = spaghettiRoughnessFunction(randomFactory);
            DensityFunction.NoiseHolder s2dThicknessNoise = noiseHolder(randomFactory, spaghetti2dThickness(), "spaghetti_2d_thickness");
            DensityFunction s2dThicknessModulator = DensityFunctions.cacheOnce(
                    DensityFunctions.mappedNoise(s2dThicknessNoise, 2.0, 1.0, -0.6, -1.3));
            spaghetti2dFunc = spaghetti2D(randomFactory, s2dThicknessModulator, settings);
        } else {
            spaghettiRoughnessFunc = DensityFunctions.zero();
            spaghetti2dFunc = DensityFunctions.constant(1000000.0);
        }
        DensityFunction entrancesFunc = entrances(randomFactory, spaghettiRoughnessFunc);
        DensityFunction pillarsFunc = pillars(randomFactory);
        DensityFunction noodleFunc = noodleCaves ? noodleCaves(randomFactory, y, settings)
                : DensityFunctions.constant(1000000.0);

        // Combine surface + caves
        DensityFunction surfaceWithEntrances = DensityFunctions.min(slopedCheese,
                DensityFunctions.mul(DensityFunctions.constant(5.0), entrancesFunc));
        DensityFunction caves = DensityFunctions.rangeChoice(slopedCheese, -1000000.0, SURFACE_DENSITY_THRESHOLD,
                surfaceWithEntrances, underground(randomFactory, slopedCheese, spaghetti2dFunc,
                        spaghettiRoughnessFunc, entrancesFunc, pillarsFunc, cheeseCaves));
        DensityFunction fullNoise = DensityFunctions.min(postProcess(slideOverworld(amplified, caves, settings)), noodleFunc);

        // Ore veins
        int veinMinY = -44;
        int veinMaxY = 44;
        DensityFunction.NoiseHolder veinToggleNoise = noiseHolder(randomFactory, oreVeininess(), "ore_veininess");
        DensityFunction.NoiseHolder veinANoise = noiseHolder(randomFactory, oreVeinA(), "ore_vein_a");
        DensityFunction.NoiseHolder veinBNoise = noiseHolder(randomFactory, oreVeinB(), "ore_vein_b");
        DensityFunction.NoiseHolder veinGapNoise = noiseHolder(randomFactory, oreGap(), "ore_gap");

        DensityFunction veinToggle = yLimitedInterpolatable(y,
                DensityFunctions.noise(veinToggleNoise, 1.5, 1.5), veinMinY, veinMaxY, 0);
        DensityFunction veinA = yLimitedInterpolatable(y,
                DensityFunctions.noise(veinANoise, 4.0, 4.0), veinMinY, veinMaxY, 0).abs();
        DensityFunction veinB = yLimitedInterpolatable(y,
                DensityFunctions.noise(veinBNoise, 4.0, 4.0), veinMinY, veinMaxY, 0).abs();
        DensityFunction veinRidged = DensityFunctions.add(DensityFunctions.constant(-0.08),
                DensityFunctions.max(veinA, veinB));
        DensityFunction veinGap = DensityFunctions.noise(veinGapNoise);

        return new NoiseRouter(barrierNoise, fluidLevelFloodedness, fluidLevelSpread, lavaNoise,
                temperatureFunc, vegetationFunc, continentsFunc, erosionFunc, depth, ridgesFunc,
                prelimSurface, fullNoise, veinToggle, veinRidged, veinGap);
    }

    /** Creates a NoiseRouter with all zeroes except finalDensity. */
    public static NoiseRouter none() {
        return simpleRouter(DensityFunctions.zero());
    }

    private static NoiseRouter simpleRouter(DensityFunction finalDensity) {
        DensityFunction z = DensityFunctions.zero();
        return new NoiseRouter(z, z, z, z, z, z, z, z, z, z, z, finalDensity, z, z, z);
    }
}
