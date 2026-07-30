package sayys.depthsupdate.mixin;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.noise.CaveNoiseGenerator;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

@Mixin(ChunkGeneratorOverworld.class)
public abstract class MixinChunkGeneratorOverworld {
    @Shadow
    private World world;

    @Shadow
    @Final
    private Random rand;

    @Unique
    private UndergroundRiverGenerator depthsupdate$riverGenerator;

    @Unique
    private CaveNoiseGenerator depthsupdate$noiseCaveGenerator;

    @Inject(method = "setBlocksInChunk", at = @At("RETURN"))
    private void depthsupdate$fillDeepUnderground(int x, int z, ChunkPrimer primer, CallbackInfo ci) {
        if (((Object) this).getClass() != ChunkGeneratorOverworld.class) {
            return;
        }

        HeightContext ctx = HeightManager.get(this.world);
        int minY = ctx.minY();
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState deepslate = BlockUtils.getDeepslateBlockState();

        int maxY = DepthsUpdateConfig.deepslateMaxY;
        int transitionRange = DepthsUpdateConfig.deepslateTransitionRange;
        int fullDeepslateY = maxY - transitionRange;

        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                for (int by = minY; by <= Math.max(0, maxY); by++) {
                    if (by <= minY + this.rand.nextInt(5)) {
                        primer.setBlockState(bx, by, bz, Blocks.BEDROCK.getDefaultState());
                    } else if (by <= fullDeepslateY) {
                        primer.setBlockState(bx, by, bz, deepslate);
                    } else if (by < maxY) {
                        double chance = (double) (maxY - by) / (double) transitionRange;

                        if (this.rand.nextDouble() < chance) {
                            primer.setBlockState(bx, by, bz, deepslate);
                        } else if (by < 0) {
                            primer.setBlockState(bx, by, bz, stone);
                        }
                    } else if (by < 0) {
                        primer.setBlockState(bx, by, bz, stone);
                    }

                }
            }
        }

        if (DepthsUpdateConfig.generateUndergroundRivers) {
            if (this.depthsupdate$riverGenerator == null) {
                this.depthsupdate$riverGenerator = new UndergroundRiverGenerator(this.world);
            }

            this.depthsupdate$riverGenerator.generate(x, z, primer);
        }
        if (this.depthsupdate$noiseCaveGenerator == null) {
            this.depthsupdate$noiseCaveGenerator = new CaveNoiseGenerator(this.world);
        }

        this.depthsupdate$noiseCaveGenerator.generate(x, z, primer);
    }

}
