package sayys.depthsupdate.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import sayys.depthsupdate.registry.DeepslateRegistry;
import sayys.depthsupdate.registry.IHasModel;

public class BlockDeepslateOre extends Block implements IHasModel {
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
        if (this == DeepslateRegistry.deepslate_iron_ore) return DeepslateRegistry.raw_iron;
        if (this == DeepslateRegistry.deepslate_gold_ore) return DeepslateRegistry.raw_gold;
        if (this == DeepslateRegistry.deepslate_copper_ore) return DeepslateRegistry.raw_copper;
        if (this == DeepslateRegistry.deepslate_coal_ore) return Items.COAL;
        if (this == DeepslateRegistry.deepslate_diamond_ore) return Items.DIAMOND;
        if (this == DeepslateRegistry.deepslate_emerald_ore) return Items.EMERALD;
        if (this == DeepslateRegistry.deepslate_lapis_ore) return Items.DYE;
        if (this == DeepslateRegistry.deepslate_redstone_ore) return Items.REDSTONE;

        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public int damageDropped(IBlockState state) {
        if (this == DeepslateRegistry.deepslate_lapis_ore) return 4; // Lapis is blue dye (meta 4)

        return super.damageDropped(state);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune <= 0 || Item.getItemFromBlock(this) == this.getItemDropped(this.getDefaultState(), random, fortune)) {
            return this.quantityDropped(random);
        }

        if (this == DeepslateRegistry.deepslate_redstone_ore) {
            return uniformBonus(this.quantityDropped(random), fortune, random);
        }

        return oreBonus(this.quantityDropped(random), fortune, random);
    }

    @Override
    public int quantityDropped(Random random) {
        if (this == DeepslateRegistry.deepslate_copper_ore) {
            return 2 + random.nextInt(4);
        }

        if (this == DeepslateRegistry.deepslate_redstone_ore) {
            return 4 + random.nextInt(2);
        }

        if (this == DeepslateRegistry.deepslate_lapis_ore) {
            return 4 + random.nextInt(6);
        }

        return 1;
    }

    static int oreBonus(int count, int fortune, Random random) {
        return count * Math.max(1, random.nextInt(fortune + 2));
    }

    static int uniformBonus(int count, int fortune, Random random) {
        return count + random.nextInt(fortune + 1);
    }
}
