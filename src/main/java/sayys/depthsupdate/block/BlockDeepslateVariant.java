package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockDeepslateVariant extends Block {

    public BlockDeepslateVariant(String name, float hardness, float resistance, SoundType soundType) {
        super(Material.ROCK);
        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public MapColor getMapColor(net.minecraft.block.state.IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.STONE;
    }
}
