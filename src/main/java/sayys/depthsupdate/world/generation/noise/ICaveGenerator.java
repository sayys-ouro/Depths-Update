package sayys.depthsupdate.world.generation.noise;

public interface ICaveGenerator {
    boolean canGenerate();

    default void prepare(int chunkX, int chunkZ) {
    }

    /** highestY is the tallest terrain column in the chunk; nothing above it is ever sampled. */
    default void prepare(int chunkX, int chunkZ, int highestY) {
        prepare(chunkX, chunkZ);
    }

    void sample(CaveSampleContext context);
}
