package sayys.depthsupdate.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.registry.AmethystRegistry;
import sayys.depthsupdate.registry.IHasModel;

public class BlockAmethystCluster extends Block implements IHasModel {
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
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7));
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
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (this == AmethystRegistry.amethyst_cluster) {
            Random rand = world instanceof World ? ((World) world).rand : new Random();
            EntityPlayer player = harvesters.get();
            boolean isPickaxe = false;

            if (player != null) {
                ItemStack heldItem = player.getHeldItemMainhand();

                if (!heldItem.isEmpty() && heldItem.getItem().getToolClasses(heldItem).contains("pickaxe")) {
                    isPickaxe = true;
                }
            }

            int count = isPickaxe ? 4 : 2;

            if (fortune > 0) {
                if (fortune == 1) {
                    if (rand.nextInt(3) == 0) {
                        count *= 2;
                    }
                } else if (fortune == 2) {
                    int r = rand.nextInt(4);

                    if (r == 0) {
                        count *= 2;
                    } else if (r == 1) {
                        count *= 3;
                    }
                } else {
                    int r = rand.nextInt(5);

                    if (r == 0) {
                        count *= 2;
                    } else if (r == 1) {
                        count *= 3;
                    } else if (r == 2) {
                        count *= 4;
                    }
                }
            }

            drops.add(new ItemStack(AmethystRegistry.amethyst_shard, count));
        }
    }
}
