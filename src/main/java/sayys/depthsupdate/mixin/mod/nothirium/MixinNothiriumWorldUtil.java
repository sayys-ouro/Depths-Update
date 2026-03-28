package sayys.depthsupdate.mixin.mod.nothirium;

import meldexun.nothirium.mc.util.WorldUtil;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(value = WorldUtil.class, remap = false)
public class MixinNothiriumWorldUtil {
    /**
     * @author __sayys
     * @reason Support negative section coordinates.
     */
    @Overwrite
    public static ExtendedBlockStorage getSection(World world, int sectionX, int sectionY, int sectionZ) {
        if (sectionY < -4 || sectionY >= 16) {
            return null;
        }

        Chunk chunk = world.getChunk(sectionX, sectionZ);

        if (chunk == null) {
            return null;
        }

        int storageIndex = DimensionHelper.toStorageIndex(DimensionHelper.isExtendedDimension(world), sectionY << 4);

        ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();

        if (storageIndex < 0 || storageIndex >= storageArray.length) {
            return null;
        }

        return storageArray[storageIndex];
    }
}
