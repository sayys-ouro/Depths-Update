package sayys.depthsupdate.mixin.mod.optifine;

import java.lang.reflect.Field;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sayys.depthsupdate.mixin.IMixinViewFrustum;

@Mixin(ViewFrustum.class)
public abstract class MixinOptiFineViewFrustum {
    @Shadow
    public RenderChunk[] renderChunks;

    @Unique
    private static Field neighboursField;

    @Unique
    private static Field neighboursValidField;

    @Unique
    private static Field offset16Field;

    @Unique
    private static Field neighboursUpdatedField;

    @Unique
    private static Field offset16UpdatedField;

    @Unique
    private static boolean reflectionFailed = false;

    @Unique
    private static void initReflection() {
        if (neighboursField != null || reflectionFailed) return;

        try {
            neighboursField = RenderChunk.class.getDeclaredField("renderChunkNeighbours");
            neighboursField.setAccessible(true);

            neighboursValidField = RenderChunk.class.getDeclaredField("renderChunkNeighboursValid");
            neighboursValidField.setAccessible(true);

            offset16Field = RenderChunk.class.getDeclaredField("renderChunksOfset16");
            offset16Field.setAccessible(true);

            neighboursUpdatedField = RenderChunk.class.getDeclaredField("renderChunkNeighboursUpated");
            neighboursUpdatedField.setAccessible(true);

            offset16UpdatedField = RenderChunk.class.getDeclaredField("renderChunksOffset16Updated");
            offset16UpdatedField.setAccessible(true);

        } catch (NoSuchFieldException e) {
            reflectionFailed = true;
        }
    }

    @Inject(method = "updateChunkPositions", at = @At("TAIL"))
    private void depthsupdate$fixNeighbourLinks(double viewEntityX, double viewEntityZ, CallbackInfo ci) {
        initReflection();

        if (reflectionFailed) return;

        ViewFrustum self = (ViewFrustum) (Object) this;

        for (RenderChunk renderChunk : this.renderChunks) {
            if (renderChunk == null) continue;

            try {
                RenderChunk[] neighbours = (RenderChunk[]) neighboursField.get(renderChunk);
                RenderChunk[] neighboursValid = (RenderChunk[]) neighboursValidField.get(renderChunk);
                RenderChunk[] offset16 = (RenderChunk[]) offset16Field.get(renderChunk);

                for (EnumFacing facing : EnumFacing.VALUES) {
                    BlockPos neighbourPos = renderChunk.getPosition().offset(facing, 16);
                    int y = neighbourPos.getY();

                    RenderChunk neighbour = null;

                    if (y >= -64 && y < 320) {
                        neighbour = ((IMixinViewFrustum) self).invokeGetRenderChunk(neighbourPos);
                    }

                    int idx = facing.getIndex();

                    if (neighbours != null) neighbours[idx] = neighbour;
                    if (neighboursValid != null) neighboursValid[idx] = neighbour;
                    if (offset16 != null) offset16[idx] = neighbour;
                }

                neighboursUpdatedField.setBoolean(renderChunk, true);
                offset16UpdatedField.setBoolean(renderChunk, true);
            } catch (Exception e) {}
        }
    }
}
