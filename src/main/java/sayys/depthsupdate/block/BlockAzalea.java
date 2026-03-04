package sayys.depthsupdate.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ParametersAreNonnullByDefault
public class BlockAzalea extends Block implements IGrowable {
    private static final AxisAlignedBB CANOPY_AABB = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB TRUNK_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D);
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

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
        return worldIn.rand.nextFloat() < 0.45D;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {}

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
}
