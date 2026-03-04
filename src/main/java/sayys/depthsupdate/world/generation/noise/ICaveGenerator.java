package sayys.depthsupdate.world.generation.noise;

public interface ICaveGenerator {
    boolean canGenerate();

    void sample(CaveSampleContext context);
}
