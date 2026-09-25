package sayys.depthsupdate.mixin.client;

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

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = ViewFrustum.class, priority = 1100)
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
    private HeightContext depthsupdate$ctx() {
        World world = Minecraft.getMinecraft().world;
        return HeightManager.get(world);
    }

    @ModifyConstant(method = "setCountChunksXYZ", constant = @Constant(intValue = 16))
    private int depthsupdate$modifyCountChunksY(int original) {
        HeightContext ctx = depthsupdate$ctx();
        return ctx.isExtended() ? ctx.totalStorageSections() : original;
    }

    @ModifyArg(method = "updateChunkPositions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;setPosition(III)V"), index = 1)
    private int depthsupdate$modifyChunkYPosition(int y) {
        HeightContext ctx = depthsupdate$ctx();
        return ctx.isExtended() ? y + ctx.minY() : y;
    }

    @ModifyVariable(method = "markBlocksForUpdate", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int depthsupdate$modifyMinY(int minY) {
        HeightContext ctx = depthsupdate$ctx();
        return ctx.isExtended() ? minY - ctx.minY() : minY;
    }

    @ModifyVariable(method = "markBlocksForUpdate", at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private int depthsupdate$modifyMaxY(int maxY) {
        HeightContext ctx = depthsupdate$ctx();
        return ctx.isExtended() ? maxY - ctx.minY() : maxY;
    }

    @ModifyVariable(method = "getRenderChunk", at = @At("HEAD"), argsOnly = true)
    private BlockPos depthsupdate$modifyPos(@NonNull BlockPos pos) {
        HeightContext ctx = depthsupdate$ctx();
        return ctx.isExtended() ? pos.up(-ctx.minY()) : pos;
    }
}
