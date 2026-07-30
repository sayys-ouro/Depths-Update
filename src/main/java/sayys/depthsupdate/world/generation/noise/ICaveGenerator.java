package sayys.depthsupdate.world.generation.noise;

public interface ICaveGenerator {
    boolean canGenerate();

    default void prepare(int chunkX, int chunkZ) {
    }

    void sample(CaveSampleContext context);
}
