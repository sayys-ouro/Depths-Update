package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockBuddingAmethyst extends Block {
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
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextInt(5) == 0) {
            EnumFacing growDirection = EnumFacing.values()[rand.nextInt(EnumFacing.values().length)];
            BlockPos growPos = pos.offset(growDirection);
            IBlockState relativeState = worldIn.getBlockState(growPos);
            Block nextStage = null;

            if (canClusterGrowAtState(relativeState)) {
                nextStage = RegistryHandler.small_amethyst_bud;
            } else if (relativeState.getBlock() == RegistryHandler.small_amethyst_bud && relativeState.getValue(BlockAmethystCluster.FACING) == growDirection) {
                nextStage = RegistryHandler.medium_amethyst_bud;
            } else if (relativeState.getBlock() == RegistryHandler.medium_amethyst_bud && relativeState.getValue(BlockAmethystCluster.FACING) == growDirection) {
                nextStage = RegistryHandler.large_amethyst_bud;
            } else if (relativeState.getBlock() == RegistryHandler.large_amethyst_bud && relativeState.getValue(BlockAmethystCluster.FACING) == growDirection) {
                nextStage = RegistryHandler.amethyst_cluster;
            }

            if (nextStage != null) {
                IBlockState targetState = nextStage.getDefaultState().withProperty(BlockAmethystCluster.FACING, growDirection);
                worldIn.setBlockState(growPos, targetState);
            }
        }
    }

    public static boolean canClusterGrowAtState(IBlockState state) {
        return state.getBlock().isAir(state, null, null) || (state.getMaterial() == Material.WATER);
    }
}
