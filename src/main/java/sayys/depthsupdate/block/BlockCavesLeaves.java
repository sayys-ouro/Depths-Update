package sayys.depthsupdate.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCavesLeaves extends BlockLeaves {
    public BlockCavesLeaves(String name) {
        super();

        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setHardness(0.2F);
        this.setResistance(0.2F);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(CHECK_DECAY, Boolean.valueOf(true))
                .withProperty(DECAYABLE, Boolean.valueOf(true)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(DECAYABLE, (meta & 4) == 0)
                .withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        if (!state.getValue(DECAYABLE)) {
            i |= 4;
        }
        if (state.getValue(CHECK_DECAY)) {
            i |= 8;
        }
        return i;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.OAK;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return java.util.Collections.singletonList(new ItemStack(this));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.SAPLING);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
            net.minecraft.util.EnumFacing side) {
        return true;
    }

    @Override
    protected void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance) {}

    @Override
    protected int getSaplingDropChance(IBlockState state) {
        return 20;
    }
}
