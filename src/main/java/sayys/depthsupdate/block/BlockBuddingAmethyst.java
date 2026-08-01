package sayys.depthsupdate.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import sayys.depthsupdate.registry.IHasModel;
import sayys.depthsupdate.registry.AmethystRegistry;

public class BlockBuddingAmethyst extends Block implements IHasModel {
    public BlockBuddingAmethyst() {
        super(Material.GLASS);

        this.setRegistryName("depthsupdate", "budding_amethyst");
        this.setTranslationKey("budding_amethyst");
        this.setHardness(1.5F);
        this.setResistance(1.5F);
        this.setSoundType(SoundType.GLASS);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setTickRandomly(true);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    protected boolean canSilkHarvest() {
        return false;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextInt(5) == 0) {
            EnumFacing growDirection = EnumFacing.VALUES[rand.nextInt(EnumFacing.VALUES.length)];
            BlockPos growPos = pos.offset(growDirection);
            IBlockState relativeState = worldIn.getBlockState(growPos);
            Block nextStage = null;

            if (canClusterGrowAtState(relativeState)) {
                nextStage = AmethystRegistry.small_amethyst_bud;
            } else if (relativeState.getBlock() == AmethystRegistry.small_amethyst_bud && relativeState.getValue(BlockAmethystCluster.FACING) == growDirection) {
                nextStage = AmethystRegistry.medium_amethyst_bud;
            } else if (relativeState.getBlock() == AmethystRegistry.medium_amethyst_bud && relativeState.getValue(BlockAmethystCluster.FACING) == growDirection) {
                nextStage = AmethystRegistry.large_amethyst_bud;
            } else if (relativeState.getBlock() == AmethystRegistry.large_amethyst_bud && relativeState.getValue(BlockAmethystCluster.FACING) == growDirection) {
                nextStage = AmethystRegistry.amethyst_cluster;
            }

            if (nextStage != null) {
                IBlockState targetState = nextStage.getDefaultState().withProperty(BlockAmethystCluster.FACING, growDirection);
                worldIn.setBlockState(growPos, targetState);
            }
        }
    }

    public static boolean canClusterGrowAtState(IBlockState state) {
        return state.getMaterial() == Material.AIR || state.getMaterial() == Material.WATER;
    }
}
