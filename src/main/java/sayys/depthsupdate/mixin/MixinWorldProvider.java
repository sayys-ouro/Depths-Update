package sayys.depthsupdate.mixin;

import net.minecraft.world.WorldProvider;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider {
    @Inject(method = "getActualHeight", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getActualHeight(@NonNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(320);
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getHeight(@NonNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(320);
    }
}
