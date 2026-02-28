package sayys.depthsupdate.mixin;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Shadow
    private int renderDistanceChunks;

    @Shadow
    private ViewFrustum viewFrustum;

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;getPosition()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos depthsupdate$redirectRenderChunkPosForEntityArray(RenderChunk renderChunk) {
        BlockPos pos = renderChunk.getPosition();
        return new BlockPos(pos.getX(), pos.getY() + 64, pos.getZ());
    }

    @Inject(method = "getRenderChunkOffset", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getRenderChunkOffset(BlockPos playerPos, RenderChunk renderChunkBase, EnumFacing facing,
            CallbackInfoReturnable<RenderChunk> cir) {
        BlockPos blockpos = renderChunkBase.getBlockPosOffset16(facing);

        if (net.minecraft.util.math.MathHelper.abs(playerPos.getX() - blockpos.getX()) > this.renderDistanceChunks
                * 16) {
            cir.setReturnValue(null);
        } else if (blockpos.getY() < -64 || blockpos.getY() >= 320) {
            cir.setReturnValue(null);
        } else {
            cir.setReturnValue(net.minecraft.util.math.MathHelper
                    .abs(playerPos.getZ() - blockpos.getZ()) > this.renderDistanceChunks * 16 ? null
                            : ((IMixinViewFrustum) this.viewFrustum).invokeGetRenderChunk(blockpos));
        }
    }
}
