package sayys.depthsupdate.mixin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {
    /**
     * ThreadLocal flag set to true when reading a chunk in an extended dimension.
     * This is needed because @Redirect cannot access the World parameter directly.
     */
    @Unique
    private static final ThreadLocal<Boolean> depthsupdate$isExtended = ThreadLocal.withInitial(() -> false);

    /**
     * Capture the World parameter at the start of readChunkFromNBT to determine
     * if we're loading a chunk in an extended dimension.
     */
    @Inject(method = "readChunkFromNBT", at = @At("HEAD"))
    private void depthsupdate$captureWorld(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        depthsupdate$isExtended.set(DimensionHelper.isExtendedDimension(worldIn));
    }

    /**
     * Clean up the ThreadLocal after readChunkFromNBT completes (success or failure).
     */
    @Inject(method = "readChunkFromNBT", at = @At("RETURN"))
    private void depthsupdate$cleanupWorld(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        depthsupdate$isExtended.remove();
    }

    @ModifyConstant(method = "readChunkFromNBT", constant = @Constant(intValue = 16))
    private int depthsupdate$modifyStorageArraysSize(int original) {
        if (depthsupdate$isExtended.get()) {
            return DimensionHelper.EXTENDED_STORAGE_SECTIONS;
        }

        return original;
    }

    @Redirect(method = "readChunkFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;getByte(Ljava/lang/String;)B"))
    private byte depthsupdate$offsetY(@NonNull NBTTagCompound compound, String key) {
        byte b = compound.getByte(key);

        if ("Y".equals(key) && depthsupdate$isExtended.get()) {
            return (byte) (b + DimensionHelper.SECTION_OFFSET);
        }

        return b;
    }

    @Contract("_, _ -> new")
    @Redirect(method = "readChunkFromNBT", at = @At(value = "NEW", target = "net/minecraft/world/chunk/storage/ExtendedBlockStorage"))
    private @NonNull ExtendedBlockStorage depthsupdate$fixConstructorY(int y, boolean storeSkylight) {
        if (depthsupdate$isExtended.get()) {
            return new ExtendedBlockStorage(y - 64, storeSkylight);
        }

        return new ExtendedBlockStorage(y, storeSkylight);
    }

    /**
     * When saving chunks in an extended dimension, add a marker tag so we know
     * the Y values are already offset when reloading.
     */
    @Inject(method = "writeChunkToNBT", at = @At("HEAD"))
    private void depthsupdate$markExtendedChunk(Chunk chunkIn, World worldIn, NBTTagCompound compound, CallbackInfo ci) {
        if (DimensionHelper.isExtendedDimension(worldIn)) {
            compound.setBoolean("DepthsUpdateExtended", true);
        }
    }
}
