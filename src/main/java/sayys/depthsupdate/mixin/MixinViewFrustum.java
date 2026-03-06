package sayys.depthsupdate.mixin;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ViewFrustum.class)
public abstract class MixinViewFrustum {
    @Shadow
    protected int countChunksX;

    @Shadow
    protected int countChunksY;

    @Shadow
    protected int countChunksZ;

    @Shadow
    public RenderChunk[] renderChunks;

    @Shadow
    protected abstract int getBaseCoordinate(int p_178157_1_, int p_178157_2_, int p_178157_3_);

    @ModifyConstant(method = "setCountChunksXYZ", constant = @Constant(intValue = 16))
    private int depthsupdate$modifyCountChunksY(int original) {
        return 20; // 20 chunks = Y: -64 to 256
    }

    @ModifyArg(method = "updateChunkPositions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;setPosition(III)V"), index = 1)
    private int depthsupdate$modifyChunkYPosition(int y) {
        return y - 64; // Shift down by 4 chunks (64 blocks)
    }

    @ModifyVariable(method = "markBlocksForUpdate", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int depthsupdate$modifyMinY(int minY) {
        return minY + 64;
    }

    @ModifyVariable(method = "markBlocksForUpdate", at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private int depthsupdate$modifyMaxY(int maxY) {
        return maxY + 64;
    }

    @ModifyVariable(method = "getRenderChunk", at = @At("HEAD"), argsOnly = true)
    private BlockPos depthsupdate$modifyPos(@NonNull BlockPos pos) {
        return pos.up(64); // Shifted Y (+4 chunks)
    }
}
