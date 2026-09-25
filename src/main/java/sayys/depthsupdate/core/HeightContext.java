package sayys.depthsupdate.core;

import sayys.depthsupdate.api.HeightInfo;

/**
 * Storage Index Layout
 *   Indices 0-15: vanilla Y [0, 255]
 *   Indices 16 to 16 + upperSections - 1: upper Y [256, maxY)
 *   Indices 16 + upperSections to total - 1: negative Y [minY, 0) (closest-to-surface first)
 */
public final class HeightContext implements HeightInfo {
    public static final int MAX_STORAGE_SECTIONS = 32;

    public static final HeightContext VANILLA = new HeightContext(0, 256, -54, -64, 63);

    private final int minY;
    private final int maxY;
    private final int totalHeight;
    private final int minSection;
    private final int maxSection;
    private final int negativeSections;
    private final int upperSections;
    private final int totalStorageSections;
    private final int seaLevel;
    private final int lavaLevel;
    private final int voidDamageLevel;

    // Primer index layout: x << (yBitShift + 4) | z << yBitShift | (y - minY)
    private final int yBitShift;
    private final int primerArraySize;

    public HeightContext(int minY, int maxY, int lavaLevel, int voidDamageLevel) {
        this(minY, maxY, lavaLevel, voidDamageLevel, 63);
    }

    public HeightContext(int minY, int maxY, int lavaLevel, int voidDamageLevel, int seaLevel) {
        if (minY % 16 != 0) {
            throw new IllegalArgumentException("minY must be a multiple of 16, got " + minY);
        }

        if (maxY % 16 != 0) {
            throw new IllegalArgumentException("maxY must be a multiple of 16, got " + maxY);
        }

        if (minY >= maxY) {
            throw new IllegalArgumentException("minY (" + minY + ") must be less than maxY (" + maxY + ")");
        }

        if (maxY - minY > 4096) {
            throw new IllegalArgumentException("Total height (" + (maxY - minY) + ") exceeds maximum of 4096");
        }

        int sections = 16 + Math.max(0, ((maxY - 1) >> 4) - 15) + Math.max(0, -(minY >> 4));

        if (sections > MAX_STORAGE_SECTIONS) {
            throw new IllegalArgumentException("Height " + minY + ".." + maxY + " needs " + sections + " chunk sections, which exceeds the " + MAX_STORAGE_SECTIONS + " a section bitmask can hold");
        }

        this.minY = minY;
        this.maxY = maxY;
        this.totalHeight = maxY - minY;
        this.minSection = minY >> 4;
        this.maxSection = (maxY - 1) >> 4;
        this.negativeSections = Math.max(0, -(minY >> 4));
        this.upperSections = Math.max(0, ((maxY - 1) >> 4) - 15);
        this.totalStorageSections = 16 + upperSections + negativeSections;
        this.seaLevel = seaLevel;
        this.lavaLevel = lavaLevel;
        this.voidDamageLevel = voidDamageLevel;

        int bits = 0;
        int h = totalHeight - 1;

        while (h > 0) {
            h >>= 1;
            bits++;
        }

        this.yBitShift = bits;
        this.primerArraySize = 16 * 16 * (1 << bits);
    }

    /**
     * Converts a world Y coordinate to a storage array index.
     * Returns -1 if out of bounds.
     */
    public int toStorageIndex(int y) {
        int sectionY = y >> 4;

        // Vanilla range: sectionY 0-15 -> indices 0-15
        if (sectionY >= 0 && sectionY < 16) {
            return sectionY;
        }

        // Upper extension: sectionY 16+ -> indices 16+
        if (sectionY >= 16 && sectionY <= maxSection) {
            return sectionY;
        }

        // Negative range: closest to surface first
        // sectionY -N -> index (16 + upperSections + N - 1)
        if (sectionY < 0 && sectionY >= minSection) {
            return 16 + upperSections + (-1 - sectionY);
        }

        return -1;
    }

    /**
     * Converts a storage array index back to a section Y coordinate.
     */
    public int fromStorageIndex(int index) {
        // Vanilla range
        if (index >= 0 && index < 16) {
            return index;
        }

        // Upper extension
        int upperEnd = 16 + upperSections;
        if (index >= 16 && index < upperEnd) {
            return index; // index IS the sectionY for upper sections
        }

        // Negative range
        int negStart = upperEnd;
        int negEnd = negStart + negativeSections;

        if (index >= negStart && index < negEnd) {
            return -1 - (index - negStart);
        }

        return Integer.MIN_VALUE;
    }

    /**
     * Computes the ChunkPrimer array index for a block position.
     */
    public int toPrimerIndex(int x, int y, int z) {
        return (x << (yBitShift + 4)) | (z << yBitShift) | (y - minY);
    }

    @Override public boolean isInBounds(int y) {
        return y >= minY && y < maxY;
    }

    /** Whether this context extends beyond vanilla [0, 256). */
    @Override public boolean isExtended() {
        return minY < 0 || maxY > 256;
    }

    @Override public int minY() { return minY; }
    @Override public int maxY() { return maxY; }
    @Override public int totalHeight() { return totalHeight; }
    public int minSection() { return minSection; }
    public int maxSection() { return maxSection; }
    public int negativeSections() { return negativeSections; }
    public int upperSections() { return upperSections; }
    public int totalStorageSections() { return totalStorageSections; }
    @Override public int seaLevel() { return seaLevel; }
    @Override public int lavaLevel() { return lavaLevel; }
    @Override public int voidDamageLevel() { return voidDamageLevel; }
    public int yBitShift() { return yBitShift; }
    public int primerArraySize() { return primerArraySize; }

    /**
     * Returns the full-chunk bitmask for network packets.
     * Each bit represents one storage section.
     */
    public int fullChunkSectionMask() {
        if (totalStorageSections >= MAX_STORAGE_SECTIONS) {
            return -1; // all 32 bits set;
        }

        return (1 << totalStorageSections) - 1;
    }

    @Override
    public String toString() {
        return "HeightContext[minY=" + minY + ", maxY=" + maxY
                + ", sections=" + totalStorageSections
                + " (vanilla=16, upper=" + upperSections + ", negative=" + negativeSections + ")]";
    }
}
