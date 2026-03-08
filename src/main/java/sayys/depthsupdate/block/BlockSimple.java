package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;

public class BlockSimple extends Block {
    public BlockSimple(String name, Material material, float hardness, float resistance, SoundType soundType) {
        super(material);

        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        if (state.getMaterial() == Material.GRASS || state.getMaterial() == Material.GROUND) {
            return true;
        }

        return super.canSustainPlant(state, world, pos, direction, plantable);
    }
}
