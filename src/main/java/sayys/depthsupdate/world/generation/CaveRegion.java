package sayys.depthsupdate.world.generation;

import java.util.Random;

import net.minecraft.util.math.BlockPos;

/** Shared ellipsoid maths for the biome-pocket generators. */
public final class CaveRegion {
    private CaveRegion() {
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
