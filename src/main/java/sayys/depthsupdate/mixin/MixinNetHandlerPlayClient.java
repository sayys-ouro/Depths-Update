package sayys.depthsupdate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;invalidateBlockReceiveRegion(IIIIII)V"), index = 1)
    private int depthsupdate$modifyInvalidateRegionMinY(int y1) {
        World world = Minecraft.getMinecraft().world;
        return DimensionHelper.isExtendedDimension(world) ? DimensionHelper.EXTENDED_MIN_Y : y1;
    }

    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;invalidateBlockReceiveRegion(IIIIII)V"), index = 4)
    private int depthsupdate$modifyInvalidateRegionMaxY(int y2) {
        World world = Minecraft.getMinecraft().world;
        return DimensionHelper.isExtendedDimension(world) ? DimensionHelper.EXTENDED_MAX_Y : y2;
    }

    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 1)
    private int depthsupdate$modifyMarkRenderMinY(int y1) {
        World world = Minecraft.getMinecraft().world;
        return DimensionHelper.isExtendedDimension(world) ? DimensionHelper.EXTENDED_MIN_Y : y1;
    }

    @ModifyArg(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;markBlockRangeForRenderUpdate(IIIIII)V"), index = 4)
    private int depthsupdate$modifyMarkRenderMaxY(int y2) {
        World world = Minecraft.getMinecraft().world;
        return DimensionHelper.isExtendedDimension(world) ? DimensionHelper.EXTENDED_MAX_Y : y2;
    }
}
