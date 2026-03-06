package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import sayys.depthsupdate.DepthsUpdateMod;
import sayys.depthsupdate.registry.RegistryHandler;

public class BlockDeepslate extends Block {
    public BlockDeepslate() {
        super(Material.ROCK);

        this.setRegistryName("depthsupdate", "deepslate");
        this.setTranslationKey("deepslate");
        this.setHardness(3.0F);
        this.setResistance(6.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.STONE;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(RegistryHandler.cobbled_deepslate);
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isReplaceableOreGen(
        IBlockState state,
        IBlockAccess world,
        BlockPos pos,
        com.google.common.base.Predicate<IBlockState> target
    ) {
        if (target != null && target.apply(net.minecraft.init.Blocks.STONE.getDefaultState())) {
            return true;
        }

        return super.isReplaceableOreGen(state, world, pos, target);
    }
}
