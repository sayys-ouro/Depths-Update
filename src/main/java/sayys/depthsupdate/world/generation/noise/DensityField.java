package sayys.depthsupdate.world.generation.noise;

public final class DensityField {
    @FunctionalInterface
    public interface Sampler {
        double density(double x, int y, double z);
    }

    public static final int CELL_WIDTH = 4;
    public static final int CELL_HEIGHT = 8;

    private static final int GRID_WIDTH = 16 / CELL_WIDTH + 1;

    private final Sampler sampler;
    private final int gridMinY;
    private final int gridHeight;
    private final double[] values;

    public DensityField(Sampler sampler, int minY, int maxY) {
        this.sampler = sampler;
        this.gridMinY = Math.floorDiv(minY, CELL_HEIGHT) * CELL_HEIGHT;

        int gridMaxY = Math.ceilDiv(maxY + 1, CELL_HEIGHT) * CELL_HEIGHT;

        this.gridHeight = (gridMaxY - this.gridMinY) / CELL_HEIGHT + 1;
        this.values = new double[GRID_WIDTH * GRID_WIDTH * this.gridHeight];
    }

    public void prepare(int chunkX, int chunkZ) {
        prepare(chunkX, chunkZ, Integer.MAX_VALUE);
    }

    /**
     * Fills only the rows needed to answer queries up to highestY. Fields that
     * reach the surface span most of the world, while any one chunk only ever
     * asks about its own terrain.
     */
    public void prepare(int chunkX, int chunkZ, int highestY) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        int index = 0;

        int rows = this.gridHeight;

        if (highestY != Integer.MAX_VALUE) {
            // get() interpolates between gridY and gridY + 1.
            rows = Math.min(rows, Math.max(2, (highestY - this.gridMinY) / CELL_HEIGHT + 2));
        }

        for (int gridY = 0; gridY < rows; gridY++) {
            int y = this.gridMinY + gridY * CELL_HEIGHT;

            for (int gridZ = 0; gridZ < GRID_WIDTH; gridZ++) {
                double z = worldZ + gridZ * CELL_WIDTH;

                for (int gridX = 0; gridX < GRID_WIDTH; gridX++) {
                    this.values[index++] = this.sampler.density(worldX + gridX * CELL_WIDTH, y, z);
                }
            }
        }
    }

    public double get(int localX, int y, int localZ) {
        int gridX = localX / CELL_WIDTH;
        int gridZ = localZ / CELL_WIDTH;
        int offsetY = y - this.gridMinY;
        int gridY = offsetY / CELL_HEIGHT;

        double fractionX = (localX % CELL_WIDTH) / (double) CELL_WIDTH;
        double fractionZ = (localZ % CELL_WIDTH) / (double) CELL_WIDTH;
        double fractionY = (offsetY % CELL_HEIGHT) / (double) CELL_HEIGHT;

        double lowerZ = lerp(fractionX,
                lerp(fractionY, at(gridX, gridY, gridZ), at(gridX, gridY + 1, gridZ)),
                lerp(fractionY, at(gridX + 1, gridY, gridZ), at(gridX + 1, gridY + 1, gridZ)));
        double upperZ = lerp(fractionX,
                lerp(fractionY, at(gridX, gridY, gridZ + 1), at(gridX, gridY + 1, gridZ + 1)),
                lerp(fractionY, at(gridX + 1, gridY, gridZ + 1), at(gridX + 1, gridY + 1, gridZ + 1)));

        return lerp(fractionZ, lowerZ, upperZ);
    }

    private double at(int gridX, int gridY, int gridZ) {
        return this.values[(gridY * GRID_WIDTH + gridZ) * GRID_WIDTH + gridX];
    }

    private static double lerp(double fraction, double from, double to) {
        return from + fraction * (to - from);
    }
}
