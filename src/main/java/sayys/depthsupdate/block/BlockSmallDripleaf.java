package sayys.depthsupdate.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockSmallDripleaf extends BlockBush implements IGrowable, IShearable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<sayys.depthsupdate.block.BlockSmallDripleaf.EnumBlockHalf> HALF = PropertyEnum.create("half", sayys.depthsupdate.block.BlockSmallDripleaf.EnumBlockHalf.class);

    protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125D, 0.0D, 0.09375D, 0.875D, 0.8125D, 0.90625D);

    public BlockSmallDripleaf() {
        super(Material.PLANTS, MapColor.FOLIAGE);

        this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, sayys.depthsupdate.block.BlockSmallDripleaf.EnumBlockHalf.LOWER).withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setRegistryName("depthsupdate", "small_dripleaf");
        this.setTranslationKey("small_dripleaf");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.FARMLAND ||
               block == Blocks.CLAY || block == Blocks.MYCELIUM;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos.up(), this.getDefaultState().withProperty(HALF, EnumBlockHalf.UPPER).withProperty(FACING, state.getValue(FACING)), 2);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
            if (worldIn.getBlockState(pos.down()).getBlock() == this) {
                if (player.capabilities.isCreativeMode) {
                    worldIn.setBlockToAir(pos.down());
                } else {
                    worldIn.destroyBlock(pos.down(), true);
                }
            }
        } else if (worldIn.getBlockState(pos.up()).getBlock() == this) {
            worldIn.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 2);
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
            return worldIn.getBlockState(pos.down()).getBlock() == this;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos.up());
            return iblockstate.getBlock() == this && super.canBlockStay(worldIn, pos, state);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HALF, (meta & 8) > 0 ? EnumBlockHalf.UPPER : EnumBlockHalf.LOWER).withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
            i |= 8;
        }
        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{HALF, FACING});
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        BlockPos bottomPos = state.getValue(HALF) == EnumBlockHalf.LOWER ? pos : pos.down();
        EnumFacing facing = state.getValue(FACING);

        worldIn.setBlockToAir(bottomPos.up());
        worldIn.setBlockToAir(bottomPos);

        int height = rand.nextInt(4) + 2;

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(bottomPos);
        int currentHeight = 0;

        while (currentHeight < height && worldIn.isAirBlock(mpos)) {
            currentHeight++;
            mpos.move(EnumFacing.UP);
        }

        int targetHeadY = bottomPos.getY() + currentHeight - 1;
        mpos.setPos(bottomPos.getX(), bottomPos.getY(), bottomPos.getZ());

        if (RegistryHandler.big_dripleaf == null || RegistryHandler.big_dripleaf_stem == null) return;

        while(mpos.getY() < targetHeadY) {
            worldIn.setBlockState(mpos, RegistryHandler.big_dripleaf_stem.getDefaultState().withProperty(BlockHorizontal.FACING, facing), 3);
            mpos.move(EnumFacing.UP);
        }

        worldIn.setBlockState(mpos, RegistryHandler.big_dripleaf.getDefaultState().withProperty(BlockHorizontal.FACING, facing), 3);
    }

    public enum EnumBlockHalf implements net.minecraft.util.IStringSerializable {
        UPPER, LOWER;

        public String toString() { return this.getName(); }
        public String getName() { return this == UPPER ? "upper" : "lower"; }
    }
}
