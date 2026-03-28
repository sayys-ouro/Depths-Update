package sayys.depthsupdate.mixin.mod.nothirium;

import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = RenderChunk.class, remap = false)
public abstract class MixinNothiriumRenderChunk extends AbstractRenderChunk {
    protected MixinNothiriumRenderChunk(int sectionX, int sectionY, int sectionZ) {
        super(sectionX, sectionY, sectionZ);
    }

    /**
     * @author __sayys
     * @reason Remove the hardcoded 0-15 dirty marking culling.
     */
    @Overwrite
    public void markDirty() {
        if (this.getSectionY() < -4 || this.getSectionY() >= 16) {
            this.getVisibility().setAllVisible();

            return;
        }

        super.markDirty();
    }
}
