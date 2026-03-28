package sayys.depthsupdate.util;

import net.minecraft.world.World;

public final class DimensionHelper {
    public static final int OVERWORLD_DIM = 0;
    public static final int EXTENDED_MIN_Y = -64;
    public static final int EXTENDED_MAX_Y = 256;
    public static final int EXTENDED_TOTAL_HEIGHT = 320;
    public static final int EXTENDED_STORAGE_SECTIONS = 20;
    public static final int SECTION_OFFSET = 4;
    public static final int VANILLA_STORAGE_SECTIONS = 16;
    public static final int VANILLA_TOTAL_HEIGHT = 256;
    public static final int VANILLA_MIN_Y = 0;

    private DimensionHelper() {}

    public static boolean isExtendedDimension(World world) {
        return world != null && world.provider.getDimension() == OVERWORLD_DIM;
    }

    /**
     * Returns the minimum Y coordinate for the given world.
     */
    public static int getMinY(World world) {
        return isExtendedDimension(world) ? EXTENDED_MIN_Y : VANILLA_MIN_Y;
    }

    /**
     * Returns the total height for the given world.
     */
    public static int getTotalHeight(World world) {
        return isExtendedDimension(world) ? EXTENDED_TOTAL_HEIGHT : VANILLA_TOTAL_HEIGHT;
    }

    /**
     * Returns the number of storage array sections for the given world.
     */
    public static int getStorageArraySize(World world) {
        return isExtendedDimension(world) ? EXTENDED_STORAGE_SECTIONS : VANILLA_STORAGE_SECTIONS;
    }

    /**
     * Converts a world Y coordinate to a storage array index.
     * For extended dimensions:
     * - Y >= 0: index = y >> 4 (Indices 0-15)
     * - Y < 0 : index = 15 - (y >> 4) (Indices 16-19)
     * For vanilla dimensions: y >> 4
     */
    public static int toStorageIndex(World world, int y) {
        return toStorageIndex(isExtendedDimension(world), y);
    }

    public static int toStorageIndex(boolean isExtended, int y) {
        int sectionY = y >> 4;

        if (isExtended) {
            // Vanilla range
            if (sectionY >= 0 && sectionY < 16) {
                return sectionY;
            }

            // Negative range
            if (sectionY < 0 && sectionY >= -4) {
                return 15 - sectionY;
            }

            if (sectionY >= 16 && sectionY < 20) {
                return sectionY;
            }
        }

        return sectionY;
    }

    /**
     * Converts a storage array index back to a section Y coordinate.
     */
    public static int fromStorageIndex(World world, int index) {
        return fromStorageIndex(isExtendedDimension(world), index);
    }

    public static int fromStorageIndex(boolean isExtended, int index) {
        if (isExtended) {
            // Vanilla range
            if (index >= 0 && index < 16) {
                return index;
            }

            // Negative range
            if (index >= 16 && index < 20) {
                return 15 - index;
            }
        }

        return index;
    }
}
