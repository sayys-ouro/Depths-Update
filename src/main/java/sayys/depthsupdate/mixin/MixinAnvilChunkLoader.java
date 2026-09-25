package sayys.depthsupdate.mixin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {
    @Unique
    private static final ThreadLocal<HeightContext> depthsupdate$ctx = ThreadLocal.withInitial(() -> HeightContext.VANILLA);

    @Unique
    private static final ThreadLocal<Integer> depthsupdate$nestingLevel = ThreadLocal.withInitial(() -> 0);

    /**
     * The context is refreshed on every entry rather than only at depth zero:
     * a throw inside the body skips the RETURN handler, and a counter left above
     * zero would otherwise pin a stale context for every later read on this thread.
     */
    @Inject(method = "readChunkFromNBT", at = @At("HEAD"))
    private void depthsupdate$startRead(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        depthsupdate$ctx.set(HeightManager.get(worldIn));
        depthsupdate$nestingLevel.set(Math.max(0, depthsupdate$nestingLevel.get()) + 1);
    }

    @Inject(method = "readChunkFromNBT", at = @At("RETURN"))
    private void depthsupdate$endRead(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        int depth = depthsupdate$nestingLevel.get() - 1;

        if (depth <= 0) {
            depthsupdate$ctx.remove();
            depthsupdate$nestingLevel.remove();
        } else {
            depthsupdate$nestingLevel.set(depth);
        }
    }

    @ModifyConstant(method = "readChunkFromNBT", constant = @Constant(intValue = 16))
    private int depthsupdate$modifyStorageArraysSize(int original) {
        HeightContext ctx = depthsupdate$ctx.get();
        if (ctx.isExtended()) {
            return ctx.totalStorageSections();
        }

        return original;
    }

    @Redirect(method = "readChunkFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;getByte(Ljava/lang/String;)B"))
    private byte depthsupdate$offsetY(@NonNull NBTTagCompound compound, String key) {
        byte b = compound.getByte(key);

        HeightContext ctx = depthsupdate$ctx.get();
        if ("Y".equals(key) && ctx.isExtended() && (compound.hasKey("Blocks") || compound.hasKey("Palette"))) {
            return (byte) ctx.toStorageIndex(b << 4);
        }

        return b;
    }

    @Contract("_, _ -> new")
    @Redirect(method = "readChunkFromNBT", at = @At(value = "NEW", target = "net/minecraft/world/chunk/storage/ExtendedBlockStorage"))
    private @NonNull ExtendedBlockStorage depthsupdate$fixConstructorY(int y, boolean storeSkylight) {
        HeightContext ctx = depthsupdate$ctx.get();
        if (ctx.isExtended()) {
            return new ExtendedBlockStorage(ctx.fromStorageIndex(y >> 4) << 4, storeSkylight);
        }

        return new ExtendedBlockStorage(y, storeSkylight);
    }

    @Inject(method = "writeChunkToNBT", at = @At("HEAD"))
    private void depthsupdate$markExtendedChunk(Chunk chunkIn, World worldIn, NBTTagCompound compound, CallbackInfo ci) {
        if (HeightManager.isExtended(worldIn)) {
            compound.setBoolean("DepthsUpdateExtended", true);
        }
    }
}
