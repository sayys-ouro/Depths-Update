package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCaveVinesPlant extends BlockCaveVinesBase implements IGrowable {
    public BlockCaveVinesPlant() {
        super();

        this.setRegistryName("depthsupdate", "cave_vines_plant");
        this.setTranslationKey("cave_vines_plant");
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
