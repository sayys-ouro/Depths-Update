package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public class SpaghettiCaveGenerator implements ICaveGenerator {
    private static final double SCALE = 0.035;
    private static final double THICKNESS = 0.025;

    private final IBlockState debugBlockBlockState;

    private final Perlin noiseA;
    private final Perlin noiseB;

    public SpaghettiCaveGenerator(long seed) {
        this.debugBlockBlockState = BlockUtils.getSpaghettiDebugBlockState();

        this.noiseA = new Perlin();
        this.noiseB = new Perlin();

        this.noiseA.setSeed((int) seed + 1337);
        this.noiseA.setOctaveCount(2);

        this.noiseB.setSeed((int) seed + 7331);
        this.noiseB.setOctaveCount(2);
    }

    @Override
    public boolean canGenerate() {
        return DepthsUpdateConfig.generateSpaghettiCaves;
    }

    @Override
    public void sample(CaveSampleContext context) {
        double spagA = noiseA.getValue(
            context.realX * SCALE,
            context.realY * SCALE,
            context.realZ * SCALE
        );
        double spagB = noiseB.getValue(
            context.realX * SCALE,
            context.realY * SCALE,
            context.realZ * SCALE
        );

        double noodleThickness = Math.max(Math.abs(spagA), Math.abs(spagB));

        double fade = 0.0;

        if (context.y > 20) {
            fade = ((double) (context.y - 20) / 10.0);
        }

        double value = noodleThickness + (fade * 0.5);

        if (value < THICKNESS) {
            context.shouldCarve = true;
        } else if (!context.shouldDebug && DepthsUpdateConfig.DEBUG.enableDebugVisualizers && value < THICKNESS + 0.03) {
            context.shouldDebug = true;
            context.debugBlock = debugBlockBlockState;
        }
    }
}
