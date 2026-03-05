package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import sayys.depthsupdate.DepthsUpdateMod;

public class BlockCaveVines extends BlockCaveVinesBase implements IGrowable {
    public BlockCaveVines() {
        super();

        this.setRegistryName("depthsupdate", "cave_vines");
        this.setTranslationKey("cave_vines");
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            super.updateTick(worldIn, pos, state, rand);

            if (worldIn.isAirBlock(pos.down()) && rand.nextFloat() < 0.11F) {
                boolean berriesOnNew = rand.nextFloat() < 0.11F;
                worldIn.setBlockState(pos.down(), this.getDefaultState().withProperty(BERRIES, berriesOnNew));

                worldIn.setBlockState(pos, DepthsUpdateMod.RegistrationHandler.cave_vines_plant.getDefaultState().withProperty(BERRIES, state.getValue(BERRIES)));
            }
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return !state.getValue(BERRIES);
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        worldIn.setBlockState(pos, state.withProperty(BERRIES, true), 2);
    }
}
