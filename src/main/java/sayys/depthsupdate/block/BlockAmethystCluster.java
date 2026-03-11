package sayys.depthsupdate.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockAmethystCluster extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private final AxisAlignedBB[] shapes;

    public BlockAmethystCluster(String name, float height, float width, int lightValue) {
        super(Material.GLASS);

        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setHardness(1.5F);
        this.setResistance(1.5F);
        this.setSoundType(SoundType.GLASS);
        this.setLightLevel(lightValue / 15.0F);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));

        double w = width / 16.0D;
        double h = height / 16.0D;
        double offset = (1.0D - w) / 2.0D;

        this.shapes = new AxisAlignedBB[6];
        this.shapes[EnumFacing.UP.getIndex()] = new AxisAlignedBB(offset, 0.0D, offset, 1.0D - offset, h, 1.0D - offset);
        this.shapes[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(offset, 1.0D - h, offset, 1.0D - offset, 1.0D, 1.0D - offset);
        this.shapes[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(offset, offset, 1.0D - h, 1.0D - offset, 1.0D - offset, 1.0D);
        this.shapes[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(offset, offset, 0.0D, 1.0D - offset, 1.0D - offset, h);
        this.shapes[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(1.0D - h, offset, offset, 1.0D, 1.0D - offset, 1.0D - offset);
        this.shapes[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(0.0D, offset, offset, h, 1.0D - offset, 1.0D - offset);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return this.shapes[state.getValue(FACING).getIndex()];
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
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(FACING, EnumFacing.byIndex(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (this.canPlaceOn(worldIn, pos, enumfacing)) {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceOn(World worldIn, BlockPos pos, EnumFacing facing) {
        BlockPos blockpos = pos.offset(facing.getOpposite());

        return worldIn.getBlockState(blockpos).isSideSolid(worldIn, blockpos, facing);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canPlaceOn(worldIn, pos, state.getValue(FACING))) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == RegistryHandler.amethyst_cluster) {
            return RegistryHandler.amethyst_shard;
        }

        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public int quantityDropped(Random random) {
        if (this == RegistryHandler.amethyst_cluster) {
            return 2;
        }

        return 0;
    }

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (this == RegistryHandler.amethyst_cluster) {
            int count = 2;
            super.getDrops(drops, world, pos, state, fortune);
        } else {
            super.getDrops(drops, world, pos, state, fortune);
        }
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (this == RegistryHandler.amethyst_cluster) {
            return 4 + random.nextInt(fortune + 1);
        }

        return 0;
    }
}
