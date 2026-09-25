package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;

/**
 * One block's sample, shared by every cave generator. Generators contribute
 * densities through {@link #offer}; the block is carved when the minimum goes
 * negative, which is vanilla's min() composition of its cave functions.
 */
public class CaveSampleContext {
    public double realX;
    public double realY;
    public double realZ;

    public int localX;
    public int localZ;
    public int y;

    /** Blocks below this column's terrain surface. Caves are placed by depth, not by absolute Y. */
    public int depth;

    /** Running minimum over all generators; the aquifer weighs barriers against it. */
    public double density;

    /**
     * One bit per generator that offered a negative density. The running
     * minimum names only the winner, which badly under-reports types that open
     * rock another type has already opened.
     */
    public int openMask;

    public boolean shouldDebug;
    public IBlockState debugBlock;

    public void reset(double realX, double realY, double realZ, int localX, int y, int localZ, int depth) {
        this.realX = realX;
        this.realY = realY;
        this.realZ = realZ;
        this.localX = localX;
        this.localZ = localZ;
        this.y = y;
        this.depth = depth;
        this.density = Double.POSITIVE_INFINITY;
        this.openMask = 0;
        this.shouldDebug = false;
        this.debugBlock = null;
    }

    public void offer(CaveType type, double density) {
        if (density < this.density) {
            this.density = density;
        }

        if (density < 0.0) {
            this.openMask |= 1 << type.ordinal();
        }
    }

    public boolean shouldCarve() {
        return this.density < 0.0;
    }
}
