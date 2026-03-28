package sayys.depthsupdate.mixin.mod.celeritas;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.world.cloned.ClonedChunkSection;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(value = ClonedChunkSection.class, remap = false)
public class MixinClonedChunkSection {
    @Inject(method = "getChunkSection", at = @At("HEAD"), cancellable = true)
    private static void depthsupdate$fixGetChunkSection(Chunk chunk, int y, CallbackInfoReturnable<ExtendedBlockStorage> cir) {
        var storageArray = chunk.getBlockStorageArray();
        int storageIndex = DimensionHelper.toStorageIndex(true, y << 4);

        if (storageIndex >= 0 && storageIndex < storageArray.length) {
            cir.setReturnValue(storageArray[storageIndex]);
        } else {
            cir.setReturnValue(null);
        }
    }
}
