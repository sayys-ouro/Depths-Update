package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public class CheeseCaveGenerator implements ICaveGenerator {
    private static final double THRESHOLD = 0.1;
    private static final double XZ_SCALE = 0.02;
    private static final double Y_SCALE = 0.0275;

    private static final double WARP_SCALE = 0.01;
    private static final double WARP_STRENGTH = 12.0;

    private final IBlockState debugBlockBlockState;

    private final Perlin noise;
    private final Perlin warpNoise;

    public CheeseCaveGenerator(long seed) {
        this.debugBlockBlockState = BlockUtils.getCheeseDebugBlockState();

        this.noise = new Perlin();
        this.warpNoise = new Perlin();

        this.noise.setSeed((int) seed + 420);
        this.noise.setOctaveCount(4);
        this.noise.setPersistence(0.5);

        this.warpNoise.setSeed((int) seed + 666);
        this.warpNoise.setOctaveCount(1);
        this.warpNoise.setFrequency(1.0);
    }

    @Override
    public boolean canGenerate() {
        return DepthsUpdateConfig.generateCheeseCaves;
    }

    @Override
    public void sample(@NonNull CaveSampleContext context) {
        double wx = warpNoise.getValue(
            context.realX * WARP_SCALE,
            context.realY * WARP_SCALE,
            context.realZ * WARP_SCALE
        );
        double wy = warpNoise.getValue(
            context.realZ * WARP_SCALE,
            context.realX * WARP_SCALE,
            context.realY * WARP_SCALE
        );
        double wz = warpNoise.getValue(
            context.realY * WARP_SCALE,
            context.realZ * WARP_SCALE,
            context.realX * WARP_SCALE
        );

        double dx = context.realX + (wx * WARP_STRENGTH);
        double dy = context.realY + (wy * WARP_STRENGTH);
        double dz = context.realZ + (wz * WARP_STRENGTH);

        double val = noise.getValue(
            dx * XZ_SCALE,
            dy * Y_SCALE,
            dz * XZ_SCALE
        );

        double fade = 0.0;

        if (context.y > 10) {
            fade = ((double) (context.y - 10) / 20.0);
        } else if (context.y < -50) {
            fade = ((double) (-50 - context.y) / 10.0);
        }

        double density = val - fade;

        if (density > THRESHOLD) {
            context.shouldCarve = true;
        } else if (DepthsUpdateConfig.DEBUG.enableDebugVisualizers && density > THRESHOLD - 0.2) {
            context.shouldDebug = true;
            context.debugBlock = debugBlockBlockState;
        }
    }
}
