package sayys.depthsupdate.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRendererChunkBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import sayys.depthsupdate.client.renderer.debug.DUDebugRendererChunkBorder;
import sayys.depthsupdate.core.HeightManager;

@Mixin(DebugRendererChunkBorder.class)
public abstract class MixinDebugRendererChunkBorder {
    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapMethod(method="render")
    private void depthsupdate$renderChunkBorder(
        float partialTicks, long finishTimeNano, Operation<Void> original
    ) {
        if (HeightManager.isExtended(this.minecraft.world)) {
            DUDebugRendererChunkBorder.render(this.minecraft, partialTicks);
        } else {
            original.call(partialTicks, finishTimeNano);
        }
    }
}
