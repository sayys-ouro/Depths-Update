package sayys.depthsupdate.mixin;

import com.google.common.base.Predicate;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import sayys.depthsupdate.util.DimensionHelper;

@Mixin(Chunk.class)
public abstract class MixinChunk {
    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private ExtendedBlockStorage[] storageArrays;

    @Shadow
    @Final
    private int[] precipitationHeightMap;

    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    @Shadow
    @Final
    private int[] heightMap;

    @Shadow
    private boolean dirty;

    @Shadow
    private int heightMapMinimum;

    @Shadow
    private boolean hasEntities;

    @Shadow
    @Final
    private ClassInheritanceMultiMap<Entity>[] entityLists;

    @Shadow
    public abstract int getTopFilledSegment();

    @Shadow
    protected abstract int getBlockLightOpacity(int x, int y, int z);

    @Shadow
    @Nullable
    public abstract TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType creationMode);

    @Shadow
    private void propagateSkylightOcclusion(int x, int z) {}

    @Shadow
    private void relightBlock(int x, int y, int z) {}

    @Shadow
    public abstract void generateSkylightMap();

    @Shadow
    private void updateSkylightNeighborHeight(int p_76609_1_, int p_76609_2_, int p_76609_3_, int p_76609_4_) {}

    @Shadow
    public abstract IBlockState getBlockState(int x, int y, int z);

    @Shadow
    public abstract int getLightFor(EnumSkyBlock type, @NonNull BlockPos pos);

    @Unique
    private static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;

    /**
     * ThreadLocal to pass dimension info into @ModifyConstant during construction.
     * Required because @Inject at HEAD of &lt;init&gt; must be static (pre-super).
     */
    @Unique
    private static final ThreadLocal<Boolean> depthsupdate$extendedInit = ThreadLocal.withInitial(() -> false);

    @Unique
    private boolean depthsupdate$isExtended() {
        return DimensionHelper.isExtendedDimension(this.world);
    }

    @Unique
    private int depthsupdate$sectionOffset() {
        return depthsupdate$isExtended() ? DimensionHelper.SECTION_OFFSET : 0;
    }

    @Unique
    private int depthsupdate$minY() {
        return depthsupdate$isExtended() ? DimensionHelper.EXTENDED_MIN_Y : DimensionHelper.VANILLA_MIN_Y;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("HEAD"))
    private static void depthsupdate$captureWorldForInit(World worldIn, int x, int z, CallbackInfo ci) {
        depthsupdate$extendedInit.set(DimensionHelper.isExtendedDimension(worldIn));
    }

    @ModifyConstant(method = "<init>(Lnet/minecraft/world/World;II)V", constant = @Constant(intValue = 16))
    private int depthsupdate$modifyStorageArraysSize(int original) {
        return depthsupdate$extendedInit.get() ? DimensionHelper.EXTENDED_STORAGE_SECTIONS : original;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"))
    private void depthsupdate$onChunkInitDefault(World worldIn, int x, int z, CallbackInfo ci) {
        depthsupdate$extendedInit.remove();
        if (DimensionHelper.isExtendedDimension(worldIn)) {
            for (int i = 0; i < this.heightMap.length; ++i) {
                this.heightMap[i] = DimensionHelper.EXTENDED_MIN_Y;
            }
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ChunkPrimer;II)V", at = @At("RETURN"))
    private void depthsupdate$onChunkInitPrimer(World worldIn, ChunkPrimer primer, int x, int z, CallbackInfo ci) {
        if (!DimensionHelper.isExtendedDimension(worldIn)) {
            return;
        }

        for (int i = 0; i < this.heightMap.length; ++i) {
            this.heightMap[i] = DimensionHelper.EXTENDED_MIN_Y;
        }

        boolean flag = worldIn.provider.hasSkyLight();

        // Read negative Y blocks from the primer (Y: -64 to -1)
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = DimensionHelper.EXTENDED_MIN_Y; l < 0; ++l) {
                    IBlockState iblockstate = primer.getBlockState(j, l, k);

                    if (iblockstate.getMaterial() != net.minecraft.block.material.Material.AIR) {
                        int chunkY = (l >> 4) + DimensionHelper.SECTION_OFFSET;

                        if (this.storageArrays[chunkY] == NULL_BLOCK_STORAGE) {
                            this.storageArrays[chunkY] = new ExtendedBlockStorage(l >> 4 << 4, flag);
                        }

                        this.storageArrays[chunkY].set(j, l & 15, k, iblockstate);
                    }
                }
            }
        }
    }

    @Inject(method = "isEmptyBetween", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$isEmptyBetween(int startY, int endY, CallbackInfoReturnable<Boolean> cir) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        if (startY < DimensionHelper.EXTENDED_MIN_Y) {
            startY = DimensionHelper.EXTENDED_MIN_Y;
        }

        if (endY >= DimensionHelper.EXTENDED_TOTAL_HEIGHT) {
            endY = DimensionHelper.EXTENDED_TOTAL_HEIGHT - 1;
        }

        for (int i = startY; i <= endY; i += 16) {
            int chunkY = (i >> 4) + DimensionHelper.SECTION_OFFSET;

            if (chunkY >= 0 && chunkY < this.storageArrays.length) {
                ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];

                if (extendedblockstorage != NULL_BLOCK_STORAGE && !extendedblockstorage.isEmpty()) {
                    cir.setReturnValue(false);

                    return;
                }
            }
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "checkLight(II)Z", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$checkLight(int p_150811_1_, int p_150811_2_, CallbackInfoReturnable<Boolean> cir) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        int i = this.getTopFilledSegment();
        boolean flag = false;
        boolean flag1 = false;
        int minY = depthsupdate$minY();
        net.minecraft.util.math.BlockPos.MutableBlockPos blockpos$mutableblockpos = new net.minecraft.util.math.BlockPos.MutableBlockPos(
                (this.x << 4) + p_150811_1_, 0,
                (this.z << 4) + p_150811_2_);

        for (int j = i + 16 - 1; j > this.world.getSeaLevel() || j > minY && !flag1; --j) {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
            int k = this.getBlockLightOpacity(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(),
                    blockpos$mutableblockpos.getZ());

            if (k == 255 && blockpos$mutableblockpos.getY() < this.world.getSeaLevel()) {
                flag1 = true;
            }

            if (!flag && k > 0) {
                flag = true;
            } else if (flag && k == 0 && !this.world.checkLight(blockpos$mutableblockpos)) {
                cir.setReturnValue(false);

                return;
            }
        }

        for (int l = blockpos$mutableblockpos.getY(); l > minY; --l) {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

            if (this.getBlockState(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ()).getLightValue(this.world, blockpos$mutableblockpos) > 0) {
                this.world.checkLight(blockpos$mutableblockpos);
            }
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "getBlockState(III)Lnet/minecraft/block/state/IBlockState;", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$getBlockState(final int x, final int y, final int z, CallbackInfoReturnable<IBlockState> cir) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        if (this.world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            IBlockState iblockstate = null;

            if (y == 60) {
                iblockstate = Blocks.BARRIER.getDefaultState();
            }

            if (y == 70) {
                iblockstate = ChunkGeneratorDebug.getBlockStateFor(x, z);
            }

            cir.setReturnValue(iblockstate == null ? Blocks.AIR.getDefaultState() : iblockstate);
        } else {
            int chunkY = (y >> 4) + DimensionHelper.SECTION_OFFSET;

            if (y >= DimensionHelper.EXTENDED_MIN_Y && chunkY < this.storageArrays.length) {
                ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];

                if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                    cir.setReturnValue(extendedblockstorage.get(x & 15, y & 15, z & 15));

                    return;
                }
            }

            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$setBlockState(@NonNull BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= this.precipitationHeightMap[l] - 1) {
            this.precipitationHeightMap[l] = -999;
        }

        int i1 = this.heightMap[l];
        IBlockState iblockstate = this.getBlockState(pos.getX(), pos.getY(), pos.getZ());

        if (j < 0 || sayys.depthsupdate.util.BlockUtils.isDeepslate(iblockstate)) {
            state = sayys.depthsupdate.util.BlockUtils.getDeepslateVariant(state);
        }

        if (iblockstate == state) {
            cir.setReturnValue(null);
        } else {
            Block block = state.getBlock();
            Block block1 = iblockstate.getBlock();
            int k1 = iblockstate.getLightOpacity(this.world, pos);
            int chunkY = (j >> 4) + DimensionHelper.SECTION_OFFSET;

            if (chunkY < 0 || chunkY >= this.storageArrays.length) {
                cir.setReturnValue(null);

                return;
            }

            ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];
            boolean flag = false;

            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
                if (block == Blocks.AIR) {
                    cir.setReturnValue(null);

                    return;
                }
                extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[chunkY] = extendedblockstorage;
                flag = j >= i1;
            }

            extendedblockstorage.set(i, j & 15, k, state);

            if (!this.world.isRemote) {
                if (block1 != block)
                    block1.breakBlock(this.world, pos, iblockstate);

                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                    this.world.removeTileEntity(pos);
            } else if (block1.hasTileEntity(iblockstate)) {
                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                    this.world.removeTileEntity(pos);
            }

            if (extendedblockstorage.get(i, j & 15, k).getBlock() != block) {
                cir.setReturnValue(null);
            } else {
                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int j1 = state.getLightOpacity(this.world, pos);

                    if (j1 > 0) {
                        if (j >= i1) {
                            this.relightBlock(i, j + 1, k);
                        }
                    } else if (j == i1 - 1) {
                        this.relightBlock(i, j, k);
                    }

                    if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0
                            || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(i, k);
                    }
                }

                if (!this.world.isRemote && block1 != block
                        && (!this.world.captureBlockSnapshots || block.hasTileEntity(state))) {
                    block.onBlockAdded(this.world, pos, state);
                }

                if (block.hasTileEntity(state)) {
                    TileEntity tileentity1 = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (tileentity1 == null) {
                        tileentity1 = block.createTileEntity(this.world, state);
                        this.world.setTileEntity(pos, tileentity1);
                    }

                    if (tileentity1 != null) {
                        tileentity1.updateContainingBlockInfo();
                    }
                }

                this.dirty = true;

                cir.setReturnValue(iblockstate);
            }
        }
    }

    @Inject(method = "getLightFor", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$getLightFor(EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int chunkY = (j >> 4) + DimensionHelper.SECTION_OFFSET;

        if (chunkY < 0 || chunkY >= this.storageArrays.length) {
            cir.setReturnValue(type.defaultLightValue);

            return;
        }

        ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            int height = this.heightMap[k << 4 | i];

            cir.setReturnValue((j >= height) ? type.defaultLightValue : 0);
        } else if (type == EnumSkyBlock.SKY) {
            cir.setReturnValue(!this.world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(i, j & 15, k));
        } else {
            cir.setReturnValue(type == EnumSkyBlock.BLOCK ? extendedblockstorage.getBlockLight(i, j & 15, k)
                    : type.defaultLightValue);
        }
    }

    @Inject(method = "setLightFor", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$setLightFor(EnumSkyBlock type, @NonNull BlockPos pos, int lightValue, CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int chunkY = (j >> 4) + DimensionHelper.SECTION_OFFSET;

        if (chunkY < 0 || chunkY >= this.storageArrays.length) {
            ci.cancel();

            return;
        }

        ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[chunkY] = extendedblockstorage;
            this.generateSkylightMap();
        }

        this.dirty = true;

        if (type == EnumSkyBlock.SKY) {
            if (this.world.provider.hasSkyLight()) {
                extendedblockstorage.setSkyLight(i, j & 15, k, lightValue);
            }
        } else if (type == EnumSkyBlock.BLOCK) {
            extendedblockstorage.setBlockLight(i, j & 15, k, lightValue);
        }

        ci.cancel();
    }

    @Inject(method = "getLightSubtracted", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$getLightSubtracted(@NonNull BlockPos pos, int amount, CallbackInfoReturnable<Integer> cir) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int chunkY = (j >> 4) + DimensionHelper.SECTION_OFFSET;

        if (chunkY < 0 || chunkY >= this.storageArrays.length) {
            cir.setReturnValue(this.world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue
                    ? EnumSkyBlock.SKY.defaultLightValue - amount
                    : 0);

            return;
        }

        ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            cir.setReturnValue(this.world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue
                    ? EnumSkyBlock.SKY.defaultLightValue - amount
                    : 0);
        } else {
            int l = !this.world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(i, j & 15, k);
            l = l - amount;
            int i1 = extendedblockstorage.getBlockLight(i, j & 15, k);

            if (i1 > l) {
                l = i1;
            }

            cir.setReturnValue(l);
        }
    }

    /**
     * Second inject on primer constructor — for extended dimensions, this completely rebuilds
     * the chunk from the primer to handle the full -64 to 255 range.
     * For non-extended dimensions, vanilla's constructor output is used as-is.
     */
    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ChunkPrimer;II)V", at = @At("RETURN"))
    private void depthsupdate$onChunkPrimerInit(@NonNull World worldIn, ChunkPrimer primer, int x, int z, CallbackInfo ci) {
        if (!DimensionHelper.isExtendedDimension(worldIn)) {
            return;
        }

        boolean flag = worldIn.provider.hasSkyLight();

        for (int i = 0; i < this.storageArrays.length; ++i) {
            this.storageArrays[i] = null;
        }

        for (int i = 0; i < this.heightMap.length; ++i) {
            this.heightMap[i] = DimensionHelper.EXTENDED_MIN_Y;
        }

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = DimensionHelper.EXTENDED_MIN_Y; l < DimensionHelper.EXTENDED_MAX_Y; ++l) {
                    IBlockState iblockstate = primer.getBlockState(j, l, k);

                    if (iblockstate.getMaterial() != net.minecraft.block.material.Material.AIR) {
                        int chunkY = (l >> 4) + DimensionHelper.SECTION_OFFSET;

                        if (this.storageArrays[chunkY] == null) {
                            this.storageArrays[chunkY] = new ExtendedBlockStorage(l >> 4 << 4, flag);
                        }

                        this.storageArrays[chunkY].set(j, l & 15, k, iblockstate);
                    }
                }
            }
        }
    }

    @Inject(method = "getTopFilledSegment", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$getTopFilledSegment(CallbackInfoReturnable<Integer> cir) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        for (int i = this.storageArrays.length - 1; i >= 0; --i) {
            if (this.storageArrays[i] != NULL_BLOCK_STORAGE) {
                cir.setReturnValue(this.storageArrays[i].getYLocation());

                return;
            }
        }

        cir.setReturnValue(DimensionHelper.EXTENDED_MIN_Y);
    }

    @Inject(method = "generateHeightMap", at = @At("HEAD"), cancellable = true, require = 0)
    private void depthsupdate$generateHeightMap(@NonNull CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        ci.cancel();
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;
        int minY = DimensionHelper.EXTENDED_MIN_Y;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                this.heightMap[k << 4 | j] = minY;

                for (int l = i + 16; l > minY; --l) {
                    if (this.getBlockLightOpacity(j, l - 1, k) != 0) {
                        this.heightMap[k << 4 | j] = l;

                        if (l < this.heightMapMinimum) {
                            this.heightMapMinimum = l;
                        }

                        break;
                    }
                }
            }
        }

        this.dirty = true;
    }

    @Inject(method = "generateSkylightMap", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$generateSkylightMap(@NonNull CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        ci.cancel();
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;
        int minY = DimensionHelper.EXTENDED_MIN_Y;
        int sectionOffset = DimensionHelper.SECTION_OFFSET;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                this.heightMap[k << 4 | j] = minY;

                for (int l = i + 16; l > minY; --l) {
                    if (this.getBlockLightOpacity(j, l - 1, k) != 0) {
                        this.heightMap[k << 4 | j] = l;

                        if (l < this.heightMapMinimum) {
                            this.heightMapMinimum = l;
                        }

                        break;
                    }
                }

                if (this.world.provider.hasSkyLight()) {
                    int k1 = 15;
                    int i1 = i + 16 - 1;

                    while (true) {
                        int j1 = this.getBlockLightOpacity(j, i1, k);

                        if (j1 == 0 && k1 != 15) {
                            j1 = 1;
                        }

                        k1 -= j1;

                        if (k1 > 0) {
                            int chunkY = (i1 >> 4) + sectionOffset;

                            if (chunkY >= 0 && chunkY < this.storageArrays.length) {
                                ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];

                                if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                                    extendedblockstorage.setSkyLight(j, i1 & 15, k, k1);
                                    this.world.notifyLightSet(new BlockPos((this.x << 4) + j, i1, (this.z << 4) + k));
                                }
                            }
                        } else {
                            break;
                        }

                        --i1;

                        if (i1 < minY || k1 <= 0) {
                            break;
                        }
                    }
                }
            }
        }

        this.dirty = true;
    }

    @Inject(method = "relightBlock", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$relightBlock(int x, int y, int z, @NonNull CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        ci.cancel();
        int i = this.heightMap[z << 4 | x];
        int j = i;
        int minY = DimensionHelper.EXTENDED_MIN_Y;
        int sectionOffset = DimensionHelper.SECTION_OFFSET;

        if (y > i) {
            j = y;
        }

        while (j > minY && this.getBlockLightOpacity(x, j - 1, z) == 0) {
            --j;
        }

        if (j != i) {
            this.world.markBlocksDirtyVertical(x + this.x * 16, z + this.z * 16, j, i);
            this.heightMap[z << 4 | x] = j;
            int k = this.x * 16 + x;
            int l = this.z * 16 + z;

            if (this.world.provider.hasSkyLight()) {
                if (j < i) {
                    for (int j1 = j; j1 < i; ++j1) {
                        int chunkY = (j1 >> 4) + sectionOffset;

                        if (chunkY >= 0 && chunkY < this.storageArrays.length) {
                            ExtendedBlockStorage extendedblockstorage2 = this.storageArrays[chunkY];

                            if (extendedblockstorage2 != NULL_BLOCK_STORAGE) {
                                extendedblockstorage2.setSkyLight(x, j1 & 15, z, 15);
                                this.world.notifyLightSet(new BlockPos((this.x << 4) + x, j1, (this.z << 4) + z));
                            }
                        }
                    }
                } else {
                    for (int i1 = i; i1 < j; ++i1) {
                        int chunkY = (i1 >> 4) + sectionOffset;

                        if (chunkY >= 0 && chunkY < this.storageArrays.length) {
                            ExtendedBlockStorage extendedblockstorage = this.storageArrays[chunkY];

                            if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                                extendedblockstorage.setSkyLight(x, i1 & 15, z, 0);
                                this.world.notifyLightSet(new BlockPos((this.x << 4) + x, i1, (this.z << 4) + z));
                            }
                        }
                    }
                }

                int k1 = 15;

                while (j > minY && k1 > 0) {
                    --j;
                    int i2 = this.getBlockLightOpacity(x, j, z);

                    if (i2 == 0) {
                        i2 = 1;
                    }

                    k1 -= i2;

                    if (k1 < 0) {
                        k1 = 0;
                    }

                    int chunkY = (j >> 4) + sectionOffset;

                    if (chunkY >= 0 && chunkY < this.storageArrays.length) {
                        ExtendedBlockStorage extendedblockstorage1 = this.storageArrays[chunkY];

                        if (extendedblockstorage1 != NULL_BLOCK_STORAGE) {
                            extendedblockstorage1.setSkyLight(x, j & 15, z, k1);
                        }
                    }
                }
            }

            int l1 = this.heightMap[z << 4 | x];
            int j2 = i;
            int k2 = l1;

            if (l1 < i) {
                j2 = l1;
                k2 = i;
            }

            if (l1 < this.heightMapMinimum) {
                this.heightMapMinimum = l1;
            }

            if (this.world.provider.hasSkyLight()) {
                for (net.minecraft.util.EnumFacing enumfacing : net.minecraft.util.EnumFacing.Plane.HORIZONTAL) {
                    this.updateSkylightNeighborHeight(k + enumfacing.getXOffset(), l + enumfacing.getZOffset(), j2, k2);
                }

                this.updateSkylightNeighborHeight(k, l, j2, k2);
            }

            this.dirty = true;
        }
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$addEntity(@NonNull Entity entityIn, CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        this.hasEntities = true;
        int i = MathHelper.floor(entityIn.posX / 16.0D);
        int j = MathHelper.floor(entityIn.posZ / 16.0D);

        if (i != this.x || j != this.z) {
            org.apache.logging.log4j.LogManager.getLogger().warn("Wrong location! ({}, {}) should be ({}, {}), {}", i,
                    j, this.x, this.z, entityIn);
            entityIn.setDead();
        }

        int k = MathHelper.floor(entityIn.posY / 16.0D) + DimensionHelper.SECTION_OFFSET;

        if (k < 0) {
            k = 0;
        }

        if (k >= this.entityLists.length) {
            k = this.entityLists.length - 1;
        }

        net.minecraftforge.common.MinecraftForge.EVENT_BUS
                .post(new net.minecraftforge.event.entity.EntityEvent.EnteringChunk(entityIn, this.x, this.z, entityIn.chunkCoordX, entityIn.chunkCoordZ));
        entityIn.addedToChunk = true;
        entityIn.chunkCoordX = this.x;
        entityIn.chunkCoordY = k;
        entityIn.chunkCoordZ = this.z;
        this.entityLists[k].add(entityIn);
        this.dirty = true;

        ci.cancel();
    }

    @Inject(method = "getEntitiesWithinAABBForEntity", at = @At("HEAD"), cancellable = true)
    public void depthsupdate$getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, @NonNull AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> filter, CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        int offset = DimensionHelper.SECTION_OFFSET;
        int i = MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D) + offset;
        int j = MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D) + offset;
        i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
        j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

        for (int k = i; k <= j; ++k) {
            if (!this.entityLists[k].isEmpty()) {
                for (Entity entity : this.entityLists[k]) {
                    if (entity.getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                        if (filter == null || filter.apply(entity)) {
                            listToFill.add(entity);
                        }

                        Entity[] aentity = entity.getParts();

                        if (aentity != null) {
                            for (Entity entity1 : aentity) {
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                        && (filter == null || filter.apply(entity1))) {
                                    listToFill.add(entity1);
                                }
                            }
                        }
                    }
                }
            }
        }

        ci.cancel();
    }

    @Inject(method = "getEntitiesOfTypeWithinAABB", at = @At("HEAD"), cancellable = true)
    public <T extends Entity> void depthsupdate$getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, @NonNull AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter, CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        int offset = DimensionHelper.SECTION_OFFSET;
        int i = MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D) + offset;
        int j = MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D) + offset;
        i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
        j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

        for (int k = i; k <= j; ++k) {
            for (T t : this.entityLists[k].getByClass(entityClass)) {
                if (t.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(t))) {
                    listToFill.add(t);
                }
            }
        }

        ci.cancel();
    }
}
