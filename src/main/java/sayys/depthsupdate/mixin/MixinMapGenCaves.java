package sayys.depthsupdate.mixin;

import com.google.common.base.MoreObjects;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

@Mixin(MapGenCaves.class)
public abstract class MixinMapGenCaves extends MapGenBase {
    @Unique
    private UndergroundRiverGenerator depthsupdate$river;

    /**
     * The primer scan above only sees this chunk, and only the dig box
     * itself. The river is pure noise, so it can be asked about the one-block
     * margin and about columns across the chunk border.
     */
    @Unique
    private boolean depthsupdate$riverTouches(int chunkX, int chunkZ, int xMin, int xMax, int zMin, int zMax, int yLow, int yHigh) {
        if (!DepthsUpdateConfig.generateUndergroundRivers) {
            return false;
        }

        if (this.depthsupdate$river == null) {
            this.depthsupdate$river = new UndergroundRiverGenerator(this.world);
        }

        for (int bx = xMin - 1; bx <= xMax; ++bx) {
            for (int bz = zMin - 1; bz <= zMax; ++bz) {
                if (this.depthsupdate$river.waterWithin(chunkX * 16 + bx, chunkZ * 16 + bz, yLow, yHigh)) {
                    return true;
                }
            }
        }

        return false;
    }
    @Shadow
    protected abstract boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ);

    @Shadow
    protected abstract boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ);

    @Shadow
    protected abstract boolean canReplaceBlock(IBlockState p_175793_1_, IBlockState p_175793_2_);

    @Shadow
    protected abstract void addRoom(long p_180703_1_, int p_180703_3_, int p_180703_4_, ChunkPrimer p_180703_5_,
            double p_180703_6_, double p_180703_8_, double p_180703_10_);

    @Inject(method = "digBlock", at = @At("HEAD"), cancellable = true)
    protected void depthsupdate$digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, IBlockState state, IBlockState up, CallbackInfo ci) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        ci.cancel();

        Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState top = biome.topBlock;
        IBlockState filler = biome.fillerBlock;

        IBlockState deepslate = BlockUtils.getDeepslateBlockState();

        if (this.canReplaceBlock(state, up) || state.getBlock() == top.getBlock()
                || state.getBlock() == filler.getBlock()
                || state == deepslate || state.getBlock() == deepslate.getBlock()) {
            if (y < HeightManager.getLavaLevel(world)) {
                data.setBlockState(x, y, z, Blocks.LAVA.getDefaultState());
            } else {
                data.setBlockState(x, y, z, Blocks.AIR.getDefaultState());

                if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
                    data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
                }
            }
        }
    }

    @Inject(method = "addTunnel", at = @At("HEAD"), cancellable = true)
    protected void depthsupdate$addTunnel(long p_180702_1_, int p_180702_3_, int p_180702_4_, ChunkPrimer p_180702_5_, double p_180702_6_, double p_180702_8_, double p_180702_10_, float p_180702_12_, float p_180702_13_, float p_180702_14_, int p_180702_15_, int p_180702_16_, double p_180702_17_, CallbackInfo ci) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        ci.cancel();

        double d0 = (double) (p_180702_3_ * 16 + 8);
        double d1 = (double) (p_180702_4_ * 16 + 8);
        float f = 0.0F;
        float f1 = 0.0F;
        Random random = new Random(p_180702_1_);

        if (p_180702_16_ <= 0) {
            int i = this.range * 16 - 16;
            p_180702_16_ = i - random.nextInt(i / 4);
        }

        boolean flag2 = false;

        if (p_180702_15_ == -1) {
            p_180702_15_ = p_180702_16_ / 2;
            flag2 = true;
        }

        int j = random.nextInt(p_180702_16_ / 2) + p_180702_16_ / 4;

        for (boolean flag = random.nextInt(6) == 0; p_180702_15_ < p_180702_16_; ++p_180702_15_) {
            double d2 = 1.5D
                    + (double) (MathHelper.sin((float) p_180702_15_ * (float) Math.PI / (float) p_180702_16_)
                            * p_180702_12_);
            double d3 = d2 * p_180702_17_;
            float f2 = MathHelper.cos(p_180702_14_);
            float f3 = MathHelper.sin(p_180702_14_);
            p_180702_6_ += (double) (MathHelper.cos(p_180702_13_) * f2);
            p_180702_8_ += (double) f3;
            p_180702_10_ += (double) (MathHelper.sin(p_180702_13_) * f2);

            if (flag) {
                p_180702_14_ = p_180702_14_ * 0.92F;
            } else {
                p_180702_14_ = p_180702_14_ * 0.7F;
            }

            p_180702_14_ = p_180702_14_ + f1 * 0.1F;
            p_180702_13_ += f * 0.1F;
            f1 = f1 * 0.9F;
            f = f * 0.75F;
            f1 = f1 + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f = f + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!flag2 && p_180702_15_ == j && p_180702_12_ > 1.0F && p_180702_16_ > 0) {
                this.addTunnel(random.nextLong(), p_180702_3_, p_180702_4_, p_180702_5_, p_180702_6_, p_180702_8_,
                        p_180702_10_, random.nextFloat() * 0.5F + 0.5F, p_180702_13_ - ((float) Math.PI / 2F),
                        p_180702_14_ / 3.0F, p_180702_15_, p_180702_16_, 1.0D);
                this.addTunnel(random.nextLong(), p_180702_3_, p_180702_4_, p_180702_5_, p_180702_6_, p_180702_8_,
                        p_180702_10_, random.nextFloat() * 0.5F + 0.5F, p_180702_13_ + ((float) Math.PI / 2F),
                        p_180702_14_ / 3.0F, p_180702_15_, p_180702_16_, 1.0D);
                return;
            }

            if (flag2 || random.nextInt(4) != 0) {
                double d4 = p_180702_6_ - d0;
                double d5 = p_180702_10_ - d1;
                double d6 = (double) (p_180702_16_ - p_180702_15_);
                double d7 = (double) (p_180702_12_ + 2.0F + 16.0F);

                if (d4 * d4 + d5 * d5 - d6 * d6 > d7 * d7) {
                    return;
                }

                if (p_180702_6_ >= d0 - 16.0D - d2 * 2.0D && p_180702_10_ >= d1 - 16.0D - d2 * 2.0D
                        && p_180702_6_ <= d0 + 16.0D + d2 * 2.0D && p_180702_10_ <= d1 + 16.0D + d2 * 2.0D) {
                    HeightContext heightCtx = HeightManager.get(this.world);
                    int worldMinY = heightCtx.minY();
                    int worldMaxY = heightCtx.maxY();

                    int k2 = MathHelper.floor(p_180702_6_ - d2) - p_180702_3_ * 16 - 1;
                    int k = MathHelper.floor(p_180702_6_ + d2) - p_180702_3_ * 16 + 1;
                    int l2 = MathHelper.floor(p_180702_8_ - d3) - 1;
                    int l = MathHelper.floor(p_180702_8_ + d3) + 1;
                    int i3 = MathHelper.floor(p_180702_10_ - d2) - p_180702_4_ * 16 - 1;
                    int i1 = MathHelper.floor(p_180702_10_ + d2) - p_180702_4_ * 16 + 1;

                    if (k2 < 0) {
                        k2 = 0;
                    }

                    if (k > 16) {
                        k = 16;
                    }

                    if (l2 < worldMinY + 1) {
                        l2 = worldMinY + 1;
                    }

                    if (l > worldMaxY - 5) {
                        l = worldMaxY - 5;
                    }

                    if (i3 < 0) {
                        i3 = 0;
                    }

                    if (i1 > 16) {
                        i1 = 16;
                    }

                    // Vanilla scans only the shell of this box: interior columns
                    // skip from the top straight to the bottom. That is enough
                    // when water only sits at the surface, but underground
                    // rivers are a mid-depth slab the skip jumps clean over, so
                    // the tunnel cuts an open face into the water. Scan in full.
                    boolean flag3 = false;

                    for (int j1 = k2; !flag3 && j1 < k; ++j1) {
                        for (int k1 = i3; !flag3 && k1 < i1; ++k1) {
                            for (int l1 = l + 1; !flag3 && l1 >= l2 - 1; --l1) {
                                if (l1 >= worldMinY && l1 < worldMaxY
                                        && isOceanBlock(p_180702_5_, j1, l1, k1, p_180702_3_, p_180702_4_)) {
                                    flag3 = true;
                                }
                            }
                        }
                    }

                    if (!flag3) {
                        flag3 = depthsupdate$riverTouches(p_180702_3_, p_180702_4_, k2, k, i3, i1,
                                Math.max(l2 - 1, worldMinY), Math.min(l + 1, worldMaxY - 1));
                    }

                    if (!flag3) {
                        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                        for (int j3 = k2; j3 < k; ++j3) {
                            double d10 = ((double) (j3 + p_180702_3_ * 16) + 0.5D - p_180702_6_) / d2;

                            for (int i2 = i3; i2 < i1; ++i2) {
                                double d8 = ((double) (i2 + p_180702_4_ * 16) + 0.5D - p_180702_10_) / d2;
                                boolean flag1 = false;

                                if (d10 * d10 + d8 * d8 < 1.0D) {
                                    for (int j2 = l; j2 > l2; --j2) {
                                        double d9 = ((double) (j2 - 1) + 0.5D - p_180702_8_) / d3;

                                        if (d9 > -0.7D && d10 * d10 + d9 * d9 + d8 * d8 < 1.0D) {
                                            IBlockState iblockstate1 = p_180702_5_.getBlockState(j3, j2, i2);
                                            IBlockState iblockstate2 = (IBlockState) MoreObjects.firstNonNull(
                                                    p_180702_5_.getBlockState(j3, j2 + 1, i2),
                                                    Blocks.AIR.getDefaultState());

                                            if (isTopBlock(p_180702_5_, j3, j2, i2, p_180702_3_, p_180702_4_)) {
                                                flag1 = true;
                                            }

                                            digBlock(p_180702_5_, j3, j2, i2, p_180702_3_, p_180702_4_, flag1,
                                                    iblockstate1, iblockstate2);
                                        }
                                    }
                                }
                            }
                        }

                        if (flag2) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Shadow
    protected abstract void addTunnel(long p_180702_1_, int p_180702_3_, int p_180702_4_, ChunkPrimer p_180702_5_, double p_180702_6_, double p_180702_8_, double p_180702_10_, float p_180702_12_, float p_180702_13_, float p_180702_14_, int p_180702_15_, int p_180702_16_, double p_180702_17_);

    @Shadow
    protected abstract void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, IBlockState state, IBlockState up);

    @Inject(method = "recursiveGenerate", at = @At("HEAD"), cancellable = true)
    protected void depthsupdate$recursiveGenerate(World p_180701_1_, int p_180701_2_, int p_180701_3_, int p_180701_4_, int p_180701_5_, ChunkPrimer p_180701_6_, CallbackInfo ci) {
        if (!HeightManager.isExtended(p_180701_1_)) {
            return;
        }

        ci.cancel();
        HeightContext heightCtx = HeightManager.get(p_180701_1_);
        int minY = heightCtx.minY();
        int totalHeight = heightCtx.totalHeight();

        int i = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);

        if (this.rand.nextInt(7) != 0) {
            i = 0;
        }

        // Modern vanilla's cave carver runs from 8 above the bottom to y=180.
        int carverMinY = minY + 8;
        int carverMaxY = Math.min(180, minY + totalHeight - 8);
        int yRange = Math.max(8, carverMaxY - carverMinY - 8);

        for (int j = 0; j < i; ++j) {
            double d0 = (double) (p_180701_2_ * 16 + this.rand.nextInt(16));

            double vanillaLikeY = this.rand.nextInt(yRange) + 8;
            double d1 = (double) (this.rand.nextInt((int) vanillaLikeY) + carverMinY);

            double d2 = (double) (p_180701_3_ * 16 + this.rand.nextInt(16));
            int k = 1;

            if (this.rand.nextInt(4) == 0) {
                this.addRoom(this.rand.nextLong(), p_180701_4_, p_180701_5_, p_180701_6_, d0, d1, d2);
                k += this.rand.nextInt(4);
            }

            for (int l = 0; l < k; ++l) {
                float f = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();

                if (this.rand.nextInt(10) == 0) {
                    f2 *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
                }

                // Length 0 lets addTunnel derive vanilla's range*16-16. Anything
                // longer than that cannot generate fully: a tunnel is only
                // carved by chunks whose origin scan reaches it, and that scan
                // stops at MapGenBase.range chunks.
                this.addTunnel(this.rand.nextLong(), p_180701_4_, p_180701_5_, p_180701_6_, d0, d1, d2, f2, f, f1, 0,
                        0, 1.0D);
            }
        }
    }
}
