package sayys.depthsupdate.mixin;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Vanilla sets useNeighborBrightness in Block.registerBlocks, which only walks
 * its own registry, so blocks registered through the Forge events never get it.
 */
@Mixin(Block.class)
public interface IMixinBlock {
    @Accessor("useNeighborBrightness")
    void depthsupdate$setUseNeighborBrightness(boolean value);
}
