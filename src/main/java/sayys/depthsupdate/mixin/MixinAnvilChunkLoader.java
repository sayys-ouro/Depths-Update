package sayys.depthsupdate.mixin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {
    @ModifyConstant(method = "readChunkFromNBT", constant = @Constant(intValue = 16))
    private int depthsupdate$modifyStorageArraysSize(int original) {
        return 20;
    }

    @Redirect(method = "readChunkFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;getByte(Ljava/lang/String;)B"))
    private byte depthsupdate$offsetY(@NonNull NBTTagCompound compound, String key) {
        byte b = compound.getByte(key);

        if ("Y".equals(key)) {
            return (byte) (b + 4);
        }

        return b;
    }

    @Contract("_, _ -> new")
    @Redirect(method = "readChunkFromNBT", at = @At(value = "NEW", target = "net/minecraft/world/chunk/storage/ExtendedBlockStorage"))
    private @NonNull ExtendedBlockStorage depthsupdate$fixConstructorY(int y, boolean storeSkylight) {
        return new ExtendedBlockStorage(y - 64, storeSkylight);
    }
}
