package sayys.depthsupdate.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockPointedDripstone extends Block {
    public static final PropertyEnum<DripstoneThickness> THICKNESS = PropertyEnum.create(
        "thickness", DripstoneThickness.class
    );
    public static final PropertyDirection TIP_DIRECTION = PropertyDirection.create(
        "tip_direction", EnumFacing.Plane.VERTICAL
    );
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    private static final AxisAlignedBB SHAPE_TIP_MERGE = new AxisAlignedBB(
        0.2D, 0.0D, 0.2D, 0.8D, 1.0D, 0.8D
    );
    private static final AxisAlignedBB SHAPE_TIP_UP = new AxisAlignedBB(
        0.2D, 0.0D, 0.2D, 0.8D, 0.7D, 0.8D
    );
    private static final AxisAlignedBB SHAPE_TIP_DOWN = new AxisAlignedBB(
        0.2D, 0.3D, 0.2D, 0.8D, 1.0D, 0.8D
    );
    private static final AxisAlignedBB SHAPE_FRUSTUM = new AxisAlignedBB(
        0.15D, 0.0D, 0.15D, 0.85D, 1.0D, 0.85D
    );
    private static final AxisAlignedBB SHAPE_MIDDLE = new AxisAlignedBB(
        0.1D, 0.0D, 0.1D, 0.9D, 1.0D, 0.9D
    );
    private static final AxisAlignedBB SHAPE_BASE = new AxisAlignedBB(
        0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D
    );

    public BlockPointedDripstone() {
        super(Material.ROCK);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(TIP_DIRECTION, EnumFacing.UP)
                .withProperty(THICKNESS, DripstoneThickness.TIP)
                .withProperty(WATERLOGGED, false));
        this.setHardness(1.5F);
        this.setResistance(3.0F);
        this.setSoundType(SoundType.STONE);
        this.setRegistryName("depthsupdate", "pointed_dripstone");
        this.setTranslationKey("pointed_dripstone");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setTickRandomly(true);
        this.setLightOpacity(0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TIP_DIRECTION, THICKNESS, WATERLOGGED);
    }

    @Override
    public int getMetaFromState(@NonNull IBlockState state) {
        int i = 0;

        if (state.getValue(TIP_DIRECTION) == EnumFacing.UP) {
            i |= 8;
        }

        i |= state.getValue(THICKNESS).ordinal();

        return i;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing direction = (meta & 8) != 0 ? EnumFacing.UP : EnumFacing.DOWN;
        int thicknessOrd = meta & 7;
        DripstoneThickness thickness = DripstoneThickness.values()[Math.min(thicknessOrd, DripstoneThickness.values().length - 1)];

        return this.getDefaultState()
                .withProperty(TIP_DIRECTION, direction)
                .withProperty(THICKNESS, thickness);
    }

    @Override
    public AxisAlignedBB getBoundingBox(@NonNull IBlockState state, IBlockAccess source, BlockPos pos) {
        DripstoneThickness thickness = state.getValue(THICKNESS);
        EnumFacing dir = state.getValue(TIP_DIRECTION);

        return switch (thickness) {
            case TIP_MERGE -> SHAPE_TIP_MERGE;
            case TIP -> dir == EnumFacing.UP ? SHAPE_TIP_UP : SHAPE_TIP_DOWN;
            case FRUSTUM -> SHAPE_FRUSTUM;
            case MIDDLE -> SHAPE_MIDDLE;
            case BASE -> SHAPE_BASE;
            default -> FULL_BLOCK_AABB;
        };
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return isValidPointedDripstonePlacement(worldIn, pos, EnumFacing.DOWN) || isValidPointedDripstonePlacement(worldIn, pos, EnumFacing.UP);
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, @NonNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing defaultTipDirection = facing.getOpposite();
        EnumFacing tipDirection = calculateTipDirection(world, pos, defaultTipDirection);

        if (tipDirection == null) {
            return this.getDefaultState();
        }

        boolean mergeOpposingTips = !placer.isSneaking();
        DripstoneThickness thickness = calculateDripstoneThickness(world, pos, tipDirection, mergeOpposingTips);

        boolean isWaterlogged = world.getBlockState(pos).getMaterial() == Material.WATER;

        return this.getDefaultState()
                .withProperty(TIP_DIRECTION, tipDirection)
                .withProperty(THICKNESS, thickness)
                .withProperty(WATERLOGGED, isWaterlogged);
    }

    @Override
    public void onBlockAdded(@NonNull World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            if (!isValidPointedDripstonePlacement(worldIn, pos, state.getValue(TIP_DIRECTION))) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);

        if (worldIn.isRemote) return;

        EnumFacing tipDirection = state.getValue(TIP_DIRECTION);

        if (!isValidPointedDripstonePlacement(worldIn, pos, tipDirection)) {
            worldIn.scheduleUpdate(pos, this, 2);
        } else {
            DripstoneThickness newThickness = calculateDripstoneThickness(worldIn, pos, tipDirection, true);
            if (newThickness != state.getValue(THICKNESS)) {
                worldIn.setBlockState(pos, state.withProperty(THICKNESS, newThickness), 3);
            }
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, @NonNull IBlockState state, Random rand) {
        if (!isValidPointedDripstonePlacement(worldIn, pos, state.getValue(TIP_DIRECTION))) {
            if (isStalactite(state)) {
                spawnFallingStalactite(state, worldIn, pos);
            } else {
                worldIn.destroyBlock(pos, true);
            }
        } else {
            maybeTransferFluid(state, worldIn, pos, rand.nextFloat());
            if (rand.nextFloat() < 0.011377778F && isStalactiteStartPos(state, worldIn, pos)) {
                growStalactiteOrStalagmiteIfPossible(state, worldIn, pos, rand);
            }
        }
    }

    @Override
    public void onFallenUpon(@NonNull World worldIn, BlockPos pos, net.minecraft.entity.Entity entityIn, float fallDistance) {
        IBlockState state = worldIn.getBlockState(pos);

        if (state.getValue(TIP_DIRECTION) == EnumFacing.UP && state.getValue(THICKNESS) == DripstoneThickness.TIP) {
            entityIn.fall(fallDistance + 2.5F, 2.0F);
        } else {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
    }

    private static void spawnFallingStalactite(IBlockState state, World world, BlockPos pos) {
        BlockPos.MutableBlockPos fallPos = new BlockPos.MutableBlockPos(pos);

        for (IBlockState fallState = state; isStalactite(fallState); fallState = world.getBlockState(fallPos)) {
            EntityFallingBlock entity = new EntityFallingBlock(world, fallPos.getX() + 0.5D, fallPos.getY(), fallPos.getZ() + 0.5D, fallState);
            entity.setHurtEntities(true);
            entity.shouldDropItem = true;
            entity.fallTime = 1;
            world.spawnEntity(entity);

            world.setBlockToAir(fallPos);
            fallPos.move(EnumFacing.DOWN);
        }
    }

    public static void growStalactiteOrStalagmiteIfPossible(IBlockState stalactiteStartState, @NonNull World world, @NonNull BlockPos stalactiteStartPos, java.util.Random random) {
        IBlockState rootState = world.getBlockState(stalactiteStartPos.up(1));
        IBlockState stateAbove = world.getBlockState(stalactiteStartPos.up(2));

        if (canGrow(rootState, stateAbove)) {
            BlockPos stalactiteTipPos = findTip(stalactiteStartState, world, stalactiteStartPos, 7, false);

            if (stalactiteTipPos != null) {
                IBlockState stalactiteTipState = world.getBlockState(stalactiteTipPos);

                if (canDrip(stalactiteTipState) && canTipGrow(stalactiteTipState, world, stalactiteTipPos)) {
                    if (random.nextBoolean()) {
                        grow(world, stalactiteTipPos, EnumFacing.DOWN);
                    } else {
                        growStalagmiteBelow(world, stalactiteTipPos);
                    }
                }
            }
        }
    }

    private static boolean canGrow(@NonNull IBlockState rootState, IBlockState aboveState) {
        return rootState.getBlock() == RegistryHandler.dripstone_block && aboveState.getMaterial() == Material.WATER;
    }

    private static boolean canTipGrow(@NonNull IBlockState tipState, @NonNull World world, @NonNull BlockPos tipPos) {
        EnumFacing growDirection = tipState.getValue(TIP_DIRECTION);
        BlockPos growPos = tipPos.offset(growDirection);
        IBlockState stateAtGrowPos = world.getBlockState(growPos);

        if (stateAtGrowPos.getMaterial() == Material.WATER || stateAtGrowPos.getMaterial() == Material.LAVA) {
            return false;
        }

        return stateAtGrowPos.getBlock().isReplaceable(world, growPos) || isUnmergedTipWithDirection(stateAtGrowPos, growDirection.getOpposite());
    }

    private static boolean isUnmergedTipWithDirection(IBlockState state, EnumFacing tipDirection) {
        return isTip(state, false) && state.getValue(TIP_DIRECTION) == tipDirection;
    }

    private static void grow(@NonNull World world, @NonNull BlockPos growFromPos, EnumFacing growToDirection) {
        BlockPos targetPos = growFromPos.offset(growToDirection);
        IBlockState existingStateAtTargetPos = world.getBlockState(targetPos);

        if (isUnmergedTipWithDirection(existingStateAtTargetPos, growToDirection.getOpposite())) {
            createMergedTips(existingStateAtTargetPos, world, targetPos);
        } else if (existingStateAtTargetPos.getBlock().isReplaceable(world, targetPos)) {
            createDripstone(world, targetPos, growToDirection, DripstoneThickness.TIP);
        }
    }

    private static void growStalagmiteBelow(World world, BlockPos posAboveStalagmite) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posAboveStalagmite);

        for (int i = 0; i < 10; ++i) {
            pos.move(EnumFacing.DOWN);
            IBlockState state = world.getBlockState(pos);

            if (state.getMaterial() == Material.WATER || state.getMaterial() == Material.LAVA) return;

            if (isUnmergedTipWithDirection(state, EnumFacing.UP) && canTipGrow(state, world, pos)) {
                grow(world, pos, EnumFacing.UP);

                return;
            }

            if (isValidPointedDripstonePlacement(world, pos, EnumFacing.UP) && world.getBlockState(pos.down()).getMaterial() != Material.WATER) {
                grow(world, pos.down(), EnumFacing.UP);

                return;
            }
        }
    }

    private static void createDripstone(@NonNull World world, BlockPos pos, EnumFacing direction, DripstoneThickness thickness) {
        IBlockState state = RegistryHandler.pointed_dripstone.getDefaultState()
            .withProperty(TIP_DIRECTION, direction)
            .withProperty(THICKNESS, thickness)
            .withProperty(WATERLOGGED, world.getBlockState(pos).getMaterial() == Material.WATER);
        world.setBlockState(pos, state, 3);
    }

    private static void createMergedTips(@NonNull IBlockState tipState, World world, BlockPos tipPos) {
        BlockPos stalactitePos;
        BlockPos stalagmitePos;

        if (tipState.getValue(TIP_DIRECTION) == EnumFacing.UP) {
            stalagmitePos = tipPos;
            stalactitePos = tipPos.up();
        } else {
            stalactitePos = tipPos;
            stalagmitePos = tipPos.down();
        }

        createDripstone(world, stalactitePos, EnumFacing.DOWN, DripstoneThickness.TIP_MERGE);
        createDripstone(world, stalagmitePos, EnumFacing.UP, DripstoneThickness.TIP_MERGE);
    }

    @Nullable
    private EnumFacing calculateTipDirection(World world, BlockPos pos, EnumFacing defaultTipDirection) {
        if (defaultTipDirection != EnumFacing.UP && defaultTipDirection != EnumFacing.DOWN) {
            return null;
        }

        if (isValidPointedDripstonePlacement(world, pos, defaultTipDirection)) {
            return defaultTipDirection;
        } else if (isValidPointedDripstonePlacement(world, pos, defaultTipDirection.getOpposite())) {
            return defaultTipDirection.getOpposite();
        }

        return null;
    }

    private DripstoneThickness calculateDripstoneThickness(@NonNull World world, @NonNull BlockPos pos, @NonNull EnumFacing tipDirection, boolean mergeOpposingTips) {
        EnumFacing baseDirection = tipDirection.getOpposite();
        IBlockState inFrontState = world.getBlockState(pos.offset(tipDirection));

        if (isPointedDripstoneWithDirection(inFrontState, baseDirection)) {
            return mergeOpposingTips ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP;
        } else if (!isPointedDripstoneWithDirection(inFrontState, tipDirection)) {
            return DripstoneThickness.TIP;
        } else {
            DripstoneThickness inFrontThickness = inFrontState.getValue(THICKNESS);
            if (inFrontThickness != DripstoneThickness.TIP && inFrontThickness != DripstoneThickness.TIP_MERGE) {
                IBlockState behindState = world.getBlockState(pos.offset(baseDirection));

                return !isPointedDripstoneWithDirection(behindState, tipDirection) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
            } else {
                return DripstoneThickness.FRUSTUM;
            }
        }
    }

    private static boolean isValidPointedDripstonePlacement(@NonNull World world, @NonNull BlockPos pos, @NonNull EnumFacing tipDirection) {
        BlockPos behindPos = pos.offset(tipDirection.getOpposite());
        IBlockState behindState = world.getBlockState(behindPos);

        return behindState.isSideSolid(world, behindPos, tipDirection) || isPointedDripstoneWithDirection(behindState, tipDirection);
    }

    public static boolean isPointedDripstoneWithDirection(@NonNull IBlockState state, EnumFacing tipDirection) {
        return state.getBlock() instanceof BlockPointedDripstone && state.getValue(TIP_DIRECTION) == tipDirection;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, java.util.Random rand) {
        if (canDrip(stateIn)) {
            float f = rand.nextFloat();

            if (f <= 0.12F) {
                Material fluidAbove = getFluidAboveStalactite(worldIn, pos, stateIn);

                if (fluidAbove != null && (f < 0.02F || fluidAbove == Material.WATER || fluidAbove == Material.LAVA)) {
                    spawnDripParticle(worldIn, pos, stateIn, fluidAbove);
                }
            }
        }
    }


    public static boolean canDrip(IBlockState state) {
        return isStalactite(state) && state.getValue(THICKNESS) == DripstoneThickness.TIP && !state.getValue(WATERLOGGED);
    }

    private static boolean isStalactite(IBlockState state) {
        return isPointedDripstoneWithDirection(state, EnumFacing.DOWN);
    }

    @Nullable
    public static Material getFluidAboveStalactite(World world, BlockPos pos, IBlockState state) {
        if (!isStalactite(state)) return null;

        BlockPos rootPos = findRootBlock(world, pos, state, 11);

        if (rootPos != null) {
            BlockPos aboveRootPos = rootPos.up();
            IBlockState aboveRootState = world.getBlockState(aboveRootPos);
            Material material = aboveRootState.getMaterial();

            if (material == Material.WATER || material == Material.LAVA) {
                return material;
            }

            if (aboveRootState.isSideSolid(world, aboveRootPos, EnumFacing.DOWN)) {
                BlockPos liquidPos = aboveRootPos.up();
                IBlockState liquidState = world.getBlockState(liquidPos);
                Material liquidMaterial = liquidState.getMaterial();

                if (liquidMaterial == Material.WATER || liquidMaterial == Material.LAVA) {
                    return liquidMaterial;
                }
            }
        }

        return null;
    }

    @Nullable
    private static BlockPos findRootBlock(World world, BlockPos pos, @NonNull IBlockState state, int maxSearchLength) {
        EnumFacing tipDirection = state.getValue(TIP_DIRECTION);
        EnumFacing searchDirection = tipDirection.getOpposite();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);

        for (int i = 1; i < maxSearchLength; ++i) {
            mutablePos.move(searchDirection);
            IBlockState checkState = world.getBlockState(mutablePos);

            if (!isPointedDripstoneWithDirection(checkState, tipDirection)) {
                return mutablePos.offset(tipDirection).toImmutable();
            }
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void spawnDripParticle(World world, @NonNull BlockPos pos, IBlockState state, Material fluidAbove) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;

        if (fluidAbove == Material.LAVA) {
            world.spawnParticle(EnumParticleTypes.DRIP_LAVA, x, y, z, 0.0D, 0.0D, 0.0D);
        } else {
            world.spawnParticle(EnumParticleTypes.DRIP_WATER, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void maybeTransferFluid(IBlockState state, World world, BlockPos pos, float randomValue) {
        if (randomValue > 0.17578125F && randomValue > 0.05859375F) {
            return;
        }

        if (isStalactiteStartPos(state, world, pos)) {
            Material fluidAbove = getFluidAboveStalactite(world, pos, state);

            if (fluidAbove != null) {
                float transferProbability = fluidAbove == Material.WATER ? 0.17578125F : 0.05859375F;

                if (randomValue < transferProbability) {
                    BlockPos tipPos = findTip(state, world, pos, 11, false);

                    if (tipPos != null) {
                        BlockPos cauldronPos = findFillableCauldronBelowStalactiteTip(world, tipPos);

                        if (cauldronPos != null) {
                            IBlockState cauldronState = world.getBlockState(cauldronPos);

                            if (cauldronState.getBlock() == Blocks.CAULDRON) {
                                int level = cauldronState.getValue(BlockCauldron.LEVEL);

                                if (level < 3) {
                                    world.setBlockState(cauldronPos, cauldronState.withProperty(BlockCauldron.LEVEL, level + 1));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isStalactiteStartPos(IBlockState state, World world, BlockPos pos) {
        return isStalactite(state) && !(world.getBlockState(pos.up()).getBlock() instanceof BlockPointedDripstone);
    }

    @Nullable
    private static BlockPos findTip(IBlockState dripstoneState, World world, BlockPos pos, int maxSearchLength, boolean includeMergedTip) {
        if (isTip(dripstoneState, includeMergedTip)) {
            return pos;
        }

        EnumFacing searchDirection = dripstoneState.getValue(TIP_DIRECTION);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);

        for (int i = 1; i < maxSearchLength; ++i) {
            mutablePos.move(searchDirection);
            IBlockState checkState = world.getBlockState(mutablePos);

            if (isPointedDripstoneWithDirection(checkState, searchDirection)) {
                if (isTip(checkState, includeMergedTip)) {
                    return mutablePos.toImmutable();
                }
            } else {
                return null;
            }
        }

        return null;
    }

    private static boolean isTip(@NonNull IBlockState state, boolean includeMergedTip) {
        if (!(state.getBlock() instanceof BlockPointedDripstone)) return false;

        DripstoneThickness thickness = state.getValue(THICKNESS);

        return thickness == DripstoneThickness.TIP || (includeMergedTip && thickness == DripstoneThickness.TIP_MERGE);
    }

    @Nullable
    private static BlockPos findFillableCauldronBelowStalactiteTip(World world, BlockPos tipPos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(tipPos);

        for (int i = 1; i < 11; ++i) {
            mutablePos.move(EnumFacing.DOWN);
            IBlockState state = world.getBlockState(mutablePos);

            if (state.getBlock() == Blocks.CAULDRON) {
                if (state.getValue(BlockCauldron.LEVEL) < 3) return mutablePos.toImmutable();

                return null;
            }

            if (state.getMaterial() != Material.AIR) {
                return null;
            }
        }

        return null;
    }

    public enum DripstoneThickness implements IStringSerializable {
        TIP_MERGE("tip_merge"),
        TIP("tip"),
        FRUSTUM("frustum"),
        MIDDLE("middle"),
        BASE("base");

        private final String name;

        DripstoneThickness(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
