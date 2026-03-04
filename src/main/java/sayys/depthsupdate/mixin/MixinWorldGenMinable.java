package sayys.depthsupdate.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenMinable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldGenMinable.class)
public abstract class MixinWorldGenMinable {
    @ModifyVariable(method = "generate", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private @NonNull BlockPos depthsupdate$offsetModdedOres(@NonNull BlockPos pos) {
        int y = pos.getY();

        if (y >= 0 && y <= 64) {
            if (Math.random() < 0.5) {
                int newY = -64 + (int) (Math.random() * (y + 64 + 1));

                return new BlockPos(pos.getX(), newY, pos.getZ());
            }
        }

        return pos;
    }
}
