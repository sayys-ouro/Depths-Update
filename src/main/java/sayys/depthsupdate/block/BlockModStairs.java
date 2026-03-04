package sayys.depthsupdate.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockModStairs extends BlockStairs {
    public BlockModStairs(String name, IBlockState modelState) {
        super(modelState);

        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.useNeighborBrightness = true;
    }
}
