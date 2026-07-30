package sayys.depthsupdate.compat;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;

public final class FluidloggedCompat {
    public static final String MOD_ID = "fluidlogged_api";

    private FluidloggedCompat() {}

    public static boolean isLoaded() {
        return Present.VALUE;
    }

    public static boolean hasFluid(IBlockAccess world, BlockPos pos) {
        return Present.VALUE && Hooks.hasFluid(world, pos);
    }

    public static void logWater(World world, BlockPos pos, IBlockState here) {
        if (Present.VALUE) {
            Hooks.logWater(world, pos, here);
        }
    }

    private static final class Present {
        private static final boolean VALUE = Loader.isModLoaded(MOD_ID);
    }

    private static final class Hooks {
        private static boolean hasFluid(IBlockAccess world, BlockPos pos) {
            return !FluidState.get(world, pos).isEmpty();
        }

        private static void logWater(World world, BlockPos pos, IBlockState here) {
            FluidloggedUtils.setFluidState(
                world,
                pos,
                here,
                FluidState.of(FluidRegistry.WATER),
                false,
                2
            );
        }
    }
}
