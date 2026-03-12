package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockDeepslateOre extends Block {
    public BlockDeepslateOre(String name) {
        super(Material.ROCK);

        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setHardness(4.5F);
        this.setResistance(3.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == RegistryHandler.deepslate_iron_ore) return RegistryHandler.raw_iron;
        if (this == RegistryHandler.deepslate_gold_ore) return RegistryHandler.raw_gold;
        if (this == RegistryHandler.deepslate_copper_ore) return RegistryHandler.raw_copper;

        if (this == RegistryHandler.deepslate_coal_ore) return net.minecraft.init.Items.COAL;
        if (this == RegistryHandler.deepslate_diamond_ore) return net.minecraft.init.Items.DIAMOND;
        if (this == RegistryHandler.deepslate_emerald_ore) return net.minecraft.init.Items.EMERALD;
        if (this == RegistryHandler.deepslate_lapis_ore) return net.minecraft.init.Items.DYE;
        if (this == RegistryHandler.deepslate_redstone_ore) return net.minecraft.init.Items.REDSTONE;

        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public int damageDropped(IBlockState state) {
        if (this == RegistryHandler.deepslate_lapis_ore) return 4; // Lapis is blue dye (meta 4)
        return super.damageDropped(state);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.getDefaultState(), random, fortune)) {
            int i = random.nextInt(fortune + 2) - 1;
            if (i < 0) i = 0;
            return this.quantityDropped(random) * (i + 1);
        } else {
            return this.quantityDropped(random);
        }
    }

    @Override
    public int quantityDropped(Random random) {
        if (this == RegistryHandler.deepslate_copper_ore) {
            return 2 + random.nextInt(4);
        }
        if (this == RegistryHandler.deepslate_redstone_ore) {
            return 4 + random.nextInt(2);
        }
        if (this == RegistryHandler.deepslate_lapis_ore) {
            return 4 + random.nextInt(5);
        }
        return 1;
    }
}
