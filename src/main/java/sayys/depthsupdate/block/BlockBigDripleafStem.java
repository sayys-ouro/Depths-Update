package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockBigDripleafStem extends Block implements IGrowable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    protected static final AxisAlignedBB SHAPE_NORTH = new AxisAlignedBB(0.3125D, 0.0D, 0.25D, 0.6875D, 1.0D, 1.0D);
    protected static final AxisAlignedBB SHAPE_SOUTH = new AxisAlignedBB(0.3125D, 0.0D, 0.0D, 0.6875D, 1.0D, 0.75D);
    protected static final AxisAlignedBB SHAPE_WEST = new AxisAlignedBB(0.25D, 0.0D, 0.3125D, 1.0D, 1.0D, 0.6875D);
    protected static final AxisAlignedBB SHAPE_EAST = new AxisAlignedBB(0.0D, 0.0D, 0.3125D, 0.75D, 1.0D, 0.6875D);

    public BlockBigDripleafStem() {
        super(Material.PLANTS, MapColor.FOLIAGE);

        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(0.1F);
        this.setHarvestLevel("axe", 0);
        this.setSoundType(SoundType.PLANT);
        this.setRegistryName("depthsupdate", "big_dripleaf_stem");
        this.setTranslationKey("big_dripleaf_stem");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case SOUTH: return SHAPE_SOUTH;
            case WEST: return SHAPE_WEST;
            case EAST: return SHAPE_EAST;
            case NORTH:
            default: return SHAPE_NORTH;
        }
    }

    @javax.annotation.Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
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
    public net.minecraft.util.BlockRenderLayer getRenderLayer() {
        return net.minecraft.util.BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState downState = worldIn.getBlockState(pos.down());
        IBlockState upState = worldIn.getBlockState(pos.up());
        Block upBlock = upState.getBlock();
        Block downBlock = downState.getBlock();

        boolean canStayDown = downBlock == this || downState.isSideSolid(worldIn, pos.down(), EnumFacing.UP) || downBlock == Blocks.DIRT || downBlock == Blocks.GRASS || downBlock == Blocks.CLAY || downBlock == Blocks.FARMLAND;
        boolean canStayUp = upBlock == this || (RegistryHandler.big_dripleaf != null && upBlock == RegistryHandler.big_dripleaf);

        return canStayDown && canStayUp;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canPlaceBlockAt(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return RegistryHandler.big_dripleaf == null ? Items.AIR : Item.getItemFromBlock(RegistryHandler.big_dripleaf);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(RegistryHandler.big_dripleaf);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        BlockPos headPos = findHead(worldIn, pos);

        if (headPos != null && worldIn.isAirBlock(headPos.up())) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        BlockPos headPos = findHead(worldIn, pos);
        if (headPos != null) {
            BlockPos placePos = headPos.up();
            IBlockState headState = worldIn.getBlockState(headPos);
            EnumFacing facing = headState.getProperties().containsKey(FACING) ? headState.getValue(FACING) : state.getValue(FACING);

            worldIn.setBlockState(headPos, this.getDefaultState().withProperty(FACING, facing), 3);
            worldIn.setBlockState(placePos, headState, 3);
        }
    }

    private BlockPos findHead(World worldIn, BlockPos pos) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(pos);

        for (int i = 0; i < 256; i++) {
            mpos.move(EnumFacing.UP);
            Block block = worldIn.getBlockState(mpos).getBlock();
            if (block == RegistryHandler.big_dripleaf) {
                return mpos.toImmutable();
            }
            if (block != this) {
                break;
            }
        }

        return null;
    }
}
