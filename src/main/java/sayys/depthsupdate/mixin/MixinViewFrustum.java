package sayys.depthsupdate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import sayys.depthsupdate.util.DimensionHelper;

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

    @Unique
    private boolean depthsupdate$isExtended() {
        World world = Minecraft.getMinecraft().world;
        return DimensionHelper.isExtendedDimension(world);
    }

    @ModifyConstant(method = "setCountChunksXYZ", constant = @Constant(intValue = 16))
    private int depthsupdate$modifyCountChunksY(int original) {
        return depthsupdate$isExtended() ? DimensionHelper.EXTENDED_STORAGE_SECTIONS : original;
    }

    @ModifyArg(method = "updateChunkPositions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;setPosition(III)V"), index = 1)
    private int depthsupdate$modifyChunkYPosition(int y) {
        return depthsupdate$isExtended() ? y - 64 : y;
    }

    @ModifyVariable(method = "markBlocksForUpdate", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int depthsupdate$modifyMinY(int minY) {
        return depthsupdate$isExtended() ? minY + 64 : minY;
    }

    @ModifyVariable(method = "markBlocksForUpdate", at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private int depthsupdate$modifyMaxY(int maxY) {
        return depthsupdate$isExtended() ? maxY + 64 : maxY;
    }

    @ModifyVariable(method = "getRenderChunk", at = @At("HEAD"), argsOnly = true)
    private BlockPos depthsupdate$modifyPos(@NonNull BlockPos pos) {
        return depthsupdate$isExtended() ? pos.up(64) : pos;
    }
}
