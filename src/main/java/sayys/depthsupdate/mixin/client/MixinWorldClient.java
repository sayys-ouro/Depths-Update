package sayys.depthsupdate.mixin.client;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(WorldClient.class)
public class MixinWorldClient {
    @ModifyArg(
        method="doPreChunk",
        at=@At(
            value="INVOKE",
            target="Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"
        ),
        index=1
    )
    private int depthsupdate$modifyPreChunkMinY(int y1) {
        return HeightManager.getMinY((World) (Object) this);
    }

    @ModifyArg(
        method="doPreChunk",
        at=@At(
            value="INVOKE",
            target="Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"
        ),
        index=4
    )
    private int depthsupdate$modifyPreChunkMaxY(int y2) {
        return HeightManager.getMaxY((World) (Object) this);
    }
}
