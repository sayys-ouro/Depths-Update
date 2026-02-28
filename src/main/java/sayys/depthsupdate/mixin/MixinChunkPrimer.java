package sayys.depthsupdate.mixin;

import net.minecraft.world.chunk.ChunkPrimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ChunkPrimer.class, priority = 2000)
public abstract class MixinChunkPrimer {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 65536))
    private int depthsupdate$expandDataArrays(int original) {
        return 131072;
    }

    /**
     * @author __sayys
     * @reason Support chunks up to 320 blocks tall including negative Y.
     */
    @Overwrite
    private static int getBlockIndex(int x, int y, int z) {
        return x << 13 | z << 9 | (y + 64);
    }

    // Also need to fix the loop in findGroundBlockIdx which is hardcoded for 0..255
    @ModifyConstant(method = "findGroundBlockIdx", constant = @Constant(intValue = 255))
    private int depthsupdate$modifyFindGroundMaxY(int original) {
        return 255;
    }

    @ModifyConstant(method = "findGroundBlockIdx", constant = @Constant(intValue = 0))
    private int depthsupdate$modifyFindGroundMinY(int original) {
        return -64;
    }

}
