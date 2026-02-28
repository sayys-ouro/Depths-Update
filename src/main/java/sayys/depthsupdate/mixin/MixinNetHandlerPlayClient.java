package sayys.depthsupdate.mixin;

import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;invalidateBlockReceiveRegion(IIIIII)V"), index = 1)
    private int depthsupdate$modifyInvalidateRegionMinY(int y1) {
        return -64;
    }

    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;invalidateBlockReceiveRegion(IIIIII)V"), index = 4)
    private int depthsupdate$modifyInvalidateRegionMaxY(int y2) {
        return 256;
    }

    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 1)
    private int depthsupdate$modifyMarkRenderMinY(int y1) {
        return -64;
    }

    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 4)
    private int depthsupdate$modifyMarkRenderMaxY(int y2) {
        return 256;
    }
}
