package sayys.depthsupdate.mixin;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ViewFrustum.class)
public abstract class MixinViewFrustum {
    @Shadow
    protected int countChunksX;

    @Shadow
    protected int countChunksY;

    @Shadow
    protected int countChunksZ;

    @Shadow
    public RenderChunk[] renderChunks;

    @Shadow
    protected abstract int getBaseCoordinate(int p_178157_1_, int p_178157_2_, int p_178157_3_);

    /**
     * @author __sayys
     * @reason Expand RenderChunk vertical limit to 20 for Y=-64
     */
    @Overwrite
    protected void setCountChunksXYZ(int renderDistanceChunks) {
        int i = renderDistanceChunks * 2 + 1;
        this.countChunksX = i;
        this.countChunksY = 20; // 20 chunks = Y: -64 to 256
        this.countChunksZ = i;
    }

    /**
     * @author __sayys
     * @reason map RenderChunk layout below Y=0
     */
    @Overwrite
    public void updateChunkPositions(double viewEntityX, double viewEntityZ) {
        int i = MathHelper.floor(viewEntityX) - 8;
        int j = MathHelper.floor(viewEntityZ) - 8;
        int k = this.countChunksX * 16;

        for (int l = 0; l < this.countChunksX; l++) {
            int i1 = this.getBaseCoordinate(i, k, l);

            for (int j1 = 0; j1 < this.countChunksZ; j1++) {
                int k1 = this.getBaseCoordinate(j, k, j1);

                for (int l1 = 0; l1 < this.countChunksY; l1++) {
                    // Shift down by 4 chunks (64 blocks)
                    int i2 = (l1 - 4) * 16;
                    RenderChunk renderchunk = this.renderChunks[(j1 * this.countChunksY + l1) * this.countChunksX + l];
                    renderchunk.setPosition(i1, i2, k1);
                }
            }
        }
    }

    /**
     * @author __sayys
     * @reason map BlockPos to shifted RenderChunk array indices
     */
    @Overwrite
    public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {
        int i = MathHelper.intFloorDiv(minX, 16);
        int j = MathHelper.intFloorDiv(minY, 16);
        int k = MathHelper.intFloorDiv(minZ, 16);
        int l = MathHelper.intFloorDiv(maxX, 16);
        int i1 = MathHelper.intFloorDiv(maxY, 16);
        int j1 = MathHelper.intFloorDiv(maxZ, 16);

        for (int k1 = i; k1 <= l; k1++) {
            int l1 = k1 % this.countChunksX;

            if (l1 < 0) {
                l1 += this.countChunksX;
            }

            for (int i2 = j; i2 <= i1; i2++) {
                // Shift chunk Y coordinate (+4 offset for -64 mapping)
                int j2 = i2 + 4;

                if (j2 >= 0 && j2 < this.countChunksY) {
                    for (int k2 = k; k2 <= j1; k2++) {
                        int l2 = k2 % this.countChunksZ;

                        if (l2 < 0) {
                            l2 += this.countChunksZ;
                        }

                        int i3 = (l2 * this.countChunksY + j2) * this.countChunksX + l1;
                        RenderChunk renderchunk = this.renderChunks[i3];
                        renderchunk.setNeedsUpdate(updateImmediately);
                    }
                }
            }
        }
    }

    /**
     * @author __sayys
     * @reason map BlockPos to shifted RenderChunk array indices
     */
    @Overwrite
    protected RenderChunk getRenderChunk(@NonNull BlockPos pos) {
        int i = MathHelper.intFloorDiv(pos.getX(), 16);
        int j = MathHelper.intFloorDiv(pos.getY(), 16) + 4; // Shifted Y (+4)
        int k = MathHelper.intFloorDiv(pos.getZ(), 16);

        if (j >= 0 && j < this.countChunksY) {
            i %= this.countChunksX;

            if (i < 0) {
                i += this.countChunksX;
            }

            k %= this.countChunksZ;

            if (k < 0) {
                k += this.countChunksZ;
            }

            int l = (k * this.countChunksY + j) * this.countChunksX + i;

            return this.renderChunks[l];
        } else {
            return null;
        }
    }
}
