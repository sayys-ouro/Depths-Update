package sayys.depthsupdate.world.noise;

/**
 * Factory for creating deterministic RandomSource instances based on position or name.
 * Ensures the same coordinates always produce the same random sequence for a given world seed.
 */
public interface PositionalRandomFactory {

    RandomSource at(int x, int y, int z);

    RandomSource fromHashOf(String name);

    RandomSource fromSeed(long seed);
}
