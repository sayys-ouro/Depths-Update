package sayys.depthsupdate.world.density;

/**
 * Container for all 15 density functions that drive terrain generation.
 * Faithful backport of modern Minecraft's NoiseRouter record.
 */
public final class NoiseRouter {
    private final DensityFunction barrierNoise;
    private final DensityFunction fluidLevelFloodednessNoise;
    private final DensityFunction fluidLevelSpreadNoise;
    private final DensityFunction lavaNoise;
    private final DensityFunction temperature;
    private final DensityFunction vegetation;
    private final DensityFunction continents;
    private final DensityFunction erosion;
    private final DensityFunction depth;
    private final DensityFunction ridges;
    private final DensityFunction preliminarySurfaceLevel;
    private final DensityFunction finalDensity;
    private final DensityFunction veinToggle;
    private final DensityFunction veinRidged;
    private final DensityFunction veinGap;

    public NoiseRouter(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise,
                       DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise,
                       DensityFunction temperature, DensityFunction vegetation,
                       DensityFunction continents, DensityFunction erosion,
                       DensityFunction depth, DensityFunction ridges,
                       DensityFunction preliminarySurfaceLevel, DensityFunction finalDensity,
                       DensityFunction veinToggle, DensityFunction veinRidged,
                       DensityFunction veinGap) {
        this.barrierNoise = barrierNoise;
        this.fluidLevelFloodednessNoise = fluidLevelFloodednessNoise;
        this.fluidLevelSpreadNoise = fluidLevelSpreadNoise;
        this.lavaNoise = lavaNoise;
        this.temperature = temperature;
        this.vegetation = vegetation;
        this.continents = continents;
        this.erosion = erosion;
        this.depth = depth;
        this.ridges = ridges;
        this.preliminarySurfaceLevel = preliminarySurfaceLevel;
        this.finalDensity = finalDensity;
        this.veinToggle = veinToggle;
        this.veinRidged = veinRidged;
        this.veinGap = veinGap;
    }

    public NoiseRouter mapAll(DensityFunction.Visitor visitor) {
        return new NoiseRouter(
                this.barrierNoise.mapAll(visitor),
                this.fluidLevelFloodednessNoise.mapAll(visitor),
                this.fluidLevelSpreadNoise.mapAll(visitor),
                this.lavaNoise.mapAll(visitor),
                this.temperature.mapAll(visitor),
                this.vegetation.mapAll(visitor),
                this.continents.mapAll(visitor),
                this.erosion.mapAll(visitor),
                this.depth.mapAll(visitor),
                this.ridges.mapAll(visitor),
                this.preliminarySurfaceLevel.mapAll(visitor),
                this.finalDensity.mapAll(visitor),
                this.veinToggle.mapAll(visitor),
                this.veinRidged.mapAll(visitor),
                this.veinGap.mapAll(visitor));
    }

    public DensityFunction barrierNoise() { return barrierNoise; }
    public DensityFunction fluidLevelFloodednessNoise() { return fluidLevelFloodednessNoise; }
    public DensityFunction fluidLevelSpreadNoise() { return fluidLevelSpreadNoise; }
    public DensityFunction lavaNoise() { return lavaNoise; }
    public DensityFunction temperature() { return temperature; }
    public DensityFunction vegetation() { return vegetation; }
    public DensityFunction continents() { return continents; }
    public DensityFunction erosion() { return erosion; }
    public DensityFunction depth() { return depth; }
    public DensityFunction ridges() { return ridges; }
    public DensityFunction preliminarySurfaceLevel() { return preliminarySurfaceLevel; }
    public DensityFunction finalDensity() { return finalDensity; }
    public DensityFunction veinToggle() { return veinToggle; }
    public DensityFunction veinRidged() { return veinRidged; }
    public DensityFunction veinGap() { return veinGap; }
}
