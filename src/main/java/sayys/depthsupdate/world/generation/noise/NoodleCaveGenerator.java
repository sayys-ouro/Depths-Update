package sayys.depthsupdate.world.generation.noise;

import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;

public class NoodleCaveGenerator implements ICaveGenerator {
    private final NoodleCaveNoise noise;
    /** The selector varies over hundreds of blocks, so cell interpolation is safe for it. */
    private final DensityField selectorField;

    public NoodleCaveGenerator(long seed, int caveMinY, int caveMaxY) {
        this.noise = new NoodleCaveNoise(seed);
        this.selectorField = new DensityField(this.noise::selector, caveMinY, caveMaxY);
    }

    @Override
    public boolean canGenerate() {
        return DepthsUpdateConfig.generateNoodleCaves;
    }

    @Override
    public void prepare(int chunkX, int chunkZ) {
        this.selectorField.prepare(chunkX, chunkZ);
    }

    @Override
    public void prepare(int chunkX, int chunkZ, int highestY) {
        this.selectorField.prepare(chunkX, chunkZ, highestY);
    }

    @Override
    public void sample(@NonNull CaveSampleContext context) {
        if (this.selectorField.get(context.localX, context.y, context.localZ) < 0.0) {
            return;
        }

        context.offer(CaveType.NOODLE, this.noise.density(context.realX, context.realY, context.realZ, context.depth));
    }
}
