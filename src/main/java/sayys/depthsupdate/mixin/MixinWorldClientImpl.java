package sayys.depthsupdate.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(WorldClient.class)
public class MixinWorldClientImpl {
    @ModifyArg(method = "doPreChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 1)
    private int depthsupdate$modifyPreChunkMinY(int y1) {
        World self = (World) (Object) this;
        if (DimensionHelper.isExtendedDimension(self)) {
            return DimensionHelper.EXTENDED_MIN_Y;
        }
        return y1;
    }

    @ModifyArg(method = "doPreChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 4)
    private int depthsupdate$modifyPreChunkMaxY(int y2) {
        World self = (World) (Object) this;
        if (DimensionHelper.isExtendedDimension(self)) {
            return DimensionHelper.EXTENDED_MAX_Y;
        }
        return y2;
    }
}
