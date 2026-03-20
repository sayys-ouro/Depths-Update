package sayys.depthsupdate.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(World.class)
public abstract class MixinWorldClient {
    @Shadow
    public abstract boolean isValid(BlockPos pos);

    @Shadow
    public abstract boolean isBlockLoaded(BlockPos pos);

    @Shadow
    public abstract int getLightFor(EnumSkyBlock type, BlockPos pos);

    @Inject(method = "getLightFromNeighborsFor", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getLightFromNeighborsFor(EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        World world = (World) (Object) this;
        if (!DimensionHelper.isExtendedDimension(world)) {
            return;
        }

        if (pos.getY() >= DimensionHelper.EXTENDED_MIN_Y && pos.getY() < 0) {
            if (!world.provider.hasSkyLight() && type == EnumSkyBlock.SKY) {
                cir.setReturnValue(0);
            } else {
                if (!this.isValid(pos)) {
                    cir.setReturnValue(type.defaultLightValue);
                } else if (!this.isBlockLoaded(pos)) {
                    cir.setReturnValue(type.defaultLightValue);
                } else if (world.getBlockState(pos).useNeighborBrightness()) {
                    int i1 = this.getLightFor(type, pos.up());
                    int i = this.getLightFor(type, pos.east());
                    int j = this.getLightFor(type, pos.west());
                    int k = this.getLightFor(type, pos.south());
                    int l = this.getLightFor(type, pos.north());

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
                    cir.setReturnValue(world.getChunk(pos).getLightFor(type, pos));
                }
            }
        }
    }
}
