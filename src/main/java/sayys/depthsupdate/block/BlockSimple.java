package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

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
}
