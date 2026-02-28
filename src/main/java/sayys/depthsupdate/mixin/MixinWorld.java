package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow
    private int skylightSubtracted;

    @Shadow
    public abstract Chunk getChunk(BlockPos pos);

    @Shadow
    public abstract IBlockState getBlockState(BlockPos pos);

    @Shadow
    public abstract int getLight(BlockPos pos, boolean checkNeighbors);

    @Shadow
    public abstract boolean isValid(BlockPos pos);

    @Shadow
    public abstract boolean isBlockLoaded(BlockPos pos);

    @Shadow
    protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Inject(method = "isAreaLoaded(IIIIIIZ)Z", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$isAreaLoaded(int startX, int startY, int startZ, int endX, int endY, int endZ,
            boolean allowEmpty, CallbackInfoReturnable<Boolean> cir) {
        if (endY >= -64 && startY < 256) {
            int chunkStartX = startX >> 4;
            int chunkStartZ = startZ >> 4;
            int chunkEndX = endX >> 4;
            int chunkEndZ = endZ >> 4;

            for (int i = chunkStartX; i <= chunkEndX; ++i) {
                for (int j = chunkStartZ; j <= chunkEndZ; ++j) {
                    if (!this.isChunkLoaded(i, j, allowEmpty)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isOutsideBuildHeight", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$isOutsideBuildHeight(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(pos.getY() < -64 || pos.getY() >= 256);
    }

    @Inject(method = "getLight(Lnet/minecraft/util/math/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getLightSimple(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (pos.getY() >= -64 && pos.getY() < 0) {
            cir.setReturnValue(this.getChunk(pos).getLightSubtracted(pos, 0));
        }
    }

    @Inject(method = "getLight(Lnet/minecraft/util/math/BlockPos;Z)I", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getLight(BlockPos pos, boolean checkNeighbors, CallbackInfoReturnable<Integer> cir) {
        if (pos.getY() >= -64 && pos.getY() < 0) {
            if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000) {
                if (checkNeighbors && this.getBlockState(pos).useNeighborBrightness()) {
                    int i1 = this.getLight(pos.up(), false);
                    int i = this.getLight(pos.east(), false);
                    int j = this.getLight(pos.west(), false);
                    int k = this.getLight(pos.south(), false);
                    int l = this.getLight(pos.north(), false);

                    if (i > i1)
                        i1 = i;
                    if (j > i1)
                        i1 = j;
                    if (k > i1)
                        i1 = k;
                    if (l > i1)
                        i1 = l;

                    cir.setReturnValue(i1);
                } else {
                    Chunk chunk = this.getChunk(pos);
                    cir.setReturnValue(chunk.getLightSubtracted(pos, this.skylightSubtracted));
                }
            } else {
                cir.setReturnValue(15);
            }
        }
    }

    @Inject(method = "getLightFor", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getLightFor(EnumSkyBlock type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (pos.getY() >= -64 && pos.getY() < 0) {
            if (!this.isValid(pos)) {
                cir.setReturnValue(type.defaultLightValue);
            } else if (!this.isBlockLoaded(pos)) {
                cir.setReturnValue(type.defaultLightValue);
            } else {
                Chunk chunk = this.getChunk(pos);
                cir.setReturnValue(chunk.getLightFor(type, pos));
            }
        }
    }
}
