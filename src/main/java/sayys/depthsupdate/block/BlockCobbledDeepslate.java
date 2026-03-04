package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockCobbledDeepslate extends Block {
    public BlockCobbledDeepslate() {
        super(Material.ROCK);

        this.setRegistryName("depthsupdate", "cobbled_deepslate");
        this.setTranslationKey("cobbled_deepslate");
        this.setHardness(3.5F);
        this.setResistance(6.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public MapColor getMapColor(net.minecraft.block.state.IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.STONE;
    }
}
