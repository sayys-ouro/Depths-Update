package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache {
    @Shadow
    protected int chunkX;

    @Shadow
    protected int chunkZ;

    @Shadow
    protected Chunk[][] chunkArray;

    @Shadow
    protected abstract boolean withinBounds(int x, int z);

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getBlockState(BlockPos pos, CallbackInfoReturnable<IBlockState> cir) {
        if (pos.getY() >= -64 && pos.getY() < 0) {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;

            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length) {
                Chunk chunk = this.chunkArray[i][j];
                if (chunk != null) {
                    cir.setReturnValue(chunk.getBlockState(pos));
                    return;
                }
            }
            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }

    @Inject(method = "getLightFor", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getLightFor(EnumSkyBlock type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (pos.getY() >= -64 && pos.getY() < 0) {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;
            if (!this.withinBounds(i, j)) {
                cir.setReturnValue(type.defaultLightValue);
            } else {
                cir.setReturnValue(this.chunkArray[i][j].getLightFor(type, pos));
            }
        }
    }

    @Inject(method = "getLightForExt", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getLightForExt(EnumSkyBlock type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (pos.getY() >= -64 && pos.getY() < 0) {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;
            if (!this.withinBounds(i, j)) {
                cir.setReturnValue(type.defaultLightValue);
            } else {
                cir.setReturnValue(this.chunkArray[i][j].getLightFor(type, pos));
            }
        }
    }

    @Inject(method = "isSideSolid", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$isSideSolid(BlockPos pos, EnumFacing side, boolean _default,
            CallbackInfoReturnable<Boolean> cir) {
        if (pos.getY() >= -64 && pos.getY() < 0) {
            int x = (pos.getX() >> 4) - this.chunkX;
            int z = (pos.getZ() >> 4) - this.chunkZ;
            if (!withinBounds(x, z)) {
                cir.setReturnValue(_default);
            } else {
                IBlockState state = this.chunkArray[x][z].getBlockState(pos);
                cir.setReturnValue(state.getBlock().isSideSolid(state, (ChunkCache) (Object) this, pos, side));
            }
        }
    }
}
