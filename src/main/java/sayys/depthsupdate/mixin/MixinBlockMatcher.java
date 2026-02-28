package sayys.depthsupdate.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.block.BlockDeepslate;

@Mixin(BlockMatcher.class)
public class MixinBlockMatcher {
    @Shadow
    @Final
    private Block block;

    @Inject(method = "apply(Lnet/minecraft/block/state/IBlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$matchDeepslate(IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (this.block == Blocks.STONE && state != null && state.getBlock() instanceof BlockDeepslate) {
            cir.setReturnValue(true);
        }
    }
}
