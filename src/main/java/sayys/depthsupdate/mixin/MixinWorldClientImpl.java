package sayys.depthsupdate.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldClient.class)
public class MixinWorldClientImpl {
    @ModifyArg(method = "doPreChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 1)
    private int depthsupdate$modifyPreChunkMinY(int y1) {
        return -64;
    }

    @ModifyArg(method = "doPreChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 4)
    private int depthsupdate$modifyPreChunkMaxY(int y2) {
        return 256;
    }
}
