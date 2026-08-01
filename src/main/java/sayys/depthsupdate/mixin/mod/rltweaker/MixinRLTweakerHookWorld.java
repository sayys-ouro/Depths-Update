package sayys.depthsupdate.mixin.mod.rltweaker;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

/**
 * RLTweaker's patchFasterBlockCollision replaces World#getCollisionBoxes with a
 * scan that assumes Y to [0, 255],
 * and it reads sections as getBlockStorageArray()[chunkY] with a raw section
 * index. Below Y=0 the clamp leaves minChunkY above maxChunkY so the loop never
 * runs and nothing collides at all; the raw index would then throw, because
 * HeightContext stores negative sections at the tail of the array.
 *
 * Vanilla-height worlds fall through to RLTweaker untouched.
 */
@Mixin(targets = "com.charles445.rltweaker.hook.HookWorld", remap = false)
public class MixinRLTweakerHookWorld {
    @Inject(method = "getCollisionBoxes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void depthsupdate$extendedCollisionBoxes(World world, Entity entity, AxisAlignedBB aabb,
            boolean stopOnFirst, List<AxisAlignedBB> list, CallbackInfoReturnable<Boolean> cir) {
        if (!HeightManager.isExtended(world)) {
            return;
        }

        if (MathHelper.floor(aabb.minY) - 1 >= 0 && MathHelper.floor(aabb.maxY) + 1 <= 255) {
            return;
        }

        cir.setReturnValue(depthsupdate$collect(world, entity, aabb, stopOnFirst, list));
    }

    private static boolean depthsupdate$collect(World world, Entity entity, AxisAlignedBB aabb,
            boolean stopOnFirst, List<AxisAlignedBB> list) {
        if (stopOnFirst) {
            if (aabb.minX < -30_000_000 || aabb.maxX > 30_000_000 || aabb.minZ < -30_000_000 || aabb.maxZ > 30_000_000) {
                return true;
            }

            MinecraftForge.EVENT_BUS.post(new GetCollisionBoxesEvent(world, entity, aabb, list));

            if (!list.isEmpty()) {
                return true;
            }
        } else if (entity != null && entity.isOutsideBorder() == world.isInsideWorldBorder(entity)) {
            entity.setOutsideBorder(!entity.isOutsideBorder());
        }

        HeightContext ctx = HeightManager.get(world);

        int minX = MathHelper.floor(aabb.minX) - 1;
        int maxX = MathHelper.floor(aabb.maxX) + 1;
        int minZ = MathHelper.floor(aabb.minZ) - 1;
        int maxZ = MathHelper.floor(aabb.maxZ) + 1;
        int minY = Math.max(MathHelper.floor(aabb.minY) - 1, ctx.minY());
        int maxY = Math.min(MathHelper.floor(aabb.maxY) + 1, ctx.maxY() - 1);

        if (minY > maxY) {
            return !list.isEmpty();
        }

        WorldBorder border = world.getWorldBorder();
        boolean checkBorder = !stopOnFirst
                && entity != null
                && !entity.isOutsideBorder()
                && (minX < border.minX() || maxX + 1 > border.maxX() || minZ < border.minZ() || maxZ + 1 > border.maxZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            int x0 = Math.max(minX, chunkX << 4);
            int x1 = Math.min(maxX, (chunkX << 4) | 15);

            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);

                if (chunk == null) {
                    continue;
                }

                int z0 = Math.max(minZ, chunkZ << 4);
                int z1 = Math.min(maxZ, (chunkZ << 4) | 15);
                ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();

                for (int chunkY = minY >> 4; chunkY <= maxY >> 4; chunkY++) {
                    int index = ctx.toStorageIndex(chunkY << 4);

                    if (index < 0 || index >= sections.length) {
                        continue;
                    }

                    ExtendedBlockStorage section = sections[index];

                    if (section == Chunk.NULL_BLOCK_STORAGE) {
                        continue;
                    }

                    int y0 = Math.max(minY, chunkY << 4);
                    int y1 = Math.min(maxY, (chunkY << 4) | 15);

                    for (int x = x0; x <= x1; x++) {
                        boolean xBorder = x == minX || x == maxX;

                        for (int z = z0; z <= z1; z++) {
                            boolean zBorder = z == minZ || z == maxZ;

                            if (xBorder && zBorder) {
                                continue;
                            }

                            for (int y = y0; y <= y1; y++) {
                                if ((xBorder || zBorder) && (y == minY || y == maxY)) {
                                    continue;
                                }

                                pos.setPos(x, y, z);

                                IBlockState state = checkBorder && !border.contains(pos)
                                        ? Blocks.STONE.getDefaultState()
                                        : section.get(x & 15, y & 15, z & 15);

                                state.addCollisionBoxToList(world, pos, aabb, list, entity, false);

                                if (stopOnFirst && !list.isEmpty()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return !list.isEmpty();
    }
}
