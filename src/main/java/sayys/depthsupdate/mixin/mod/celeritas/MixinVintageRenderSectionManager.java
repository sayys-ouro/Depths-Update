package sayys.depthsupdate.mixin.mod.celeritas;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.chunk.Chunk;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.VintageRenderPassConfigurationBuilder;
import org.taumc.celeritas.impl.render.terrain.VintageRenderSectionManager;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = VintageRenderSectionManager.class, remap = false)
public class MixinVintageRenderSectionManager {
    @Shadow
    private WorldClient world;

    /**
     * @author __sayys
     * @reason Pass extended minSection for negative depth.
     */
    @Overwrite
    public static VintageRenderSectionManager create(ChunkVertexType vertexType, WorldClient world, int renderDistance, CommandList commandList) {
        HeightContext ctx = HeightManager.get(world);
        int minSection = ctx.isExtended() ? ctx.minSection() : 0;
        int maxSection = ctx.isExtended() ? ctx.maxSection() + 1 : 16;
        return new VintageRenderSectionManager(VintageRenderPassConfigurationBuilder.build(vertexType), world, renderDistance, commandList, minSection, maxSection);
    }

    @Inject(method = "isSectionVisuallyEmpty", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$fixIsSectionVisuallyEmpty(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        Chunk chunk = this.world.getChunk(x, z);

        if (chunk.isEmpty()) {
            cir.setReturnValue(true);

            return;
        }

        HeightContext ctx = HeightManager.get(this.world);
        var array = chunk.getBlockStorageArray();
        int storageIndex = ctx.toStorageIndex(y << 4);

        if (storageIndex < 0 || storageIndex >= array.length) {
            cir.setReturnValue(true);

            return;
        }

        cir.setReturnValue(array[storageIndex] == Chunk.NULL_BLOCK_STORAGE || array[storageIndex].isEmpty());
    }
}
