package sayys.depthsupdate.mixin.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
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
public abstract class MixinChunkCacheLighting {
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

    @Shadow
    public abstract int getLightFor(EnumSkyBlock type, BlockPos pos);

    @Inject(method = "getLightFor", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getLightFor(
        EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir
    ) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        int y = pos.getY();
        int minY = HeightManager.getMinY(this.world);
        int maxY = HeightManager.getMaxY(this.world);

        if (y < minY || y >= maxY) {
            cir.setReturnValue(type.defaultLightValue);
        } else if (y < 0 || y >= 256) {
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
    private void depthsupdate$getLightForExt(EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        int y = pos.getY();
        int minY = HeightManager.getMinY(this.world);
        int maxY = HeightManager.getMaxY(this.world);

        if (y < minY || y >= maxY) {
            cir.setReturnValue(type.defaultLightValue);

            return;
        }

        if (y >= 0 && y < 256) {
            return;
        }

        if (this.getBlockState(pos).useNeighborBrightness()) {
            int brightest = 0;

            for (EnumFacing facing : EnumFacing.values()) {
                int light = this.getLightFor(type, pos.offset(facing));

                if (light > brightest) {
                    brightest = light;
                }

                if (brightest >= 15) {
                    break;
                }
            }

            cir.setReturnValue(brightest);

            return;
        }

        int i = (pos.getX() >> 4) - this.chunkX;
        int j = (pos.getZ() >> 4) - this.chunkZ;

        if (!this.withinBounds(i, j)) {
            cir.setReturnValue(type.defaultLightValue);
        } else {
            cir.setReturnValue(this.chunkArray[i][j].getLightFor(type, pos));
        }
    }
}
