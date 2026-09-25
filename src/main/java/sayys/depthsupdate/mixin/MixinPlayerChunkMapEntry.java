package sayys.depthsupdate.mixin;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeModContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(PlayerChunkMapEntry.class)
public abstract class MixinPlayerChunkMapEntry {
    @Shadow
    private boolean sentToPlayers;

    @Shadow
    private int changes;

    @Shadow
    private int changedSectionFilter;

    @Shadow
    @Final
    private PlayerChunkMap playerChunkMap;

    @Shadow
    @Final
    private ChunkPos pos;

    @Shadow
    @Nullable
    private Chunk chunk;

    @Shadow
    @Final
    private List<EntityPlayerMP> players;

    @Unique
    private int[] depthsupdate$changedBlocks = new int[64];

    @Shadow
    public abstract void sendPacket(Packet<?> packetIn);

    @Shadow
    protected abstract void sendBlockEntity(@Nullable TileEntity p_187273_1_);

    @Unique
    private HeightContext depthsupdate$ctx() {
        return this.chunk != null ? HeightManager.get(this.chunk.getWorld()) : HeightContext.VANILLA;
    }

    @Unique
    private boolean depthsupdate$isExtended() {
        return this.chunk != null && HeightManager.isExtended(this.chunk.getWorld());
    }

    @ModifyConstant(method = "sendToPlayers", constant = @Constant(intValue = 65535))
    private int depthsupdate$modifySendToPlayersMask(int original) {
        return depthsupdate$isExtended() ? depthsupdate$ctx().fullChunkSectionMask() : original;
    }

    @ModifyConstant(method = "sendToPlayer", constant = @Constant(intValue = 65535))
    private int depthsupdate$modifySendToPlayerMask(int original) {
        return depthsupdate$isExtended() ? depthsupdate$ctx().fullChunkSectionMask() : original;
    }

    /**
     * Replaces blockChanged for extended dimensions only: vanilla packs Y into
     * 8 bits, which cannot address negative or upper-extension coordinates.
     * Vanilla dimensions keep the vanilla path, including its changedBlocks
     * field and SPacketMultiBlockChange batching.
     */
    @Inject(method = "blockChanged", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$blockChanged(int x, int y, int z, CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        ci.cancel();

        if (this.sentToPlayers) {
            if (this.changes == 0) {
                this.playerChunkMap.entryChanged((PlayerChunkMapEntry) (Object) this);
            }

            HeightContext ctx = depthsupdate$ctx();
            int sectionY = ctx.toStorageIndex(y);

            if (sectionY < 0)
                sectionY = 0;
            if (sectionY > ctx.totalStorageSections() - 1)
                sectionY = ctx.totalStorageSections() - 1;

            this.changedSectionFilter |= 1 << sectionY;

            // Pack X in upper 4 bits, Z in next 4 bits, Y in bottom 16 bits.
            int packed = (x << 28) | (z << 24) | (y & 65535);

            for (int i = 0; i < this.changes; ++i) {
                if (this.depthsupdate$changedBlocks[i] == packed) {
                    return;
                }
            }

            if (this.changes == this.depthsupdate$changedBlocks.length) {
                this.depthsupdate$changedBlocks = Arrays.copyOf(this.depthsupdate$changedBlocks,
                        this.depthsupdate$changedBlocks.length << 1);
            }

            this.depthsupdate$changedBlocks[this.changes++] = packed;
        }
    }

    /**
     * Replaces update for extended dimensions only, unpacking the 16-bit Y
     * coordinates stored in the int[] above. Individual SPacketBlockChange
     * packets stand in for SPacketMultiBlockChange, whose wire format has no
     * room for Y outside 0..255.
     */
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$update(CallbackInfo ci) {
        if (!depthsupdate$isExtended()) {
            return;
        }

        ci.cancel();

        if (this.sentToPlayers && this.chunk != null) {
            if (this.changes != 0) {
                if (this.changes == 1) {
                    int i = (this.depthsupdate$changedBlocks[0] >> 28 & 15) + this.pos.x * 16;
                    int k = (this.depthsupdate$changedBlocks[0] >> 24 & 15) + this.pos.z * 16;
                    int j = (short) (this.depthsupdate$changedBlocks[0] & 65535);
                    BlockPos blockpos = new BlockPos(i, j, k);
                    this.sendPacket(new SPacketBlockChange(this.playerChunkMap.getWorldServer(), blockpos));
                    IBlockState state = this.playerChunkMap.getWorldServer()
                            .getBlockState(blockpos);

                    if (state.getBlock().hasTileEntity(state)) {
                        this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos));
                    }
                } else if (this.changes >= ForgeModContainer.clumpingThreshold) {
                    this.sendPacket(new SPacketChunkData(this.chunk, this.changedSectionFilter));
                } else {
                    for (int l = 0; l < this.changes; ++l) {
                        int i1 = (this.depthsupdate$changedBlocks[l] >> 28 & 15) + this.pos.x * 16;
                        int k1 = (this.depthsupdate$changedBlocks[l] >> 24 & 15) + this.pos.z * 16;
                        int j1 = (short) (this.depthsupdate$changedBlocks[l] & 65535);
                        BlockPos blockpos1 = new BlockPos(i1, j1, k1);

                        this.sendPacket(new SPacketBlockChange(this.playerChunkMap.getWorldServer(), blockpos1));

                        IBlockState state = this.playerChunkMap.getWorldServer()
                                .getBlockState(blockpos1);
                        if (state.getBlock().hasTileEntity(state)) {
                            this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos1));
                        }
                    }
                }

                this.changes = 0;
                this.changedSectionFilter = 0;
            }
        }
    }
}
