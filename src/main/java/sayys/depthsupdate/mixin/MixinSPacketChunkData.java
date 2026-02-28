package sayys.depthsupdate.mixin;

import net.minecraft.network.play.server.SPacketChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(SPacketChunkData.class)
public class MixinSPacketChunkData {
    @ModifyConstant(method = "<init>(Lnet/minecraft/world/chunk/Chunk;I)V", constant = @Constant(intValue = 65535))
    private int depthsupdate$modifyFullChunkCheck(int original) {
        return 1048575; // 20 chunks mask
    }
}
