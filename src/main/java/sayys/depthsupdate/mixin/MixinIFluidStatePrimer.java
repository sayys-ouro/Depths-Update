package sayys.depthsupdate.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "git.jbredwards.fluidlogged_api.api.world.IFluidStatePrimer", remap = false)
public abstract class MixinIFluidStatePrimer {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 65536))
    private int depthsupdate$expandFluidDataArray(int original) {
        return 131072;
    }
}
