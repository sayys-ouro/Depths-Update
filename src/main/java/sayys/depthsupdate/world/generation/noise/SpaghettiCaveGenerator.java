package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public class SpaghettiCaveGenerator implements ICaveGenerator {
    private static final double SCALE = 0.035;
    private static final double THICKNESS = 0.025;
    /** Blocks below caveMaxY over which the noodles taper away. */
    private static final int FADE_BAND = 10;

    private final IBlockState debugBlockBlockState;

    private final Perlin noiseA;
    private final Perlin noiseB;

    // spaghetti caves fade out near the top of the cave range
    private final int fadeTopStart;
    private final int fadeTopRange;
    private final int caveMaxY;

    public SpaghettiCaveGenerator(long seed, int caveMaxY) {
        this.debugBlockBlockState = BlockUtils.getSpaghettiDebugBlockState();

        this.noiseA = new Perlin();
        this.noiseB = new Perlin();

        this.noiseA.setSeed((int) seed + 1337);
        this.noiseA.setOctaveCount(2);

        this.noiseB.setSeed((int) seed + 7331);
        this.noiseB.setOctaveCount(2);

        this.caveMaxY = caveMaxY;
        this.fadeTopStart = caveMaxY - FADE_BAND;
        this.fadeTopRange = FADE_BAND;
    }

    @Override
    public boolean canGenerate() {
        return DepthsUpdateConfig.generateSpaghettiCaves;
    }

    @Override
    public void sample(CaveSampleContext context) {
        if (context.y > this.caveMaxY) {
            return;
        }

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

        if (context.y > fadeTopStart) {
            fade = Math.min(1.0, (double) (context.y - fadeTopStart) / fadeTopRange);
        }

        double threshold = THICKNESS * (1.0 - fade);
        double value = noodleThickness;

        if (value < threshold) {
            context.shouldCarve = true;
            context.density = value - threshold;
        } else if (!context.shouldDebug && DepthsUpdateConfig.DEBUG.enableDebugVisualizers && value < THICKNESS + 0.03) {
            context.shouldDebug = true;
            context.debugBlock = debugBlockBlockState;
        }
    }
}
