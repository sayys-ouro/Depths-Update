package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.util.BlockUtils;

public class CheeseCaveGenerator implements ICaveGenerator {
    private static final double DEBUG_BAND = 0.25;

    private final IBlockState debugBlockBlockState;
    private final DensityField field;

    public CheeseCaveGenerator(long seed, double offsetX, double offsetY, double offsetZ, int caveMinY, int caveMaxY) {
        this.debugBlockBlockState = BlockUtils.getCheeseDebugBlockState();

        CheeseCaveNoise noise = new CheeseCaveNoise(seed, offsetX, offsetY, offsetZ, caveMaxY,
                DepthsUpdateConfig.cheeseCavesAbundance);
        this.field = new DensityField(noise::density, caveMinY, caveMaxY);
    }

    @Override
    public boolean canGenerate() {
        return DepthsUpdateConfig.generateCheeseCaves;
    }

    @Override
    public void prepare(int chunkX, int chunkZ) {
        this.field.prepare(chunkX, chunkZ);
    }

    @Override
    public void sample(@NonNull CaveSampleContext context) {
        double density = this.field.get(context.localX, context.y, context.localZ);

        if (density < 0.0) {
            context.shouldCarve = true;
            context.density = density;
        } else if (DepthsUpdateConfig.DEBUG.enableDebugVisualizers && density < DEBUG_BAND) {
            context.shouldDebug = true;
            context.debugBlock = this.debugBlockBlockState;
        }
    }
}
