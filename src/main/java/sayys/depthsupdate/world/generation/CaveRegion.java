package sayys.depthsupdate.world.generation;

import java.util.Random;

import net.minecraft.util.math.BlockPos;

import sayys.depthsupdate.core.HeightContext;

/** Shared ellipsoid maths for the biome-pocket generators. */
public final class CaveRegion {
    /** Returned by {@link #randomYInWindow} when the window misses the world entirely. */
    public static final int NO_Y = Integer.MIN_VALUE;

    private CaveRegion() {
    }

    public static int randomYInWindow(Random random, HeightContext ctx, int windowMin, int windowMax) {
        int low = Math.max(Math.min(windowMin, windowMax), ctx.minY());
        int high = Math.min(Math.max(windowMin, windowMax), ctx.maxY() - 1);

        return low > high ? NO_Y : low + random.nextInt(high - low + 1);
    }

    public static double volume(int radiusX, int radiusY, int radiusZ) {
        return 4.0 * Math.PI / 3.0 * radiusX * radiusY * radiusZ;
    }

    public static BlockPos randomPointInside(Random random, BlockPos center, int radiusX, int radiusY, int radiusZ) {
        while (true) {
            double x = random.nextDouble() * 2.0 - 1.0;
            double y = random.nextDouble() * 2.0 - 1.0;
            double z = random.nextDouble() * 2.0 - 1.0;

            if (x * x + y * y + z * z <= 1.0) {
                return center.add((int) (x * radiusX), (int) (y * radiusY), (int) (z * radiusZ));
            }
        }
    }
}
