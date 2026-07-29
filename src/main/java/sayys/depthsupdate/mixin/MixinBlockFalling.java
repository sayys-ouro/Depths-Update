package sayys.depthsupdate.mixin;

import net.minecraft.block.BlockFalling;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import sayys.depthsupdate.core.HeightManager;

@Mixin(BlockFalling.class)
public abstract class MixinBlockFalling {
    @ModifyConstant(
        method = "checkFallable",
        constant = @Constant(
            intValue = 0,
            expandZeroConditions = Constant.Condition.GREATER_THAN_OR_EQUAL_TO_ZERO
        )
    )
    private int depthsupdate$fallFloor(int original, World worldIn, BlockPos pos) {
        return HeightManager.getMinY(worldIn);
    }

    @ModifyConstant(
        method = "checkFallable",
        constant = @Constant(intValue = 0, expandZeroConditions = Constant.Condition.GREATER_THAN_ZERO)
    )
    private int depthsupdate$teleportFloor(int original, World worldIn, BlockPos pos) {
        return HeightManager.getMinY(worldIn);
    }
}
