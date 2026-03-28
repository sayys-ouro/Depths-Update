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
    @Unique
    private static final ThreadLocal<Boolean> depthsupdate$isExtended = ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<Integer> depthsupdate$nestingLevel = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "readChunkFromNBT", at = @At("HEAD"))
    private void depthsupdate$startRead(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        if (depthsupdate$nestingLevel.get() == 0) {
            depthsupdate$isExtended.set(DimensionHelper.isExtendedDimension(worldIn));
        }

        depthsupdate$nestingLevel.set(depthsupdate$nestingLevel.get() + 1);
    }

    @Inject(method = "readChunkFromNBT", at = @At("RETURN"))
    private void depthsupdate$endRead(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        depthsupdate$nestingLevel.set(depthsupdate$nestingLevel.get() - 1);

        if (depthsupdate$nestingLevel.get() <= 0) {
            depthsupdate$isExtended.remove();
            depthsupdate$nestingLevel.remove();
        }
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

        if ("Y".equals(key) && depthsupdate$isExtended.get() && (compound.hasKey("Blocks") || compound.hasKey("Palette"))) {
            return (byte) DimensionHelper.toStorageIndex(true, b << 4);
        }

        return b;
    }

    @Contract("_, _ -> new")
    @Redirect(method = "readChunkFromNBT", at = @At(value = "NEW", target = "net/minecraft/world/chunk/storage/ExtendedBlockStorage"))
    private @NonNull ExtendedBlockStorage depthsupdate$fixConstructorY(int y, boolean storeSkylight) {
        if (depthsupdate$isExtended.get()) {
            return new ExtendedBlockStorage(DimensionHelper.fromStorageIndex(true, y >> 4) << 4, storeSkylight);
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
