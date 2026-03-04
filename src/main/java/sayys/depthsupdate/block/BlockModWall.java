package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockModWall extends BlockWall {
    public BlockModWall(String name, Block modelBlock) {
        super(modelBlock);

        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(BlockWall.UP, Boolean.valueOf(false))
                .withProperty(BlockWall.NORTH, Boolean.valueOf(false))
                .withProperty(BlockWall.EAST, Boolean.valueOf(false))
                .withProperty(BlockWall.SOUTH, Boolean.valueOf(false))
                .withProperty(BlockWall.WEST, Boolean.valueOf(false))
                .withProperty(BlockWall.VARIANT, BlockWall.EnumType.NORMAL));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockWall.VARIANT, BlockWall.UP, BlockWall.NORTH, BlockWall.EAST,
                BlockWall.SOUTH, BlockWall.WEST);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }
}
