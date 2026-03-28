package sayys.depthsupdate.mixin.mod.celeritas;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(value = CeleritasWorldRenderer.class, remap = false)
public class MixinCeleritasWorldRenderer {
    @Inject(method = "getMinimumBuildHeight", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$fixMinBuildHeight(CallbackInfoReturnable<Integer> cir) {
        if (DimensionHelper.isExtendedDimension(Minecraft.getMinecraft().world)) {
            cir.setReturnValue(DimensionHelper.EXTENDED_MIN_Y);
        }
    }
}
