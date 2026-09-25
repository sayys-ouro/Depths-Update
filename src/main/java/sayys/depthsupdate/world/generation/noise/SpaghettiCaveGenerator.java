package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.util.BlockUtils;

public class SpaghettiCaveGenerator implements ICaveGenerator {
    private static final double DEBUG_BAND = 0.03;

    private final IBlockState debugBlockBlockState;
    private final SpaghettiCaveNoise noise;

    public SpaghettiCaveGenerator(long seed) {
        this.debugBlockBlockState = BlockUtils.getSpaghettiDebugBlockState();
        this.noise = new SpaghettiCaveNoise(seed);
    }

    @Override
    public boolean canGenerate() {
        return DepthsUpdateConfig.generateSpaghettiCaves;
    }

    @Override
    public void sample(CaveSampleContext context) {
        double fade = SpaghettiCaveNoise.thicknessFade(context.depth);

        if (fade <= 0.0) {
            return;
        }

        double value = this.noise.ridge(context.realX, context.realY, context.realZ);

        context.offer(CaveType.SPAGHETTI, value - SpaghettiCaveNoise.THICKNESS * fade);

        if (!context.shouldDebug && DepthsUpdateConfig.DEBUG.enableDebugVisualizers
                && value >= SpaghettiCaveNoise.THICKNESS && value < SpaghettiCaveNoise.THICKNESS + DEBUG_BAND) {
            context.shouldDebug = true;
            context.debugBlock = this.debugBlockBlockState;
        }
    }
}
