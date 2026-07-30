package sayys.depthsupdate.world.generation.noise;

/**
 * Applies {@link PillarNoise} over the chunk's noise grid. A block a cave would
 * carve stays solid where the interpolated pillar density reaches zero, which
 * is what forms the rock columns standing inside caverns.
 */
public class PillarGenerator {
    private final DensityField field;

    public PillarGenerator(long seed, double offsetX, double offsetY, double offsetZ, int caveMinY, int caveMaxY) {
        PillarNoise noise = new PillarNoise(seed, offsetX, offsetY, offsetZ);
        this.field = new DensityField(noise::density, caveMinY, caveMaxY);
    }

    public void prepare(int chunkX, int chunkZ) {
        this.field.prepare(chunkX, chunkZ);
    }

    public boolean isPillar(int localX, int y, int localZ) {
        return PillarNoise.isSolid(this.field.get(localX, y, localZ));
    }
}
