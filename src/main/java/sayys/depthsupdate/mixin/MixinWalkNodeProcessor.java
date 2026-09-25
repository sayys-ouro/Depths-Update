package sayys.depthsupdate.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sayys.depthsupdate.core.HeightManager;

@Mixin(WalkNodeProcessor.class)
public abstract class MixinWalkNodeProcessor extends NodeProcessor {
    @Shadow protected EntityLiving currentEntity;

    @Shadow protected abstract PathNodeType getPathNodeTypeRaw(IBlockAccess blockaccessIn, int x, int y, int z);

    @Shadow public abstract PathNodeType checkNeighborBlocks(IBlockAccess blockaccessIn, int x, int y, int z, PathNodeType type);

    @Unique
    private int depthsupdate$minY() {
        EntityLiving pathing = this.currentEntity != null ? this.currentEntity : this.entity;

        return pathing != null ? HeightManager.getMinY(pathing.world) : 0;
    }

    @Unique
    private int depthsupdate$minY(IBlockAccess blockaccessIn) {
        EntityLiving pathing = this.currentEntity != null ? this.currentEntity : this.entity;

        if (pathing != null) {
            return HeightManager.getMinY(pathing.world);
        }

        return blockaccessIn instanceof World world ? HeightManager.getMinY(world) : 0;
    }

    @Redirect(
        method = "getStart",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/BlockPos;getY()I",
            ordinal = 0
        )
    )
    private int depthsupdate$startGroundY(BlockPos pos) {
        return pos.getY() - depthsupdate$minY();
    }

    @ModifyConstant(
        method = "getSafePoint",
        constant = @Constant(
            intValue = 0,
            expandZeroConditions = Constant.Condition.GREATER_THAN_ZERO
        ),
        slice = @Slice(from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;collidesWithAnyBlock(Lnet/minecraft/util/math/AxisAlignedBB;)Z"
        ))
    )
    private int depthsupdate$safePointFloor(int original) {
        return depthsupdate$minY();
    }

    @Inject(
        method = "getPathNodeType(Lnet/minecraft/world/IBlockAccess;III)Lnet/minecraft/pathfinding/PathNodeType;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void depthsupdate$getPathNodeType(
        IBlockAccess blockaccessIn,
        int x,
        int y,
        int z,
        CallbackInfoReturnable<PathNodeType> cir
    ) {
        int minY = depthsupdate$minY(blockaccessIn);
        PathNodeType pathnodetype = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);

        if (pathnodetype == PathNodeType.OPEN && y > minY) {
            Block block = blockaccessIn.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
            PathNodeType below = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);

            pathnodetype = below != PathNodeType.WALKABLE
                    && below != PathNodeType.OPEN
                    && below != PathNodeType.WATER
                    && below != PathNodeType.LAVA
                    ? PathNodeType.WALKABLE
                    : PathNodeType.OPEN;

            if (below == PathNodeType.DAMAGE_FIRE || block == Blocks.MAGMA) {
                pathnodetype = PathNodeType.DAMAGE_FIRE;
            }

            if (below == PathNodeType.DAMAGE_CACTUS) {
                pathnodetype = PathNodeType.DAMAGE_CACTUS;
            }

            if (below == PathNodeType.DAMAGE_OTHER) {
                pathnodetype = PathNodeType.DAMAGE_OTHER;
            }
        }

        cir.setReturnValue(this.checkNeighborBlocks(blockaccessIn, x, y, z, pathnodetype));
    }
}
