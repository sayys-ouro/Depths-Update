package sayys.depthsupdate.mixin;

import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public abstract class MixinBiome {
    @Redirect(method = "generateBiomeTerrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkPrimer;setBlockState(IIILnet/minecraft/block/state/IBlockState;)V"))
    private void depthsupdate$filterBedrock(ChunkPrimer primer, int x, int y, int z, IBlockState state) {
        if (state.getBlock() == Blocks.BEDROCK && y >= 0) {
            return;
        }
        primer.setBlockState(x, y, z, state);
    }

}
