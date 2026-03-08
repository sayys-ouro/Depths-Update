package sayys.depthsupdate.mixin.mod.optifine;

import java.lang.reflect.Field;
import net.minecraft.client.renderer.chunk.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderChunk.class)
public class MixinOptiFineRenderChunk {
    @Unique
    private static Field neighboursUpdatedField;

    @Unique
    private static Field offset16UpdatedField;

    @Unique
    private static boolean reflectionInitialized = false;

    @Unique
    private static boolean reflectionFailed = false;

    @Unique
    private static void initReflection() {
        if (reflectionInitialized) return;

        reflectionInitialized = true;

        try {
            neighboursUpdatedField = RenderChunk.class.getDeclaredField("renderChunkNeighboursUpated");
            neighboursUpdatedField.setAccessible(true);

            offset16UpdatedField = RenderChunk.class.getDeclaredField("renderChunksOffset16Updated");
            offset16UpdatedField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            reflectionFailed = true;
        }
    }

    @Inject(method = "setPosition", at = @At("TAIL"))
    private void depthsupdate$resetNeighbourFlags(int x, int y, int z, CallbackInfo ci) {
        if (reflectionFailed) return;

        initReflection();

        if (reflectionFailed) return;

        try {
            neighboursUpdatedField.setBoolean(this, false);
            offset16UpdatedField.setBoolean(this, false);
        } catch (Exception e) {}
    }
}
