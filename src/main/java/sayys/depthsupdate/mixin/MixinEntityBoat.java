package sayys.depthsupdate.mixin;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.DepthsUpdateMod;
import sayys.depthsupdate.core.HeightManager;

@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat {
    @Shadow private double waterLevel;

    @Shadow private EntityBoat.Status status;

    /** Target distance of the boat's bottom below the water surface while floating. */
    @Unique
    private static final double depthsupdate$floatTargetSubmersion = 0.35D;

    /** Proportional gain of the float controller, per tick. */
    @Unique
    private static final double depthsupdate$floatSeekGain = 0.10D;

    @Unique
    private static final double depthsupdate$floatMaxRiseSpeed = 0.10D;

    @Unique
    private static final double depthsupdate$floatMaxSinkSpeed = 0.02D;

    @Inject(method = "updateMotion", at = @At("HEAD"))
    private void depthsupdate$sanitizeBuoyancy(CallbackInfo ci) {
        EntityBoat self = (EntityBoat) (Object) this;

        if (!HeightManager.isExtended(self.world)) {
            return;
        }

        if (this.waterLevel == Double.MIN_VALUE) {
            this.waterLevel = self.getEntityBoundingBox().minY;
        }
    }

    @Inject(method = "updateMotion", at = @At("RETURN"))
    private void depthsupdate$floatInDepths(CallbackInfo ci) {
        EntityBoat self = (EntityBoat) (Object) this;

        if (self.posY >= 0.0D || !HeightManager.isExtended(self.world)) {
            return;
        }

        AxisAlignedBB bb = self.getEntityBoundingBox();
        double surface = depthsupdate$findWaterSurface(self, bb);

        // No water near the boat, or still falling toward it: leave vanilla physics alone.
        if (Double.isNaN(surface) || surface < bb.minY - 0.05D) {
            return;
        }

        double error = (surface - depthsupdate$floatTargetSubmersion) - bb.minY;
        self.motionY = MathHelper.clamp(
            error * depthsupdate$floatSeekGain,
            -depthsupdate$floatMaxSinkSpeed,
            depthsupdate$floatMaxRiseSpeed
        );
    }

    /**
     * Highest water surface near the boat, read from block data at the bounding box's corners and center.
     */
    @Unique
    private double depthsupdate$findWaterSurface(EntityBoat self, AxisAlignedBB bb) {
        double inset = 0.1D;
        double[][] columns = {
            {bb.minX + inset, bb.minZ + inset},
            {bb.minX + inset, bb.maxZ - inset},
            {bb.maxX - inset, bb.minZ + inset},
            {bb.maxX - inset, bb.maxZ - inset},
            {(bb.minX + bb.maxX) / 2.0D, (bb.minZ + bb.maxZ) / 2.0D}
        };

        int yTop = MathHelper.floor(bb.maxY) + 1;
        int yBottom = MathHelper.floor(bb.minY) - 1;
        double best = Double.NaN;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (double[] column : columns) {
            int x = MathHelper.floor(column[0]);
            int z = MathHelper.floor(column[1]);

            for (int y = yTop; y >= yBottom; y--) {
                pos.setPos(x, y, z);
                IBlockState state = self.world.getBlockState(pos);

                if (state.getMaterial() == Material.WATER) {
                    double height = y + BlockLiquid.getBlockLiquidHeight(state, self.world, pos);

                    if (Double.isNaN(best) || height > best) {
                        best = height;
                    }

                    break;
                }
            }
        }

        return best;
    }
}
