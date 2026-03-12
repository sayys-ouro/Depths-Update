package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockCopperOre extends Block {
    public BlockCopperOre() {
        super(Material.ROCK);

        this.setRegistryName("depthsupdate", "copper_ore");
        this.setTranslationKey("copper_ore");
        this.setHardness(3.0F);
        this.setResistance(3.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return RegistryHandler.raw_copper;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(4);
    }
}
