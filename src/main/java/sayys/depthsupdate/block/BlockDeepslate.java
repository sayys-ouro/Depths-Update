package sayys.depthsupdate.block;

import java.util.Random;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import sayys.depthsupdate.registry.DeepslateRegistry;

public class BlockDeepslate extends BlockRotatedPillar {
    public BlockDeepslate() {
        super(Material.ROCK);

        this.setRegistryName("depthsupdate", "deepslate");
        this.setTranslationKey("deepslate");
        this.setHardness(3.0F);
        this.setResistance(6.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);

        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.STONE;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(DeepslateRegistry.cobbled_deepslate);
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos, com.google.common.base.Predicate<IBlockState> target) {
        if (target != null && target.apply(net.minecraft.init.Blocks.STONE.getDefaultState())) {
            return true;
        }

        return super.isReplaceableOreGen(state, world, pos, target);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing.Axis axis = EnumFacing.Axis.Y;
        int i = meta & 12;

        if (i == 4) {
            axis = EnumFacing.Axis.X;
        } else if (i == 8) {
            axis = EnumFacing.Axis.Z;
        }

        return this.getDefaultState().withProperty(AXIS, axis);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        EnumFacing.Axis axis = (EnumFacing.Axis) state.getValue(AXIS);

        if (axis == EnumFacing.Axis.X) {
            i |= 4;
        } else if (axis == EnumFacing.Axis.Z) {
            i |= 8;
        }

        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }
}
