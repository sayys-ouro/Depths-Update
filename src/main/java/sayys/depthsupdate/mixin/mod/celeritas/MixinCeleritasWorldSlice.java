package sayys.depthsupdate.mixin.mod.celeritas;

import org.embeddedt.embeddium.impl.util.position.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.taumc.celeritas.impl.world.WorldSlice;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(value = WorldSlice.class, remap = false)
public class MixinCeleritasWorldSlice {
    @Redirect(method = "prepare", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/util/position/SectionPos;y()I", ordinal = 0))
    private static int depthsupdate$remapSectionYForStorageAccess(SectionPos origin) {
        int sectionY = origin.y();

        return DimensionHelper.toStorageIndex(true, sectionY << 4);
    }
}
