package sayys.depthsupdate.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.registry.PlantRegistry;

@ParametersAreNonnullByDefault
public class BlockAzalea extends Block implements IGrowable {
    private static final AxisAlignedBB CANOPY_AABB = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB TRUNK_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D);
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private static final float BONEMEAL_SUCCESS_CHANCE = 0.45F;
    private static final int TRUNK_BASE_HEIGHT = 4;
    private static final int TRUNK_HEIGHT_VARIATION = 3;
    private static final int BEND_MIN = 1;
    private static final int BEND_VARIATION = 2;
    private static final int FOLIAGE_RADIUS = 3;
    private static final int FOLIAGE_HEIGHT = 2;
    private static final int FOLIAGE_ATTEMPTS = 50;
    private static final int FLOWERING_LEAF_RARITY = 4;

    public BlockAzalea(String name) {
        super(Material.PLANTS, MapColor.FOLIAGE);

        this.setHardness(0.5F);
        this.setSoundType(SoundType.PLANT);
        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, CANOPY_AABB);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, TRUNK_AABB);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    @Nullable
    public net.minecraft.util.math.RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, net.minecraft.util.math.Vec3d start, net.minecraft.util.math.Vec3d end) {
        net.minecraft.util.math.RayTraceResult result1 = this.rayTrace(pos, start, end, CANOPY_AABB);
        net.minecraft.util.math.RayTraceResult result2 = this.rayTrace(pos, start, end, TRUNK_AABB);

        if (result1 == null) return result2;
        if (result2 == null) return result1;

        double d1 = result1.hitVec.squareDistanceTo(start);
        double d2 = result2.hitVec.squareDistanceTo(start);

        return d1 < d2 ? result1 : result2;
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return worldIn.getBlockState(pos.up()).getBlock() == Blocks.AIR;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return rand.nextFloat() < BONEMEAL_SUCCESS_CHANCE;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote) {
            return;
        }

        int trunkHeight = TRUNK_BASE_HEIGHT + rand.nextInt(TRUNK_HEIGHT_VARIATION);

        for (int i = 1; i < trunkHeight + FOLIAGE_HEIGHT; i++) {
            if (!worldIn.isAirBlock(pos.up(i))) {
                return;
            }
        }

        Block soil = worldIn.getBlockState(pos.down()).getBlock();

        if (soil == Blocks.DIRT || soil == Blocks.GRASS) {
            worldIn.setBlockState(pos.down(), PlantRegistry.rooted_dirt.getDefaultState(), 2);
        }

        BlockPos top = pos;

        for (int i = 0; i < trunkHeight; i++) {
            top = pos.up(i);
            worldIn.setBlockState(top, Blocks.LOG.getDefaultState(), 2);
        }

        EnumFacing bend = EnumFacing.Plane.HORIZONTAL.random(rand);
        int bendLength = BEND_MIN + rand.nextInt(BEND_VARIATION);

        for (int i = 0; i < bendLength; i++) {
            BlockPos next = top.offset(bend);

            if (!worldIn.isAirBlock(next)) {
                break;
            }

            top = next;
            worldIn.setBlockState(top, Blocks.LOG.getDefaultState()
                    .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(bend.getAxis())), 2);
        }

        placeFoliage(worldIn, rand, top);
    }

    private void placeFoliage(World world, Random rand, BlockPos center) {
        for (int i = 0; i < FOLIAGE_ATTEMPTS; i++) {
            BlockPos leafPos = center.add(
                    rand.nextInt(FOLIAGE_RADIUS * 2 + 1) - FOLIAGE_RADIUS,
                    rand.nextInt(FOLIAGE_HEIGHT + 1) - 1,
                    rand.nextInt(FOLIAGE_RADIUS * 2 + 1) - FOLIAGE_RADIUS);

            if (!world.isAirBlock(leafPos)) {
                continue;
            }

            Block leaves = rand.nextInt(FLOWERING_LEAF_RARITY) == 0
                    ? PlantRegistry.flowering_azalea_leaves
                    : PlantRegistry.azalea_leaves;

            world.setBlockState(leafPos, leaves.getDefaultState()
                    .withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE), 2);
        }
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
}
