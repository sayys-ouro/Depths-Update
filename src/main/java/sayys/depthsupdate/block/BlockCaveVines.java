package sayys.depthsupdate.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import sayys.depthsupdate.registry.PlantRegistry;

public class BlockCaveVines extends BlockCaveVinesBase {
    public BlockCaveVines() {
        super();

        this.setRegistryName("depthsupdate", "cave_vines");
        this.setTranslationKey("cave_vines");
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(
        World worldIn, BlockPos pos, IBlockState state, Random rand
    ) {
        if (!worldIn.isRemote) {
            if (worldIn.isAirBlock(pos.down()) && rand.nextFloat() < 0.11F) {
                boolean berriesOnNew = rand.nextFloat() < 0.11F;

                worldIn.setBlockState(
                    pos.down(),
                    this.getDefaultState().withProperty(BERRIES, berriesOnNew)
                );

                worldIn.setBlockState(
                    pos,
                    PlantRegistry.cave_vines_plant.getDefaultState().withProperty(
                        BERRIES, state.getValue(BERRIES)
                    )
                );
            }
        }
    }
}
