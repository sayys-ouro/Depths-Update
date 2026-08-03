package sayys.depthsupdate.mixin;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenRavine;
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

@Mixin(MapGenRavine.class)
public abstract class MixinMapGenRavine extends MapGenBase {
    @Shadow
    private float[] rs;

    @Unique
    private UndergroundRiverGenerator depthsupdate$river;

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
    protected abstract boolean isExceptionBiome(Biome biome);

    @Shadow
    protected abstract boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ);

    @Shadow
    protected abstract void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ,
            boolean foundTop);

    @Shadow
    protected abstract void addTunnel(long p_180707_1_, int p_180707_3_, int p_180707_4_, ChunkPrimer p_180707_5_, double p_180707_6_, double p_180707_8_, double p_180707_10_, float p_180707_12_, float p_180707_13_, float p_180707_14_, int p_180707_15_, int p_180707_16_, double p_180707_17_);

    @Inject(method = "digBlock", at = @At("HEAD"), cancellable = true)
    protected void depthsupdate$digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, CallbackInfo ci) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        ci.cancel();

        Biome biome = this.world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState state = data.getBlockState(x, y, z);
        IBlockState top = isExceptionBiome(biome) ? Blocks.GRASS.getDefaultState() : biome.topBlock;
        IBlockState filler = isExceptionBiome(biome) ? Blocks.DIRT.getDefaultState()
                : biome.fillerBlock;

        IBlockState deepslate = BlockUtils.getDeepslateBlockState();
        if (state.getBlock() == Blocks.STONE || state.getBlock() == top.getBlock()
                || state.getBlock() == filler.getBlock()
                || state == deepslate || state.getBlock() == deepslate.getBlock()) {
            if (y < HeightManager.getLavaLevel(this.world)) {
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
    protected void depthsupdate$addTunnel(long p_180707_1_, int p_180707_3_, int p_180707_4_, ChunkPrimer p_180707_5_, double p_180707_6_, double p_180707_8_, double p_180707_10_, float p_180707_12_, float p_180707_13_, float p_180707_14_, int p_180707_15_, int p_180707_16_, double p_180707_17_, CallbackInfo ci) {
        if (!HeightManager.isExtended(this.world)) {
            return;
        }

        ci.cancel();

        Random random = new Random(p_180707_1_);
        double d0 = (double) (p_180707_3_ * 16 + 8);
        double d1 = (double) (p_180707_4_ * 16 + 8);
        float f = 0.0F;
        float f1 = 0.0F;

        if (p_180707_16_ <= 0) {
            int i = this.range * 16 - 16;
            p_180707_16_ = i - random.nextInt(i / 4);
        }

        boolean flag1 = false;

        if (p_180707_15_ == -1) {
            p_180707_15_ = p_180707_16_ / 2;
            flag1 = true;
        }

        float f2 = 1.0F;

        HeightContext heightCtx = HeightManager.get(this.world);
        // rs is vanilla's fixed-size float[1024]; without this cap a total world
        // height above 1024 would write past the end of the array.
        int rsSize = Math.min(heightCtx.maxY() - heightCtx.minY(), this.rs.length);
        for (int j = 0; j < rsSize; ++j) {
            if (j == 0 || random.nextInt(3) == 0) {
                f2 = 1.0F + random.nextFloat() * random.nextFloat();
            }

            this.rs[j] = f2 * f2;
        }

        for (; p_180707_15_ < p_180707_16_; ++p_180707_15_) {
            double d9 = 1.5D
                    + (double) (MathHelper.sin((float) p_180707_15_ * (float) Math.PI / (float) p_180707_16_)
                            * p_180707_12_);
            double d2 = d9 * p_180707_17_;
            d9 = d9 * ((double) random.nextFloat() * 0.25D + 0.75D);
            d2 = d2 * ((double) random.nextFloat() * 0.25D + 0.75D);
            float f3 = MathHelper.cos(p_180707_14_);
            float f4 = MathHelper.sin(p_180707_14_);
            p_180707_6_ += (double) (MathHelper.cos(p_180707_13_) * f3);
            p_180707_8_ += (double) f4;
            p_180707_10_ += (double) (MathHelper.sin(p_180707_13_) * f3);
            p_180707_14_ = p_180707_14_ * 0.7F;
            p_180707_14_ = p_180707_14_ + f1 * 0.05F;
            p_180707_13_ += f * 0.05F;
            f1 = f1 * 0.8F;
            f = f * 0.5F;
            f1 = f1 + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f = f + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (flag1 || random.nextInt(4) != 0) {
                double d3 = p_180707_6_ - d0;
                double d4 = p_180707_10_ - d1;
                double d5 = (double) (p_180707_16_ - p_180707_15_);
                double d6 = (double) (p_180707_12_ + 2.0F + 16.0F);

                if (d3 * d3 + d4 * d4 - d5 * d5 > d6 * d6) {
                    return;
                }

                if (p_180707_6_ >= d0 - 16.0D - d9 * 2.0D && p_180707_10_ >= d1 - 16.0D - d9 * 2.0D
                        && p_180707_6_ <= d0 + 16.0D + d9 * 2.0D && p_180707_10_ <= d1 + 16.0D + d9 * 2.0D) {
                    int worldMinY = heightCtx.minY();
                    int worldMaxY = heightCtx.maxY();

                    int k2 = MathHelper.floor(p_180707_6_ - d9) - p_180707_3_ * 16 - 1;
                    int k = MathHelper.floor(p_180707_6_ + d9) - p_180707_3_ * 16 + 1;
                    int l2 = MathHelper.floor(p_180707_8_ - d2) - 1;
                    int l = MathHelper.floor(p_180707_8_ + d2) + 1;
                    int i3 = MathHelper.floor(p_180707_10_ - d9) - p_180707_4_ * 16 - 1;
                    int i1 = MathHelper.floor(p_180707_10_ + d9) - p_180707_4_ * 16 + 1;

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

                    // Full scan rather than vanilla's shell-only one, for the
                    // same reason as MixinMapGenCaves: an underground river is a
                    // mid-depth water slab that the interior-column skip misses.
                    boolean flag2 = false;

                    for (int j1 = k2; !flag2 && j1 < k; ++j1) {
                        for (int k1 = i3; !flag2 && k1 < i1; ++k1) {
                            for (int l1 = l + 1; !flag2 && l1 >= l2 - 1; --l1) {
                                if (l1 >= worldMinY && l1 < worldMaxY
                                        && isOceanBlock(p_180707_5_, j1, l1, k1, p_180707_3_, p_180707_4_)) {
                                    flag2 = true;
                                }
                            }
                        }
                    }

                    if (!flag2) {
                        flag2 = depthsupdate$riverTouches(p_180707_3_, p_180707_4_, k2, k, i3, i1,
                                Math.max(l2 - 1, worldMinY), Math.min(l + 1, worldMaxY - 1));
                    }

                    if (!flag2) {
                        for (int j3 = k2; j3 < k; ++j3) {
                            double d10 = ((double) (j3 + p_180707_3_ * 16) + 0.5D - p_180707_6_) / d9;

                            for (int i2 = i3; i2 < i1; ++i2) {
                                double d7 = ((double) (i2 + p_180707_4_ * 16) + 0.5D - p_180707_10_) / d9;
                                boolean flag = false;

                                if (d10 * d10 + d7 * d7 < 1.0D) {
                                    for (int j2 = l; j2 > l2; --j2) {
                                        double d8 = ((double) (j2 - 1) + 0.5D - p_180707_8_) / d2;

                                        int rsIndex = j2 - worldMinY;
                                        if (rsIndex >= 0 && rsIndex < this.rs.length && (d10 * d10 + d7 * d7) * (double) this.rs[rsIndex] + d8 * d8 / 6.0D < 1.0D) {
                                            if (isTopBlock(p_180707_5_, j3, j2, i2, p_180707_3_, p_180707_4_)) {
                                                flag = true;
                                            }

                                            digBlock(p_180707_5_, j3, j2, i2, p_180707_3_, p_180707_4_, flag);
                                        }
                                    }
                                }
                            }
                        }

                        if (flag1) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "recursiveGenerate", at = @At("HEAD"), cancellable = true)
    protected void depthsupdate$recursiveGenerate(World p_180701_1_, int p_180701_2_, int p_180701_3_,
            int p_180701_4_,
            int p_180701_5_, ChunkPrimer p_180701_6_, CallbackInfo ci) {
        if (!HeightManager.isExtended(p_180701_1_)) {
            return;
        }

        ci.cancel();

        if (this.rand.nextInt(50) == 0) {
            double d0 = (double) (p_180701_2_ * 16 + this.rand.nextInt(16));

            // Vanilla's own start band, kept absolute: modern canyons run at
            // y 10..67 regardless of world depth. The deep band belongs to the
            // noise caves; stretching ravines down there matches neither 1.12
            // nor modern generation.
            double d1 = (double) (this.rand.nextInt(this.rand.nextInt(40) + 8) + 20);

            double d2 = (double) (p_180701_3_ * 16 + this.rand.nextInt(16));

            for (int j = 0; j < 1; ++j) {
                float f = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
                this.addTunnel(this.rand.nextLong(), p_180701_4_, p_180701_5_, p_180701_6_, d0, d1, d2, f2, f, f1, 0,
                        0,
                        3.0D);
            }
        }
    }
}
