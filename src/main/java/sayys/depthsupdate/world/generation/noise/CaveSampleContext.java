package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;

public class CaveSampleContext {
    public double realX;
    public double realY;
    public double realZ;

    public int localX;
    public int localZ;
    public int y;

    public boolean shouldCarve;
    public boolean shouldDebug;
    public IBlockState debugBlock;

    /** Density of the generator that carved; the aquifer weighs barriers against it. */
    public double density;

    public void reset(double realX, double realY, double realZ, int localX, int y, int localZ) {
        this.realX = realX;
        this.realY = realY;
        this.realZ = realZ;
        this.localX = localX;
        this.localZ = localZ;
        this.y = y;
        this.shouldCarve = false;
        this.shouldDebug = false;
        this.debugBlock = null;
        this.density = 1.0;
    }
}
