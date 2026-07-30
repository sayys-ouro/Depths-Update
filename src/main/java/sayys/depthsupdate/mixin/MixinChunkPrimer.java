package sayys.depthsupdate.mixin;

import sayys.depthsupdate.core.BedrockFilter;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ChunkPrimer.class, priority = 2000)
public abstract class MixinChunkPrimer {
    private static final IBlockState DEPTHSUPDATE_DEFAULT_STATE = Blocks.AIR.getDefaultState();

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 65536))
    private int depthsupdate$expandDataArrays(int original) {
        return HeightManager.getMaxContext().primerArraySize();
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$filterVanillaBedrock(int x, int y, int z, @NonNull IBlockState state, CallbackInfo ci) {
        if (y >= 0 && y <= 4 && state.getBlock() == Blocks.BEDROCK && BedrockFilter.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "getBlockIndex", at = @At("HEAD"), cancellable = true)
    private static void depthsupdate$getBlockIndex(int x, int y, int z, @NonNull CallbackInfoReturnable<Integer> cir) {
        HeightContext ctx = HeightManager.getMaxContext();
        int yBitShift = ctx.yBitShift();
        cir.setReturnValue((x << (yBitShift + 4)) | (z << yBitShift) | (y - ctx.minY()));
    }

    @Inject(method = "findGroundBlockIdx", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$findGroundBlockIdx(int x, int z, @NonNull CallbackInfoReturnable<Integer> cir) {
        HeightContext ctx = HeightManager.getMaxContext();
        int minY = ctx.minY();
        ChunkPrimer self = (ChunkPrimer) (Object) this;

        for (int y = ctx.maxY() - 1; y >= minY; --y) {
            IBlockState state = self.getBlockState(x, y, z);

            if (state != null && state != DEPTHSUPDATE_DEFAULT_STATE) {
                cir.setReturnValue(y);

                return;
            }
        }

        cir.setReturnValue(minY);
    }
}
