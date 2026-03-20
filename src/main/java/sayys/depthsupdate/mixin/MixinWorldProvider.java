package sayys.depthsupdate.mixin;

import net.minecraft.world.WorldProvider;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider {
    @Inject(method = "getActualHeight", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getActualHeight(@NonNull CallbackInfoReturnable<Integer> cir) {
        WorldProvider self = (WorldProvider) (Object) this;
        if (self.getDimension() == DimensionHelper.OVERWORLD_DIM) {
            cir.setReturnValue(DimensionHelper.EXTENDED_TOTAL_HEIGHT);
        }
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getHeight(@NonNull CallbackInfoReturnable<Integer> cir) {
        WorldProvider self = (WorldProvider) (Object) this;
        if (self.getDimension() == DimensionHelper.OVERWORLD_DIM) {
            cir.setReturnValue(DimensionHelper.EXTENDED_TOTAL_HEIGHT);
        }
    }
}
