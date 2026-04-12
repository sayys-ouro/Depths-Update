package sayys.depthsupdate.world.biome;

import net.minecraft.world.biome.Biome;

/**
 * Stores biomes at 4x4x4 resolution for a single 16-block-tall chunk section.
 * Each section contains 4x4x4 = 64 biome entries.
 *
 * Index layout: x + z*4 + y*16 where x,y,z are in [0,3] (biome coordinates within section).
 * To convert block coordinates to biome coordinates: biomeCoord = blockCoord >> 2 (divide by 4).
 */
public class BiomeSection {

    public static final int BIOME_SIZE = 4;          // entries per axis
    public static final int BIOME_TOTAL = 4 * 4 * 4; // 64 entries per section
    public static final int BLOCK_TO_BIOME_SHIFT = 2; // >> 2 to convert block to biome coord

    private final Biome[] biomes;

    public BiomeSection() {
        this.biomes = new Biome[BIOME_TOTAL];
    }

    public BiomeSection(Biome fillBiome) {
        this.biomes = new Biome[BIOME_TOTAL];
        java.util.Arrays.fill(this.biomes, fillBiome);
    }

    /**
     * Gets the biome at the given biome-resolution coordinates (0-3 each).
     */
    public Biome getBiome(int biomeX, int biomeY, int biomeZ) {
        return this.biomes[indexOf(biomeX & 3, biomeY & 3, biomeZ & 3)];
    }

    /**
     * Sets the biome at the given biome-resolution coordinates (0-3 each).
     */
    public void setBiome(int biomeX, int biomeY, int biomeZ, Biome biome) {
        this.biomes[indexOf(biomeX & 3, biomeY & 3, biomeZ & 3)] = biome;
    }

    /**
     * Gets the biome from block coordinates local to the section (0-15 each).
     */
    public Biome getBiomeFromBlock(int localBlockX, int localBlockY, int localBlockZ) {
        return getBiome(localBlockX >> BLOCK_TO_BIOME_SHIFT,
                       localBlockY >> BLOCK_TO_BIOME_SHIFT,
                       localBlockZ >> BLOCK_TO_BIOME_SHIFT);
    }

    /**
     * Gets the raw biome array (for serialization).
     */
    public Biome[] getRawBiomes() {
        return this.biomes;
    }

    /**
     * Converts biome IDs to int array for network/disk serialization.
     */
    public int[] toIntArray() {
        int[] ids = new int[BIOME_TOTAL];
        for (int i = 0; i < BIOME_TOTAL; i++) {
            ids[i] = this.biomes[i] != null ? Biome.getIdForBiome(this.biomes[i]) : 0;
        }
        return ids;
    }

    /**
     * Fills from int array (deserialization).
     */
    public void fromIntArray(int[] ids) {
        for (int i = 0; i < Math.min(ids.length, BIOME_TOTAL); i++) {
            this.biomes[i] = Biome.getBiomeForId(ids[i]);
        }
    }

    /**
     * Returns whether all entries are null (section is empty/uninitialized).
     */
    public boolean isEmpty() {
        for (Biome b : this.biomes) {
            if (b != null) return false;
        }
        return true;
    }

    private static int indexOf(int x, int y, int z) {
        return x + z * BIOME_SIZE + y * BIOME_SIZE * BIOME_SIZE;
    }
}
