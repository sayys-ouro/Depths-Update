package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sayys.depthsupdate.core.HeightManager;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache {
    @Shadow
    protected int chunkX;

    @Shadow
    protected int chunkZ;

    @Shadow
    protected Chunk[][] chunkArray;

    @Shadow
    protected World world;

    @Shadow
    protected abstract boolean withinBounds(int x, int z);

    @Shadow
    public abstract IBlockState getBlockState(BlockPos pos);

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getBlockState(@NonNull BlockPos pos, CallbackInfoReturnable<IBlockState> cir) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        int y = pos.getY();
        int minY = HeightManager.getMinY(this.world);
        int maxY = HeightManager.getMaxY(this.world);

        if (y >= minY && y < maxY) {
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
        } else {
            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }

    @Inject(method = "isSideSolid", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$isSideSolid(@NonNull BlockPos pos, EnumFacing side, boolean _default, CallbackInfoReturnable<Boolean> cir) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        int y = pos.getY();
        int minY = HeightManager.getMinY(this.world);
        int maxY = HeightManager.getMaxY(this.world);

        if (y < minY || y >= maxY) {
            cir.setReturnValue(_default);
        } else if (y < 0 || y >= 256) {
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
