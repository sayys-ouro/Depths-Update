package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;

public class CaveSampleContext {
    public double realX;
    public double realY;
    public double realZ;
    public int y;

    public boolean shouldCarve;
    public boolean shouldDebug;
    public IBlockState debugBlock;

    public void reset(double realX, double realY, double realZ, int y) {
        this.realX = realX;
        this.realY = realY;
        this.realZ = realZ;
        this.y = y;
        this.shouldCarve = false;
        this.shouldDebug = false;
        this.debugBlock = null;
    }
}
