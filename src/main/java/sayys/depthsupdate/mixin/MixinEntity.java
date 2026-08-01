package sayys.depthsupdate.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import sayys.depthsupdate.core.HeightManager;

/**
 * Adjusts the void damage Y threshold to match the configured void damage level
 * instead of vanilla's hardcoded -64.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public World world;

    @ModifyConstant(method = "onEntityUpdate", constant = @Constant(doubleValue = -64.0D))
    private double depthsupdate$modifyVoidDamageLevel(double original) {
        return (double) HeightManager.getVoidDamageLevel(this.world);
    }
}
