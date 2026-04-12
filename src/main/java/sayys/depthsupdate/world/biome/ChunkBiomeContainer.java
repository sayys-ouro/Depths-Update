package sayys.depthsupdate.world.biome;

import net.minecraft.world.biome.Biome;

/**
 * 3D biome container for an entire chunk column. Stores one BiomeSection per chunk section
 * (each section is 16 blocks tall), enabling full 3D biome resolution at 4x4x4 granularity.
 *
 * Replaces the legacy byte[256] per-column biome storage from 1.12.2.
 * Provides a shim layer for legacy code that queries biomes by (x, z) only.
 */
public class ChunkBiomeContainer {

    private final int minSectionY;
    private final int sectionCount;
    private final BiomeSection[] sections;

    /**
     * @param minY minimum world Y coordinate (e.g. -64 for extended height)
     * @param height total world height in blocks (e.g. 384)
     */
    public ChunkBiomeContainer(int minY, int height) {
        this.minSectionY = minY >> 4;
        this.sectionCount = height >> 4;
        this.sections = new BiomeSection[this.sectionCount];
    }

    /**
     * Creates a container pre-filled with a single biome.
     */
    public ChunkBiomeContainer(int minY, int height, Biome fillBiome) {
        this(minY, height);
        for (int i = 0; i < this.sectionCount; i++) {
            this.sections[i] = new BiomeSection(fillBiome);
        }
    }

    /**
     * Gets the biome at the given block coordinates (world-space).
     */
    public Biome getBiome(int blockX, int blockY, int blockZ) {
        int sectionIndex = (blockY >> 4) - this.minSectionY;
        if (sectionIndex < 0 || sectionIndex >= this.sectionCount) {
            // Clamp to nearest valid section
            sectionIndex = Math.max(0, Math.min(sectionIndex, this.sectionCount - 1));
        }
        BiomeSection section = this.sections[sectionIndex];
        if (section == null) {
            return null;
        }
        int localY = blockY & 15;
        return section.getBiomeFromBlock(blockX & 15, localY, blockZ & 15);
    }

    /**
     * Gets the biome at biome-resolution coordinates (world-space, each unit = 4 blocks).
     */
    public Biome getBiomeAtQuart(int quartX, int quartY, int quartZ) {
        return getBiome(quartX << 2, quartY << 2, quartZ << 2);
    }

    /**
     * Sets the biome at the given block coordinates (world-space).
     */
    public void setBiome(int blockX, int blockY, int blockZ, Biome biome) {
        int sectionIndex = (blockY >> 4) - this.minSectionY;
        if (sectionIndex < 0 || sectionIndex >= this.sectionCount) {
            return;
        }
        BiomeSection section = this.sections[sectionIndex];
        if (section == null) {
            section = new BiomeSection();
            this.sections[sectionIndex] = section;
        }
        int localY = blockY & 15;
        section.setBiome(
                (blockX & 15) >> BiomeSection.BLOCK_TO_BIOME_SHIFT,
                localY >> BiomeSection.BLOCK_TO_BIOME_SHIFT,
                (blockZ & 15) >> BiomeSection.BLOCK_TO_BIOME_SHIFT,
                biome);
    }

    /**
     * Sets the biome at biome-resolution coordinates (world-space).
     */
    public void setBiomeAtQuart(int quartX, int quartY, int quartZ, Biome biome) {
        setBiome(quartX << 2, quartY << 2, quartZ << 2, biome);
    }

    /**
     * Gets the BiomeSection for a given section index.
     */
    public BiomeSection getSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= this.sectionCount) {
            return null;
        }
        return this.sections[sectionIndex];
    }

    /**
     * Sets the BiomeSection for a given section index.
     */
    public void setSection(int sectionIndex, BiomeSection section) {
        if (sectionIndex >= 0 && sectionIndex < this.sectionCount) {
            this.sections[sectionIndex] = section;
        }
    }

    // ===== Legacy 2D Shim =====

    /**
     * Gets the biome at a column position, sampling from sea level (y=63).
     * This is the shim for legacy code that only queries biome by (x, z).
     *
     * @param localX local X within chunk (0-15)
     * @param localZ local Z within chunk (0-15)
     * @return biome at sea level for that column
     */
    public Biome getLegacyBiome(int localX, int localZ) {
        return getBiome(localX, 63, localZ);
    }

    /**
     * Generates a legacy byte[256] biome array from the 3D data,
     * sampling at sea level (y=63). For compatibility with vanilla serialization
     * and mods that expect the old format.
     */
    public byte[] toLegacyBiomeArray() {
        byte[] legacy = new byte[256];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                Biome biome = getBiome(x, 63, z);
                legacy[z * 16 + x] = (byte) (biome != null ? Biome.getIdForBiome(biome) : 0);
            }
        }
        return legacy;
    }

    /**
     * Fills the 3D container from a legacy byte[256] biome array.
     * Fills all Y levels with the same biome per column.
     */
    public void fromLegacyBiomeArray(byte[] legacy) {
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                Biome biome = Biome.getBiomeForId(legacy[z * 16 + x] & 0xFF);
                if (biome != null) {
                    // Fill entire column with this biome
                    int biomeX = x >> BiomeSection.BLOCK_TO_BIOME_SHIFT;
                    int biomeZ = z >> BiomeSection.BLOCK_TO_BIOME_SHIFT;
                    for (int sectionIdx = 0; sectionIdx < this.sectionCount; sectionIdx++) {
                        BiomeSection section = this.sections[sectionIdx];
                        if (section == null) {
                            section = new BiomeSection();
                            this.sections[sectionIdx] = section;
                        }
                        for (int biomeY = 0; biomeY < BiomeSection.BIOME_SIZE; biomeY++) {
                            section.setBiome(biomeX, biomeY, biomeZ, biome);
                        }
                    }
                }
            }
        }
    }

    /**
     * Serializes the entire 3D biome data to an int array.
     * Layout: section0[64], section1[64], ..., sectionN[64]
     */
    public int[] toIntArray() {
        int[] data = new int[this.sectionCount * BiomeSection.BIOME_TOTAL];
        for (int i = 0; i < this.sectionCount; i++) {
            BiomeSection section = this.sections[i];
            if (section != null) {
                int[] sectionData = section.toIntArray();
                System.arraycopy(sectionData, 0, data, i * BiomeSection.BIOME_TOTAL, BiomeSection.BIOME_TOTAL);
            }
        }
        return data;
    }

    /**
     * Deserializes 3D biome data from an int array.
     */
    public void fromIntArray(int[] data) {
        for (int i = 0; i < this.sectionCount; i++) {
            int offset = i * BiomeSection.BIOME_TOTAL;
            if (offset + BiomeSection.BIOME_TOTAL <= data.length) {
                BiomeSection section = new BiomeSection();
                int[] sectionData = new int[BiomeSection.BIOME_TOTAL];
                System.arraycopy(data, offset, sectionData, 0, BiomeSection.BIOME_TOTAL);
                section.fromIntArray(sectionData);
                this.sections[i] = section;
            }
        }
    }

    public int minSectionY() { return minSectionY; }
    public int sectionCount() { return sectionCount; }
}
