package sayys.depthsupdate.mixin;

import net.minecraft.block.BlockSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import sayys.depthsupdate.core.HeightManager;

@Mixin(BlockSkull.class)
public abstract class MixinBlockSkull {
    @Redirect(
        method = "checkWitherSpawn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I", ordinal = 0)
    )
    private int depthsupdate$witherSpawnY(
        BlockPos instance, World worldIn, BlockPos pos, TileEntitySkull te
    ) {
        return instance.getY() - HeightManager.getMinY(worldIn);
    }

    @Redirect(
        method = "canDispenserPlace",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I", ordinal = 0)
    )
    private int depthsupdate$dispenserPlaceY(
        BlockPos instance, World worldIn, BlockPos pos, ItemStack stack
    ) {
        return instance.getY() - HeightManager.getMinY(worldIn);
    }
}
