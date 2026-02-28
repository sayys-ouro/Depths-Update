package sayys.depthsupdate.mixin;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(Biome.class)
public abstract class MixinBiome {
    @ModifyConstant(method = "generateBiomeTerrain", constant = @Constant(intValue = 0))
    private int depthsupdate$modifyLoopFloor(int original) {
        return -64;
    }

    @Redirect(method = "generateBiomeTerrain", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0))
    private int depthsupdate$offsetBedrock(Random random, int bound) {
        return random.nextInt(bound) - 64;
    }

}
