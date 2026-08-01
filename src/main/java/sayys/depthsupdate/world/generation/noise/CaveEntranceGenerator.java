package sayys.depthsupdate.world.generation.noise;

import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;

public class CaveEntranceGenerator implements ICaveGenerator {
    private final DensityField field;
    private final int maxY;

    public CaveEntranceGenerator(long seed, double offsetX, double offsetY, double offsetZ, int caveMinY, int maxY) {
        this.maxY = maxY;

        CaveEntranceNoise noise = new CaveEntranceNoise(seed, offsetX, offsetY, offsetZ);
        this.field = new DensityField(noise::density, caveMinY, maxY);
    }

    @Override
    public boolean canGenerate() {
        return DepthsUpdateConfig.generateCaveEntrances;
    }

    @Override
    public void prepare(int chunkX, int chunkZ) {
        this.field.prepare(chunkX, chunkZ);
    }

    @Override
    public void sample(@NonNull CaveSampleContext context) {
        if (context.y > this.maxY) {
            return;
        }

        double density = this.field.get(context.localX, context.y, context.localZ);

        if (density < 0.0) {
            context.shouldCarve = true;
            context.density = density;
        }
    }
}
