package sayys.depthsupdate.mixin.mod.fbp;

import com.fbp.compat.CeleritasDynamicLightsHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import static com.fbp.FBP.IS_CELERITASDYNAMICLIGHTS_LOADED;
import static com.fbp.FBP.MC;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

import com.fbp.renderer.light.LightUtil;

@Mixin(value = LightUtil.class, remap = false)
public class MixinLightUtil {

    @Shadow
    private static World world;

    @Shadow
    private static Chunk chunk;

    @Shadow
    private static ExtendedBlockStorage section;

    @Shadow
    public static int pack(int skyLight, int blockLight) {
        return skyLight << 20 | blockLight << 4;
    }

    @Shadow
    public static int block(int combinedLight) {
        return (combinedLight >> 4) & 15;
    }

    @Shadow
    public static int sky(int combinedLight) {
        return combinedLight >> 20;
    }

    /**
     * @author BriefMarigold4
     * @reason Fix for allowing particle lighting outside the 0-255 range
     */
    @Overwrite
    public static int getCombinedLight(int x, int y, int z) {
        World w = MC.player.world;
        HeightContext ctx = HeightManager.get(w);

        if (y < ctx.minY() || y >= ctx.maxY())
            return 0;

        world = w;
        chunk = world.getChunk(x >> 4, z >> 4);
        section = chunk.getBlockStorageArray()[ctx.toStorageIndex(y)];
        int result;
        if (section != null) {
            if (section.get(x & 15, y & 15, z & 15).useNeighborBrightness()) {
                int light = 0;
                if (y > 0)
                    light = getNeighborLight(world, chunk, section, x, y - 1, z, light);
                if (y < 255)
                    light = getNeighborLight(world, chunk, section, x, y + 1, z, light);
                light = getNeighborLight(world, chunk, section, x, y, z - 1, light);
                light = getNeighborLight(world, chunk, section, x, y, z + 1, light);
                light = getNeighborLight(world, chunk, section, x - 1, y, z, light);
                result = getNeighborLight(world, chunk, section, x + 1, y, z, light);
            } else {
                result = pack(world.provider.hasSkyLight() ? section.getSkyLight(x & 15, y & 15, z & 15) : 0,
                    section.getBlockLight(x & 15, y & 15, z & 15));
            }
        } else {
            result = pack(world.provider.hasSkyLight() && y >= chunk.getHeightMap()[(z & 15) << 4 | (x & 15)] ? 15 : 0,
                0);
        }

        if (IS_CELERITASDYNAMICLIGHTS_LOADED) {
            int dynamic = CeleritasDynamicLightsHandler.getDynamicLight(x, y, z);
            result = pack(sky(result), Math.max(block(result), dynamic));
        }

        return result;
    }

    /**
     * @author BriefMarigold4
     * @reason Additional fix for section index
     */
    @Overwrite
    private static int getNeighborLight(World world, Chunk chunk, ExtendedBlockStorage section, int x, int y, int z, int skyBlock) {
        HeightContext ctx = HeightManager.get(world);

        if (x >> 4 != chunk.x || z >> 4 != chunk.z) {
            chunk = world.getChunk(x >> 4, z >> 4);
            section = chunk.getBlockStorageArray()[ctx.toStorageIndex(y)];
        } else if (section == null || y >> 4 != section.getYLocation() >> 4) {
            section = chunk.getBlockStorageArray()[ctx.toStorageIndex(y)];
        }
        return getLight(world, chunk, section, x, y, z, sky(skyBlock), block(skyBlock));
    }

    @Shadow
    private static int getLight(World world, Chunk chunk, ExtendedBlockStorage section, int x, int y, int z, int sky, int block){
        throw new AssertionError();
    }
}
